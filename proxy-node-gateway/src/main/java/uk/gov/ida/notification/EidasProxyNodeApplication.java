package uk.gov.ida.notification;

import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.security.impl.MetadataCredentialResolver;
import org.opensaml.security.credential.BasicCredential;
import org.opensaml.xmlsec.config.DefaultSecurityConfigurationBootstrap;
import org.opensaml.xmlsec.keyinfo.KeyInfoCredentialResolver;
import org.opensaml.xmlsec.signature.support.impl.ExplicitKeySignatureTrustEngine;
import uk.gov.ida.notification.exceptions.mappers.AuthnRequestExceptionMapper;
import uk.gov.ida.notification.exceptions.mappers.HubResponseExceptionMapper;
import uk.gov.ida.notification.healthcheck.GatewayHealthCheck;
import uk.gov.ida.notification.pki.KeyPairConfiguration;
import uk.gov.ida.notification.resources.EidasAuthnRequestResource;
import uk.gov.ida.notification.resources.HubResponseResource;
import uk.gov.ida.notification.saml.ResponseAssertionDecrypter;
import uk.gov.ida.notification.saml.SamlObjectSigner;
import uk.gov.ida.notification.saml.converters.AuthnRequestParameterProvider;
import uk.gov.ida.notification.saml.converters.ResponseParameterProvider;
import uk.gov.ida.notification.saml.metadata.Metadata;
import uk.gov.ida.notification.saml.metadata.MetadataCredentialResolverInitializer;
import uk.gov.ida.notification.saml.translation.EidasAuthnRequestTranslator;
import uk.gov.ida.notification.saml.validation.EidasAuthnRequestValidator;
import uk.gov.ida.notification.saml.validation.HubResponseValidator;
import uk.gov.ida.notification.saml.validation.components.LoaValidator;
import uk.gov.ida.notification.saml.validation.components.RequestIssuerValidator;
import uk.gov.ida.notification.saml.validation.components.RequestedAttributesValidator;
import uk.gov.ida.notification.saml.validation.components.ResponseAttributesValidator;
import uk.gov.ida.notification.saml.validation.components.SpTypeValidator;
import uk.gov.ida.saml.core.validators.DestinationValidator;
import uk.gov.ida.saml.core.validators.assertion.AssertionAttributeStatementValidator;
import uk.gov.ida.saml.core.validators.assertion.AuthnStatementAssertionValidator;
import uk.gov.ida.saml.core.validators.assertion.DuplicateAssertionValidator;
import uk.gov.ida.saml.core.validators.assertion.IPAddressValidator;
import uk.gov.ida.saml.core.validators.assertion.IdentityProviderAssertionValidator;
import uk.gov.ida.saml.core.validators.assertion.MatchingDatasetAssertionValidator;
import uk.gov.ida.saml.core.validators.subject.AssertionSubjectValidator;
import uk.gov.ida.saml.core.validators.subjectconfirmation.AssertionSubjectConfirmationValidator;
import uk.gov.ida.saml.hub.transformers.inbound.SamlStatusToIdaStatusCodeMapper;
import uk.gov.ida.saml.hub.validators.response.idp.IdpResponseValidator;
import uk.gov.ida.saml.hub.validators.response.idp.components.EncryptedResponseFromIdpValidator;
import uk.gov.ida.saml.hub.validators.response.idp.components.ResponseAssertionsFromIdpValidator;
import uk.gov.ida.saml.metadata.MetadataConfiguration;
import uk.gov.ida.saml.metadata.MetadataHealthCheck;
import uk.gov.ida.saml.metadata.bundle.MetadataResolverBundle;
import uk.gov.ida.saml.security.MetadataBackedSignatureValidator;
import uk.gov.ida.saml.security.SamlAssertionsSignatureValidator;
import uk.gov.ida.saml.security.SamlMessageSignatureValidator;
import uk.gov.ida.saml.security.validators.issuer.IssuerValidator;
import uk.gov.ida.saml.security.validators.signature.SamlRequestSignatureValidator;
import uk.gov.ida.saml.security.validators.signature.SamlResponseSignatureValidator;

