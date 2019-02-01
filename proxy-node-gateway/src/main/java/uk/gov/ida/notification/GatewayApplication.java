package uk.gov.ida.notification;

import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import org.eclipse.jetty.server.session.SessionHandler;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.security.credential.BasicCredential;
import se.litsec.opensaml.saml2.common.response.MessageReplayChecker;
import uk.gov.ida.bundles.LoggingBundle;
import uk.gov.ida.notification.healthcheck.ProxyNodeHealthCheck;
import uk.gov.ida.notification.exceptions.mappers.AuthnRequestExceptionMapper;
import uk.gov.ida.notification.exceptions.mappers.GenericExceptionMapper;
import uk.gov.ida.notification.exceptions.mappers.HubResponseExceptionMapper;
import uk.gov.ida.notification.pki.KeyPairConfiguration;
import uk.gov.ida.notification.resources.EidasAuthnRequestResource;
import uk.gov.ida.notification.resources.HubResponseResource;
import uk.gov.ida.notification.saml.EidasAuthnRequestTranslator;
import uk.gov.ida.notification.saml.ResponseAssertionDecrypter;
import uk.gov.ida.notification.saml.ResponseAssertionFactory;
import uk.gov.ida.notification.saml.SamlObjectSigner;
import uk.gov.ida.notification.saml.SamlParser;
import uk.gov.ida.notification.saml.converters.AuthnRequestParameterProvider;
import uk.gov.ida.notification.saml.converters.ResponseParameterProvider;
import uk.gov.ida.notification.saml.deprecate.DestinationValidator;
import uk.gov.ida.notification.saml.deprecate.EncryptedResponseFromIdpValidator;
import uk.gov.ida.notification.saml.deprecate.IdpResponseValidator;
import uk.gov.ida.notification.saml.deprecate.SamlStatusToIdaStatusCodeMapper;
import uk.gov.ida.notification.saml.validation.EidasAuthnRequestValidator;
import uk.gov.ida.notification.saml.validation.HubResponseValidator;
import uk.gov.ida.notification.saml.validation.components.AssertionConsumerServiceValidator;
import uk.gov.ida.notification.saml.validation.components.ComparisonValidator;
import uk.gov.ida.notification.saml.validation.components.LoaValidator;
import uk.gov.ida.notification.saml.validation.components.RequestIssuerValidator;
import uk.gov.ida.notification.saml.validation.components.RequestedAttributesValidator;
import uk.gov.ida.notification.saml.validation.components.ResponseAttributesValidator;
import uk.gov.ida.notification.saml.validation.components.SpTypeValidator;
import uk.gov.ida.saml.metadata.MetadataConfiguration;
import uk.gov.ida.saml.metadata.MetadataHealthCheck;
import uk.gov.ida.saml.metadata.bundle.MetadataResolverBundle;
import uk.gov.ida.saml.security.SamlAssertionsSignatureValidator;
import uk.gov.ida.saml.security.SamlMessageSignatureValidator;
import uk.gov.ida.saml.security.validators.signature.SamlRequestSignatureValidator;
import uk.gov.ida.saml.security.validators.signature.SamlResponseSignatureValidator;

import java.net.URI;

import static uk.gov.ida.notification.saml.SamlSignatureValidatorFactory.createSamlMessageSignatureValidator;
import static uk.gov.ida.notification.saml.SamlSignatureValidatorFactory.createSamlRequestSignatureValidator;

public class GatewayApplication extends Application<GatewayConfiguration> {

    private MetadataResolverBundle<GatewayConfiguration> hubMetadataResolverBundle;
    private MetadataResolverBundle<GatewayConfiguration> connectorMetadataResolverBundle;

    @SuppressWarnings("WeakerAccess") // Needed for DropwizardAppRules
    public GatewayApplication() {
    }

    public static void main(final String[] args) throws Exception {
        if (args == null || args.length == 0) {
            String configFile = System.getenv("CONFIG_FILE");

            if (configFile == null) {
                throw new RuntimeException("CONFIG_FILE environment variable should be set with path to configuration file");
            }

            new GatewayApplication().run("server", configFile);
        } else {
            new GatewayApplication().run(args);
        }
    }

