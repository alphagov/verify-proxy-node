package uk.gov.ida.notification.stubconnector;

import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.security.impl.MetadataCredentialResolver;
import org.opensaml.security.SecurityException;
import org.opensaml.security.credential.BasicCredential;
import org.opensaml.xmlsec.config.DefaultSecurityConfigurationBootstrap;
import org.opensaml.xmlsec.keyinfo.KeyInfoCredentialResolver;
import org.opensaml.xmlsec.signature.support.impl.ExplicitKeySignatureTrustEngine;
import uk.gov.ida.notification.SamlFormViewBuilder;
import uk.gov.ida.notification.VerifySamlInitializer;
import uk.gov.ida.notification.exceptions.mappers.AuthnRequestExceptionMapper;
import uk.gov.ida.notification.exceptions.mappers.HubResponseExceptionMapper;
import uk.gov.ida.notification.pki.KeyPairConfiguration;
import uk.gov.ida.notification.saml.ResponseAssertionDecrypter;
import uk.gov.ida.notification.saml.SamlObjectSigner;
import uk.gov.ida.notification.saml.converters.ResponseParameterProvider;
import uk.gov.ida.notification.saml.metadata.Metadata;
import uk.gov.ida.notification.saml.metadata.MetadataCredentialResolverInitializer;
import uk.gov.ida.notification.stubconnector.resources.MetadataResource;
import uk.gov.ida.notification.stubconnector.resources.ReceiveResponseResource;
import uk.gov.ida.notification.stubconnector.resources.SendAuthnRequestResource;
import uk.gov.ida.saml.metadata.MetadataConfiguration;
import uk.gov.ida.saml.metadata.MetadataHealthCheck;
import uk.gov.ida.saml.metadata.bundle.MetadataResolverBundle;
import uk.gov.ida.saml.security.MetadataBackedSignatureValidator;
import uk.gov.ida.saml.security.SamlMessageSignatureValidator;
import uk.gov.ida.saml.security.validators.signature.SamlRequestSignatureValidator;

public class StubConnectorApplication extends Application<uk.gov.ida.notification.stubconnector.StubConnectorConfiguration> {
    private Metadata proxyNodeMetadata;
    private MetadataResolverBundle<StubConnectorConfiguration> proxyNodeMetadataResolverBundle;

    @SuppressWarnings("WeakerAccess") // Needed for DropwizardAppRules
    public StubConnectorApplication() {
    }

    public static void main(final String[] args) throws Exception {
        if (args == null || args.length == 0) {
            String configFile = System.getenv("CONFIG_FILE");

            if (configFile == null) {
                throw new RuntimeException("CONFIG_FILE environment variable should be set with path to configuration file");
            }

            new StubConnectorApplication().run("server", configFile);
        } else {
            new StubConnectorApplication().run(args);
        }
    }

    @Override
    public String getName() {
        return "StubConnector";
    }

    @Override
    public void initialize(final Bootstrap<StubConnectorConfiguration> bootstrap) {
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
        proxyNodeMetadataResolverBundle = new MetadataResolverBundle<>(StubConnectorConfiguration::getProxyNodeMetadataConfiguration);
        bootstrap.addBundle(proxyNodeMetadataResolverBundle);
    }

    @Override
    public void run(final StubConnectorConfiguration configuration,
                    final Environment environment) throws
            ComponentInitializationException {

        proxyNodeMetadata = createMetadata(proxyNodeMetadataResolverBundle);

        registerMetadataHealthCheck(
                proxyNodeMetadataResolverBundle.getMetadataResolver(),
                configuration.getProxyNodeMetadataConfiguration(),
                environment,
                "proxy-node-metadata");

        registerProviders(environment);
        registerExceptionMappers(environment);
        registerResources(configuration, environment);
    }

    private void registerProviders(Environment environment) {
        environment.jersey().register(ResponseParameterProvider.class);
    }

    private void registerExceptionMappers(Environment environment) {
        environment.jersey().register(new HubResponseExceptionMapper());
        environment.jersey().register(new AuthnRequestExceptionMapper());
    }

    private void registerResources(StubConnectorConfiguration configuration, Environment environment) {
        SamlObjectSigner signer = new SamlObjectSigner(
                configuration.getSigningKeyPair().getPublicKey().getPublicKey(),
                configuration.getSigningKeyPair().getPrivateKey().getPrivateKey(),
                configuration.getSigningKeyPair().getPublicKey().getCert()
        );
        SamlFormViewBuilder samlFormViewBuilder = new SamlFormViewBuilder();
        EidasAuthnRequestGenerator authnRequestGenerator = createEidasAuthnRequestGenerator(signer);

        environment.jersey().register(new SendAuthnRequestResource(
                configuration,
                proxyNodeMetadata,
                authnRequestGenerator,
                samlFormViewBuilder));

        try {
            environment.jersey().register(new MetadataResource(
                    configuration,
                    signer));
        } catch (MarshallingException | SecurityException e) {
            e.printStackTrace();
        }

        environment.jersey().register(new ReceiveResponseResource(createDecrypter(configuration.getEncryptionKeyPair())));
    }

    public void registerMetadataHealthCheck(MetadataResolver metadataResolver, MetadataConfiguration connectorMetadataConfiguration, Environment environment, String name) {
        MetadataHealthCheck metadataHealthCheck = new MetadataHealthCheck(
                metadataResolver,
                name,
                connectorMetadataConfiguration.getExpectedEntityId()
        );

        environment.healthChecks().register(metadataHealthCheck.getName(), metadataHealthCheck);
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

    private SamlMessageSignatureValidator createSamlMessagesSignatureValidator(MetadataResolverBundle hubMetadataResolverBundle) throws ComponentInitializationException {
        MetadataCredentialResolver hubMetadataCredentialResolver = new MetadataCredentialResolverInitializer(hubMetadataResolverBundle.getMetadataResolver()).initialize();
        KeyInfoCredentialResolver keyInfoCredentialResolver = DefaultSecurityConfigurationBootstrap.buildBasicInlineKeyInfoCredentialResolver();
        ExplicitKeySignatureTrustEngine explicitKeySignatureTrustEngine = new ExplicitKeySignatureTrustEngine(hubMetadataCredentialResolver, keyInfoCredentialResolver);
        MetadataBackedSignatureValidator metadataBackedSignatureValidator = MetadataBackedSignatureValidator.withoutCertificateChainValidation(explicitKeySignatureTrustEngine);
        return new SamlMessageSignatureValidator(metadataBackedSignatureValidator);
    }

    private SamlRequestSignatureValidator createSamlResponseSignatureValidator(MetadataResolverBundle hubMetadataResolverBundle) throws ComponentInitializationException {
        SamlMessageSignatureValidator samlMessageSignatureValidator = createSamlMessagesSignatureValidator(hubMetadataResolverBundle);
        return new SamlRequestSignatureValidator(samlMessageSignatureValidator);
    }

    private EidasAuthnRequestGenerator createEidasAuthnRequestGenerator(SamlObjectSigner signer) {
        return new EidasAuthnRequestGenerator(signer);
    }
}
