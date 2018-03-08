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
import uk.gov.ida.notification.saml.translation.EidasResponseBuilder;
import uk.gov.ida.notification.saml.translation.HubResponseTranslator;
import uk.gov.ida.notification.saml.validation.EidasAuthnRequestValidator;
import uk.gov.ida.notification.saml.validation.components.LoaValidator;
import uk.gov.ida.notification.saml.validation.components.RequestIssuerValidator;
import uk.gov.ida.notification.saml.validation.components.RequestedAttributesValidator;
import uk.gov.ida.notification.saml.validation.components.SpTypeValidator;
import uk.gov.ida.saml.metadata.MetadataConfiguration;
import uk.gov.ida.saml.metadata.MetadataHealthCheck;
import uk.gov.ida.saml.metadata.bundle.MetadataResolverBundle;
import uk.gov.ida.saml.security.MetadataBackedSignatureValidator;
import uk.gov.ida.saml.security.SamlMessageSignatureValidator;
import uk.gov.ida.saml.security.validators.signature.SamlResponseSignatureValidator;

public class EidasProxyNodeApplication extends Application<EidasProxyNodeConfiguration> {

    private Metadata connectorMetadata;
    private String connectorNodeUrl;

    private MetadataResolverBundle<EidasProxyNodeConfiguration> hubMetadataResolverBundle;
    private MetadataResolverBundle<EidasProxyNodeConfiguration> connectorMetadataResolverBundle;
    private SamlResponseSignatureValidator hubResponseSignatureValidator;

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

        hubResponseSignatureValidator = createSignatureValidator(hubMetadataResolverBundle);
        connectorNodeUrl = configuration.getConnectorNodeUrl().toString();
        connectorMetadata = createMetadata(connectorMetadataResolverBundle);

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

    private SamlResponseSignatureValidator createSignatureValidator(MetadataResolverBundle hubMetadataResolverBundle) throws ComponentInitializationException {
        MetadataCredentialResolver hubMetadataCredentialResolver = new MetadataCredentialResolverInitializer(hubMetadataResolverBundle.getMetadataResolver()).initialize();
        KeyInfoCredentialResolver keyInfoCredentialResolver = DefaultSecurityConfigurationBootstrap.buildBasicInlineKeyInfoCredentialResolver();
        ExplicitKeySignatureTrustEngine explicitKeySignatureTrustEngine = new ExplicitKeySignatureTrustEngine(hubMetadataCredentialResolver, keyInfoCredentialResolver);
        MetadataBackedSignatureValidator metadataBackedSignatureValidator = MetadataBackedSignatureValidator.withoutCertificateChainValidation(explicitKeySignatureTrustEngine);
        SamlMessageSignatureValidator samlMessageSignatureValidator = new SamlMessageSignatureValidator(metadataBackedSignatureValidator);
        return new SamlResponseSignatureValidator(samlMessageSignatureValidator);
    }

    private EidasResponseGenerator createEidasResponseGenerator(EidasProxyNodeConfiguration configuration) {
        HubResponseTranslator hubResponseTranslator = new HubResponseTranslator(
                new EidasResponseBuilder(configuration.getConnectorNodeIssuerId()),
                connectorNodeUrl,
                configuration.getProxyNodeMetadataForConnectorNodeUrl().toString()
        );
        SamlObjectSigner signer = new SamlObjectSigner(
                configuration.getConnectorFacingSigningKeyPair().getPublicKey().getPublicKey(),
                configuration.getConnectorFacingSigningKeyPair().getPrivateKey().getPrivateKey(),
                configuration.getConnectorFacingSigningKeyPair().getPublicKey().getCert()
        );
        return new EidasResponseGenerator(hubResponseTranslator, signer);
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

    private EidasAuthnRequestValidator createEidasAuthnRequestValidator() {
        return new EidasAuthnRequestValidator(
                new RequestIssuerValidator(),
                new SpTypeValidator(),
                new LoaValidator(),
                new RequestedAttributesValidator()
        );
    }

    private ResponseAssertionDecrypter createDecrypter(KeyPairConfiguration configuration) {
        return new ResponseAssertionDecrypter(new BasicCredential(
                configuration.getPublicKey().getPublicKey(),
                configuration.getPrivateKey().getPrivateKey())
        );
    }

    private Metadata createMetadata(MetadataResolverBundle bundle) throws ComponentInitializationException {
        MetadataCredentialResolver metadataCredentialResolver = new MetadataCredentialResolverInitializer(bundle.getMetadataResolver()).initialize();
        return new Metadata(metadataCredentialResolver);
    }

    private void registerProviders(Environment environment) {
        environment.jersey().register(AuthnRequestParameterProvider.class);
        environment.jersey().register(ResponseParameterProvider.class);
    }

    private void registerExceptionMappers(Environment environment) {
        environment.jersey().register(new HubResponseExceptionMapper());
        environment.jersey().register(new AuthnRequestExceptionMapper());
    }

    private void registerResources(EidasProxyNodeConfiguration configuration, Environment environment) {
        SamlFormViewBuilder samlFormViewBuilder = new SamlFormViewBuilder();
        EidasResponseGenerator eidasResponseGenerator = createEidasResponseGenerator(configuration);
        HubAuthnRequestGenerator hubAuthnRequestGenerator = createHubAuthnRequestGenerator(configuration);
        ResponseAssertionDecrypter assertionDecrypter = createDecrypter(configuration.getHubFacingEncryptionKeyPair());
        EidasAuthnRequestValidator eidasAuthnRequestValidator = createEidasAuthnRequestValidator();

        environment.jersey().register(new EidasAuthnRequestResource(
                configuration,
                hubAuthnRequestGenerator,
                samlFormViewBuilder,
                eidasAuthnRequestValidator));

        environment.jersey().register(new HubResponseResource(
                eidasResponseGenerator,
                samlFormViewBuilder,
                assertionDecrypter,
                connectorNodeUrl,
                configuration.getConnectorMetadataConfiguration().getExpectedEntityId(),
                connectorMetadata,
                hubResponseSignatureValidator));
    }

    public void registerMetadataHealthCheck(MetadataResolver metadataResolver, MetadataConfiguration connectorMetadataConfiguration, Environment environment, String name) {
        MetadataHealthCheck metadataHealthCheck = new MetadataHealthCheck(
                metadataResolver,
                name,
                connectorMetadataConfiguration.getExpectedEntityId()
        );

        environment.healthChecks().register(metadataHealthCheck.getName(), metadataHealthCheck);
    }
}