import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;

public class EidasProxyNodeApplication extends Application<EidasProxyNodeConfiguration> {

    private Metadata connectorMetadata;

    private MetadataResolverBundle<EidasProxyNodeConfiguration> hubMetadataResolverBundle;
    private MetadataResolverBundle<EidasProxyNodeConfiguration> connectorMetadataResolverBundle;

    @SuppressWarnings("WeakerAccess") // Needed for DropwizardAppRules
    public EidasProxyNodeApplication() {
    }

    public static void main(final String[] args) throws Exception {
        if (args == null || args.length == 0) {
            String configFile = System.getenv("CONFIG_FILE");

            if (configFile == null) {
                throw new RuntimeException("CONFIG_FILE environment variable should be set with path to configuration file");
            }

            new EidasProxyNodeApplication().run("server", configFile);
        } else {
            new EidasProxyNodeApplication().run(args);
        }
    }

    @Override
    public String getName() {
        return "EidasProxyNode";
    }

    @Override
    public void initialize(final Bootstrap<EidasProxyNodeConfiguration> bootstrap) {
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
        } catch(InitializationException e) {
            throw new RuntimeException(e);
        }

        // Verify SAML
        VerifySamlInitializer.init();

        // Views
        bootstrap.addBundle(new ViewBundle<>());

        // Metadata
        hubMetadataResolverBundle = new MetadataResolverBundle<>(EidasProxyNodeConfiguration::getHubMetadataConfiguration);
        bootstrap.addBundle(hubMetadataResolverBundle);

