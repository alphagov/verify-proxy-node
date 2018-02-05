package uk.gov.ida.notification;

import io.dropwizard.Application;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
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

import javax.ws.rs.client.Client;
import java.net.URI;

public class EidasProxyNodeApplication extends Application<EidasProxyNodeConfiguration> {
    private static final String BEGIN_CERT = "-----BEGIN CERTIFICATE-----";
    private static final String END_CERT = "-----END CERTIFICATE-----";

    private static final String CONNECTOR_NODE_METADATA_RESOLVER_ID = "connector-node-metadata";
    private static final String HUB_METADATA_RESOLVER_ID = "hub-metadata";
    private EidasProxyNodeConfiguration configuration;
    private Environment environment;
    private Metadata connectorMetadata;
    private Metadata hubMetadata;
    private String connectorNodeUrl;

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
    }

    @Override
    public void run(final EidasProxyNodeConfiguration configuration,
                    final Environment environment) throws
            InitializationException,
            ComponentInitializationException {
        this.configuration = configuration;
        this.environment = environment;

        connectorNodeUrl = configuration.getConnectorNodeUrl().toString();
        JerseyClientConfiguration clientConfiguration = configuration.getHttpClientConfiguration();
        Client client = new JerseyClientBuilder(environment).using(clientConfiguration).build(this.getName());
        connectorMetadata = createConnectorNodeMetadata(client);
        hubMetadata = createHubMetadata(client);

        registerProviders();
        registerResources();
    }


    private EidasResponseGenerator createEidasResponseGenerator(SamlObjectSigner signer) {
        HubResponseTranslator hubResponseTranslator = new HubResponseTranslator(
                new EidasResponseBuilder(configuration.getConnectorNodeIssuerId()),
                connectorNodeUrl,
                configuration.getProxyNodeMetadataForConnectorNodeUrl().toString());
        return new EidasResponseGenerator(hubResponseTranslator, signer);
    }

    private HubAuthnRequestGenerator createHubAuthnRequestGenerator(SamlObjectSigner signer) {
        EidasAuthnRequestTranslator eidasAuthnRequestTranslator = new EidasAuthnRequestTranslator(
                configuration.getProxyNodeEntityId(),
                configuration.getHubUrl().toString());
        return new HubAuthnRequestGenerator(eidasAuthnRequestTranslator, signer);
    }

    private SigningCredential createSigningCredential() {
        KeyPairConfiguration signingKeyPair = configuration.getSigningKeyPair();
        String certString = signingKeyPair
                .getPublicKey()
                .getCert()
                .replaceAll(BEGIN_CERT, "")
                .replaceAll(END_CERT, "");
        return CredentialBuilder
                .withKeyPairConfiguration(signingKeyPair)
                .buildSigningCredential(certString);
    }

    private void registerProviders() {
        environment.jersey().register(AuthnRequestParameterProvider.class);
        environment.jersey().register(ResponseParameterProvider.class);
    }

    private void registerResources() {
        SamlFormViewBuilder samlFormViewBuilder = new SamlFormViewBuilder();
        SamlObjectSigner signer = new SamlObjectSigner(createSigningCredential());
        EidasResponseGenerator eidasResponseGenerator = createEidasResponseGenerator(signer);
        HubAuthnRequestGenerator hubAuthnRequestGenerator = createHubAuthnRequestGenerator(signer);
        ResponseAssertionDecrypter assertionDecrypter = createDecrypter();

        environment.jersey().register(new EidasAuthnRequestResource(
                configuration,
                hubAuthnRequestGenerator,
                samlFormViewBuilder
        ));

        environment.jersey().register(new HubResponseResource(
                eidasResponseGenerator,
                samlFormViewBuilder,
                assertionDecrypter,
                connectorNodeUrl,
                configuration.getConnectorNodeEntityId(),
                connectorMetadata,
                hubMetadata));
    }

    private ResponseAssertionDecrypter createDecrypter() {
        DecryptionCredential hubFacingDecryptingCredential = CredentialBuilder
                .withKeyPairConfiguration(configuration.getHubFacingEncryptionKeyPair())
                .buildDecryptionCredential();
        
        return new ResponseAssertionDecrypter(hubFacingDecryptingCredential);
    }

    private Metadata createHubMetadata(Client client) throws ComponentInitializationException, InitializationException {
        URI hubMetadataUrl = configuration.getHubMetadataUrl();

        MetadataResolver metadataResolver = new JerseyClientMetadataResolverInitializer(HUB_METADATA_RESOLVER_ID, client, hubMetadataUrl).initialize();
        MetadataCredentialResolver metadataCredentialResolver = new MetadataCredentialResolverInitializer(metadataResolver).initialize();

        return new Metadata(metadataCredentialResolver);
    }

    private Metadata createConnectorNodeMetadata(Client client) throws ComponentInitializationException {
        URI connectorNodeMetadataUrl = configuration.getConnectorNodeMetadataUrl();

        MetadataResolver metadataResolver = new JerseyClientMetadataResolverInitializer(CONNECTOR_NODE_METADATA_RESOLVER_ID, client, connectorNodeMetadataUrl).initialize();
        MetadataCredentialResolver metadataCredentialResolver = new MetadataCredentialResolverInitializer(metadataResolver).initialize();
        return new Metadata(metadataCredentialResolver);
    }
}
