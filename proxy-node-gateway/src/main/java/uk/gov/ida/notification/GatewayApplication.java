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
import uk.gov.ida.dropwizard.logstash.LogstashBundle;
import uk.gov.ida.notification.exceptions.mappers.AuthnRequestExceptionMapper;
import uk.gov.ida.notification.exceptions.mappers.GenericExceptionMapper;
import uk.gov.ida.notification.exceptions.mappers.HubResponseExceptionMapper;
import uk.gov.ida.notification.healthcheck.ProxyNodeHealthCheck;
import uk.gov.ida.notification.pki.KeyPairConfiguration;
import uk.gov.ida.notification.proxy.EidasSamlParserProxy;
import uk.gov.ida.notification.resources.EidasAuthnRequestResourceV2;
import uk.gov.ida.notification.resources.HubResponseResource;
import uk.gov.ida.notification.saml.ResponseAssertionDecrypter;
import uk.gov.ida.notification.saml.ResponseAssertionFactory;
import uk.gov.ida.notification.saml.SamlParser;
import uk.gov.ida.notification.saml.converters.AuthnRequestParameterProvider;
import uk.gov.ida.notification.saml.converters.ResponseParameterProvider;
import uk.gov.ida.notification.saml.deprecate.DestinationValidator;
import uk.gov.ida.notification.saml.deprecate.EncryptedResponseFromIdpValidator;
import uk.gov.ida.notification.saml.deprecate.IdpResponseValidator;
import uk.gov.ida.notification.saml.deprecate.SamlStatusToIdaStatusCodeMapper;
import uk.gov.ida.notification.saml.validation.HubResponseValidator;
import uk.gov.ida.notification.saml.validation.components.LoaValidator;
import uk.gov.ida.notification.saml.validation.components.ResponseAttributesValidator;
import uk.gov.ida.notification.shared.proxy.VerifyServiceProviderProxy;
import uk.gov.ida.saml.metadata.MetadataConfiguration;
import uk.gov.ida.saml.metadata.MetadataHealthCheck;
import uk.gov.ida.saml.metadata.bundle.MetadataResolverBundle;
import uk.gov.ida.saml.security.SamlAssertionsSignatureValidator;
import uk.gov.ida.saml.security.SamlMessageSignatureValidator;
import uk.gov.ida.saml.security.validators.signature.SamlResponseSignatureValidator;

import java.net.URI;

import static uk.gov.ida.notification.saml.SamlSignatureValidatorFactory.createSamlMessageSignatureValidator;

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
        bootstrap.addBundle(new LogstashBundle());

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

        HubResponseValidator hubResponseValidator = createHubResponseValidator(configuration);


        EidasSamlParserProxy espProxy = configuration
            .getEidasSamlParserServiceConfiguration()
            .buildEidasSamlParserService(environment);

        VerifyServiceProviderProxy vspProxy = configuration
            .getVerifyServiceProviderConfiguration()
            .buildVerifyServiceProviderProxy(environment);

        environment.jersey().register(new EidasAuthnRequestResourceV2(
                espProxy,
                vspProxy,
                samlFormViewBuilder));

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
}