        connectorMetadataResolverBundle = new MetadataResolverBundle<>(EidasProxyNodeConfiguration::getConnectorMetadataConfiguration);
        bootstrap.addBundle(connectorMetadataResolverBundle);
    }

    @Override
    public void run(final EidasProxyNodeConfiguration configuration,
                    final Environment environment) throws
            ComponentInitializationException {

        connectorMetadata = createMetadata(connectorMetadataResolverBundle);
        GatewayHealthCheck gatewayHealthCheck = new GatewayHealthCheck();

        environment.healthChecks().register(gatewayHealthCheck.getName(), gatewayHealthCheck);

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
    }

    private void registerExceptionMappers(Environment environment) {
        environment.jersey().register(new HubResponseExceptionMapper());
        environment.jersey().register(new AuthnRequestExceptionMapper());
    }

    private void registerResources(EidasProxyNodeConfiguration configuration, Environment environment) throws ComponentInitializationException {
        SamlFormViewBuilder samlFormViewBuilder = new SamlFormViewBuilder();

        HubAuthnRequestGenerator hubAuthnRequestGenerator = createHubAuthnRequestGenerator(configuration);

        EidasAuthnRequestValidator eidasAuthnRequestValidator = createEidasAuthnRequestValidator();
        HubResponseValidator hubResponseValidator = createHubResponseValidator(configuration);

        SamlRequestSignatureValidator samlRequestSignatureValidator = createSamlRequestSignatureValidator(connectorMetadataResolverBundle);

        environment.jersey().register(new EidasAuthnRequestResource(
                configuration,
                hubAuthnRequestGenerator,
                samlFormViewBuilder,
                eidasAuthnRequestValidator,
                samlRequestSignatureValidator));

        environment.jersey().register(new HubResponseResource(
                samlFormViewBuilder,
                configuration.getConnectorNodeUrl().toString(),
                hubResponseValidator,
                environment,
                configuration.getTranslatorUrl().toString()
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

    private HubResponseValidator createHubResponseValidator(EidasProxyNodeConfiguration configuration) throws ComponentInitializationException {
        URI proxyNodeResponseUrl = configuration.getProxyNodeResponseUrl();
        String proxyNodeEntityId = configuration.getProxyNodeEntityId();

        SamlMessageSignatureValidator hubResponseMessageSignatureValidator = createSamlMessagesSignatureValidator(hubMetadataResolverBundle);
        ResponseAssertionDecrypter responseAssertionDecrypter = createDecrypter(configuration.getHubFacingEncryptionKeyPair());

        IdpResponseValidator idpResponseValidator = new IdpResponseValidator(
            new SamlResponseSignatureValidator(hubResponseMessageSignatureValidator),
                responseAssertionDecrypter.getAssertionDecrypter(),
            new SamlAssertionsSignatureValidator(hubResponseMessageSignatureValidator),
            new EncryptedResponseFromIdpValidator<>(new SamlStatusToIdaStatusCodeMapper()),
            new DestinationValidator(proxyNodeResponseUrl, proxyNodeResponseUrl.getPath()),
            createResponseAssertionsFromIdpValidator(proxyNodeEntityId)
        );
        ResponseAttributesValidator responseAttributesValidator = new ResponseAttributesValidator();
        return new HubResponseValidator(
            idpResponseValidator,
            responseAttributesValidator,
            new LoaValidator());
    }

    private ResponseAssertionsFromIdpValidator createResponseAssertionsFromIdpValidator(String proxyNodeEntityId) {
        IdentityProviderAssertionValidator assertionValidator = new IdentityProviderAssertionValidator(
            new IssuerValidator(),
            new AssertionSubjectValidator(),
            new AssertionAttributeStatementValidator(),
            new AssertionSubjectConfirmationValidator()
        );
        DuplicateAssertionValidator duplicateAssertionValidator = new DuplicateAssertionValidator(new ConcurrentHashMap<>());
        return new ResponseAssertionsFromIdpValidator(
            assertionValidator,
            new MatchingDatasetAssertionValidator(duplicateAssertionValidator),
            new AuthnStatementAssertionValidator(duplicateAssertionValidator),
            new IPAddressValidator(),
            proxyNodeEntityId
        );
    }

    private Metadata createMetadata(MetadataResolverBundle bundle) throws ComponentInitializationException {
        MetadataCredentialResolver metadataCredentialResolver = new MetadataCredentialResolverInitializer(bundle.getMetadataResolver()).initialize();
        return new Metadata(metadataCredentialResolver);
    }

    private ResponseAssertionDecrypter createDecrypter(KeyPairConfiguration configuration) {
        BasicCredential decryptionCredential = new BasicCredential(
                configuration.getPublicKey().getPublicKey(),
                configuration.getPrivateKey().getPrivateKey()
        );
        return new ResponseAssertionDecrypter(decryptionCredential);
    }

    private EidasAuthnRequestValidator createEidasAuthnRequestValidator() {
        return new EidasAuthnRequestValidator(
            new RequestIssuerValidator(),
            new SpTypeValidator(),
            new LoaValidator(),
            new RequestedAttributesValidator()
        );
    }

    private SamlMessageSignatureValidator createSamlMessagesSignatureValidator(MetadataResolverBundle hubMetadataResolverBundle) throws ComponentInitializationException {
        MetadataCredentialResolver hubMetadataCredentialResolver = new MetadataCredentialResolverInitializer(hubMetadataResolverBundle.getMetadataResolver()).initialize();
        KeyInfoCredentialResolver keyInfoCredentialResolver = DefaultSecurityConfigurationBootstrap.buildBasicInlineKeyInfoCredentialResolver();
        ExplicitKeySignatureTrustEngine explicitKeySignatureTrustEngine = new ExplicitKeySignatureTrustEngine(hubMetadataCredentialResolver, keyInfoCredentialResolver);
        MetadataBackedSignatureValidator metadataBackedSignatureValidator = MetadataBackedSignatureValidator.withoutCertificateChainValidation(explicitKeySignatureTrustEngine);
        return new SamlMessageSignatureValidator(metadataBackedSignatureValidator);
    }

    private SamlRequestSignatureValidator createSamlRequestSignatureValidator(MetadataResolverBundle hubMetadataResolverBundle) throws ComponentInitializationException {
        SamlMessageSignatureValidator samlMessageSignatureValidator = createSamlMessagesSignatureValidator(hubMetadataResolverBundle);
        return new SamlRequestSignatureValidator(samlMessageSignatureValidator);
    }

    private HubAuthnRequestGenerator createHubAuthnRequestGenerator(EidasProxyNodeConfiguration configuration) {
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