    @Override
    public String getName() {
        return "EidasProxyNode";
    }

    @Override
    public void initialize(final Bootstrap<GatewayConfiguration> bootstrap) {
        // Needed to correctly interpolate environment variables in config file
        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(bootstrap.getConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor(false)
                )
        );

        // Needed to initialise OpenSAML libraries
        // The eidas-opensaml3 library provides its own initializer that will be executed
        // by the InitializationService
        try {
            InitializationService.initialize();
        } catch (InitializationException e) {
            throw new RuntimeException(e);
        }

        VerifySamlInitializer.init();

        bootstrap.addBundle(new ViewBundle<>());
        bootstrap.addBundle(new LoggingBundle());

        // Metadata
        hubMetadataResolverBundle = new MetadataResolverBundle<>(GatewayConfiguration::getHubMetadataConfiguration);
        bootstrap.addBundle(hubMetadataResolverBundle);

        connectorMetadataResolverBundle = new MetadataResolverBundle<>(GatewayConfiguration::getConnectorMetadataConfiguration);
        bootstrap.addBundle(connectorMetadataResolverBundle);
    }

    @Override
    public void run(final GatewayConfiguration configuration,
                    final Environment environment) throws Exception {

        ProxyNodeHealthCheck proxyNodeHealthCheck = new ProxyNodeHealthCheck("gateway");
        environment.healthChecks().register(proxyNodeHealthCheck.getName(), proxyNodeHealthCheck);

        registerMetadataHealthCheck(
                hubMetadataResolverBundle.getMetadataResolver(),
                configuration.getHubMetadataConfiguration(),
                environment,
                "hub-metadata");

        registerMetadataHealthCheck(
                connectorMetadataResolverBundle.getMetadataResolver(),
                configuration.getConnectorMetadataConfiguration(),
                environment,
                "connector-metadata");

        registerProviders(environment);
        registerExceptionMappers(environment);
        registerResources(configuration, environment);
    }

    private void registerProviders(Environment environment) {
        environment.jersey().register(AuthnRequestParameterProvider.class);
        environment.jersey().register(ResponseParameterProvider.class);
        SessionHandler sessionHandler = new SessionHandler();
        sessionHandler.setSessionCookie("gateway-session");
        environment.servlets().setSessionHandler(sessionHandler);
    }

    private void registerExceptionMappers(Environment environment) {
        environment.jersey().register(new GenericExceptionMapper());
        environment.jersey().register(new AuthnRequestExceptionMapper());
        environment.jersey().register(new HubResponseExceptionMapper());
    }

    private void registerResources(GatewayConfiguration configuration, Environment environment) throws Exception {
        SamlFormViewBuilder samlFormViewBuilder = new SamlFormViewBuilder();

        HubAuthnRequestGenerator hubAuthnRequestGenerator = createHubAuthnRequestGenerator(configuration);

        EidasAuthnRequestValidator eidasAuthnRequestValidator = createEidasAuthnRequestValidator(configuration, connectorMetadataResolverBundle);
        HubResponseValidator hubResponseValidator = createHubResponseValidator(configuration);

        SamlRequestSignatureValidator samlRequestSignatureValidator = createSamlRequestSignatureValidator(connectorMetadataResolverBundle);

        environment.jersey().register(new EidasAuthnRequestResource(
                configuration,
                hubAuthnRequestGenerator,
                samlFormViewBuilder,
                eidasAuthnRequestValidator,
                samlRequestSignatureValidator));

        TranslatorService translatorService = configuration
            .getTranslatorServiceConfiguration()
            .buildTranslatorService(environment, new SamlParser());

        environment.jersey().register(new HubResponseResource(
                samlFormViewBuilder,
                configuration.getConnectorNodeUrl().toString(),
                translatorService,
                hubResponseValidator
        ));
    }

    public void registerMetadataHealthCheck(MetadataResolver metadataResolver, MetadataConfiguration connectorMetadataConfiguration, Environment environment, String name) {
        MetadataHealthCheck metadataHealthCheck = new MetadataHealthCheck(
                metadataResolver,
                name,
                connectorMetadataConfiguration.getExpectedEntityId()
        );

        environment.healthChecks().register(metadataHealthCheck.getName(), metadataHealthCheck);
    }

    private HubResponseValidator createHubResponseValidator(GatewayConfiguration configuration) throws Exception {
        URI proxyNodeResponseUrl = configuration.getProxyNodeResponseUrl();
        String proxyNodeEntityId = configuration.getProxyNodeEntityId();

        SamlMessageSignatureValidator hubResponseMessageSignatureValidator = createSamlMessageSignatureValidator(hubMetadataResolverBundle);
        ResponseAssertionDecrypter responseAssertionDecrypter = createDecrypter(configuration.getHubFacingEncryptionKeyPair());

        MessageReplayChecker replayChecker = configuration.getReplayChecker().createMessageReplayChecker("gateway-hub");

        IdpResponseValidator idpResponseValidator = new IdpResponseValidator(
                new SamlResponseSignatureValidator(hubResponseMessageSignatureValidator),
                responseAssertionDecrypter.getAssertionDecrypter(),
                new SamlAssertionsSignatureValidator(hubResponseMessageSignatureValidator),
                new EncryptedResponseFromIdpValidator<>(new SamlStatusToIdaStatusCodeMapper()),
                new DestinationValidator(proxyNodeResponseUrl, proxyNodeResponseUrl.getPath()),
                ResponseAssertionFactory.createResponseAssertionsFromIdpValidator("Gateway", proxyNodeEntityId, replayChecker)
        );
        ResponseAttributesValidator responseAttributesValidator = new ResponseAttributesValidator();
        return new HubResponseValidator(
                idpResponseValidator,
                responseAttributesValidator,
                new LoaValidator());
    }

    private ResponseAssertionDecrypter createDecrypter(KeyPairConfiguration configuration) {
        BasicCredential decryptionCredential = new BasicCredential(
                configuration.getPublicKey().getPublicKey(),
                configuration.getPrivateKey().getPrivateKey()
        );
        return new ResponseAssertionDecrypter(decryptionCredential);
    }

    private EidasAuthnRequestValidator createEidasAuthnRequestValidator(GatewayConfiguration configuration, MetadataResolverBundle hubMetadataResolverBundle) throws Exception {
        MessageReplayChecker replayChecker = configuration.getReplayChecker().createMessageReplayChecker("gateway-hub");

        return new EidasAuthnRequestValidator(
            new RequestIssuerValidator(),
            new SpTypeValidator(),
            new LoaValidator(),
            new RequestedAttributesValidator(),
            replayChecker,
            new ComparisonValidator(),
            createDestinationValidator(configuration),
            new AssertionConsumerServiceValidator(hubMetadataResolverBundle.getMetadataResolver())
        );
    }

    private DestinationValidator createDestinationValidator(GatewayConfiguration configuration) {
        return new DestinationValidator(configuration.getProxyNodeAuthnRequestUrl(), configuration.getProxyNodeAuthnRequestUrl().getPath());
    }

    private HubAuthnRequestGenerator createHubAuthnRequestGenerator(GatewayConfiguration configuration) {
        EidasAuthnRequestTranslator eidasAuthnRequestTranslator = new EidasAuthnRequestTranslator(
                configuration.getProxyNodeEntityId(),
                configuration.getHubUrl().toString());
        SamlObjectSigner signer = new SamlObjectSigner(
                configuration.getHubFacingSigningKeyPair().getPublicKey().getPublicKey(),
                configuration.getHubFacingSigningKeyPair().getPrivateKey().getPrivateKey(),
                configuration.getHubFacingSigningKeyPair().getPublicKey().getCert()
        );
        return new HubAuthnRequestGenerator(eidasAuthnRequestTranslator, signer);
    }
}
