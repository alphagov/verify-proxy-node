package uk.gov.ida.notification;

import io.dropwizard.Application;
import io.dropwizard.client.JerseyClientBuilder;
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
import uk.gov.ida.notification.exceptions.mappers.AuthnRequestExceptionMapper;
import uk.gov.ida.notification.exceptions.mappers.HubResponseExceptionMapper;
import uk.gov.ida.notification.pki.CredentialBuilder;
import uk.gov.ida.notification.pki.DecryptionCredential;
import uk.gov.ida.notification.pki.KeyPairConfiguration;
import uk.gov.ida.notification.pki.SigningCredential;
import uk.gov.ida.notification.resources.EidasAuthnRequestResource;
import uk.gov.ida.notification.resources.HubResponseResource;
import uk.gov.ida.notification.saml.ResponseAssertionDecrypter;
import uk.gov.ida.notification.saml.SamlObjectSigner;
import uk.gov.ida.notification.saml.converters.AuthnRequestParameterProvider;
import uk.gov.ida.notification.saml.converters.ResponseParameterProvider;
import uk.gov.ida.notification.saml.metadata.JerseyClientMetadataResolverInitializer;
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
import uk.gov.ida.saml.metadata.MetadataHealthCheck;
import uk.gov.ida.saml.metadata.bundle.MetadataResolverBundle;

import javax.ws.rs.client.Client;
import java.net.URI;

public class EidasProxyNodeApplication extends Application<EidasProxyNodeConfiguration> {
    private static final String BEGIN_CERT = "-----BEGIN CERTIFICATE-----";
    private static final String END_CERT = "-----END CERTIFICATE-----";
    private static final String CONNECTOR_NODE_METADATA_RESOLVER_ID = "connector-node-metadata";

    private Metadata connectorMetadata;
    private Metadata hubMetadata;
    private String connectorNodeUrl;

    private MetadataResolverBundle<EidasProxyNodeConfiguration> hubMetadataResolverBundle;

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
    }

    @Override
    public void run(final EidasProxyNodeConfiguration configuration,
                    final Environment environment) throws
            ComponentInitializationException {
        connectorNodeUrl = configuration.getConnectorNodeUrl().toString();
        connectorMetadata = createConnectorNodeMetadata(configuration, environment);
        hubMetadata = createMetadata(hubMetadataResolverBundle);

        MetadataHealthCheck hubMetadataHealthCheck = new MetadataHealthCheck(
                hubMetadataResolverBundle.getMetadataResolver(),
                configuration.getHubMetadataConfiguration().getExpectedEntityId()
        );

        environment.healthChecks().register(hubMetadataHealthCheck.getName(), hubMetadataHealthCheck);

        registerProviders(environment);
        registerExceptionMappers(environment);
        registerResources(configuration, environment);
    }


    private EidasResponseGenerator createEidasResponseGenerator(EidasProxyNodeConfiguration configuration) {
        HubResponseTranslator hubResponseTranslator = new HubResponseTranslator(
                new EidasResponseBuilder(configuration.getConnectorNodeIssuerId()),
                connectorNodeUrl,
                configuration.getProxyNodeMetadataForConnectorNodeUrl().toString());
        SamlObjectSigner signer = new SamlObjectSigner(createSigningCredential(configuration.getConnectorFacingSigningKeyPair()));
        return new EidasResponseGenerator(hubResponseTranslator, signer);
    }

    private HubAuthnRequestGenerator createHubAuthnRequestGenerator(EidasProxyNodeConfiguration configuration) {
        EidasAuthnRequestTranslator eidasAuthnRequestTranslator = new EidasAuthnRequestTranslator(
                configuration.getProxyNodeEntityId(),
                configuration.getHubUrl().toString());
        SamlObjectSigner signer = new SamlObjectSigner(createSigningCredential(configuration.getHubFacingSigningKeyPair()));
        return new HubAuthnRequestGenerator(eidasAuthnRequestTranslator, signer);
    }

    private SigningCredential createSigningCredential(KeyPairConfiguration configuration) {
        String certString = configuration
                .getPublicKey()
                .getCert()
                .replaceAll(BEGIN_CERT, "")
                .replaceAll(END_CERT, "");
        return CredentialBuilder
                .withKeyPairConfiguration(configuration)
                .buildSigningCredential(certString);
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
                configuration.getConnectorNodeEntityId(),
                connectorMetadata,
                hubMetadata));
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
        DecryptionCredential hubFacingDecryptingCredential = CredentialBuilder
                .withKeyPairConfiguration(configuration)
                .buildDecryptionCredential();
        
        return new ResponseAssertionDecrypter(hubFacingDecryptingCredential);
    }

    private Metadata createConnectorNodeMetadata(EidasProxyNodeConfiguration configuration, Environment environment) throws ComponentInitializationException {
        URI connectorNodeMetadataUrl = configuration.getConnectorNodeMetadataUrl();
        Client client = new JerseyClientBuilder(environment).using(configuration.getHttpClientConfiguration()).build(this.getName());
        MetadataResolver metadataResolver = new JerseyClientMetadataResolverInitializer(CONNECTOR_NODE_METADATA_RESOLVER_ID, client, connectorNodeMetadataUrl).initialize();
        MetadataCredentialResolver metadataCredentialResolver = new MetadataCredentialResolverInitializer(metadataResolver).initialize();
        return new Metadata(metadataCredentialResolver);
    }

    private Metadata createMetadata(MetadataResolverBundle bundle) throws ComponentInitializationException {
        MetadataCredentialResolver metadataCredentialResolver = new MetadataCredentialResolverInitializer(bundle.getMetadataResolver()).initialize();
        return new Metadata(metadataCredentialResolver);
    }
}
