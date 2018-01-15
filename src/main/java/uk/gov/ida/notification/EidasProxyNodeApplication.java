package uk.gov.ida.notification;

import io.dropwizard.Application;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.xml.BasicParserPool;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.saml.security.impl.MetadataCredentialResolver;
import uk.gov.ida.notification.pki.CredentialBuilder;
import uk.gov.ida.notification.pki.DecryptionCredential;
import uk.gov.ida.notification.pki.SigningCredential;
import uk.gov.ida.notification.resources.ConnectorNodeMetadataResource;
import uk.gov.ida.notification.resources.EidasAuthnRequestResource;
import uk.gov.ida.notification.resources.HubMetadataResource;
import uk.gov.ida.notification.resources.HubResponseResource;
import uk.gov.ida.notification.saml.ResponseAssertionDecrypter;
import uk.gov.ida.notification.saml.SamlObjectSigner;
import uk.gov.ida.notification.saml.converters.AuthnRequestParameterProvider;
import uk.gov.ida.notification.saml.converters.ResponseParameterProvider;
import uk.gov.ida.notification.saml.metadata.ConnectorNodeMetadata;
import uk.gov.ida.notification.saml.metadata.JerseyClientMetadataResolver;
import uk.gov.ida.notification.saml.metadata.MetadataCredentialResolverBuilder;
import uk.gov.ida.notification.saml.translation.EidasAuthnRequestTranslator;
import uk.gov.ida.notification.saml.translation.HubResponseTranslator;
import uk.gov.ida.stubs.resources.StubConnectorNodeResource;

import javax.ws.rs.client.Client;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Timer;

public class EidasProxyNodeApplication extends Application<EidasProxyNodeConfiguration> {

    private final String CONNECTOR_NODE_METADATA_RESOLVER_ID = "connector-node-metadata";
    private EidasProxyNodeConfiguration configuration;
    private Environment environment;
    private HubResponseTranslator hubResponseTranslator;
    private HubAuthnRequestGenerator hubAuthnRequestGenerator;
    private SamlFormViewBuilder samlFormViewBuilder;
    private ResponseAssertionDecrypter assertionDecrypter;
    private ConnectorNodeMetadata connectorNodeMetadata;
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
            ComponentInitializationException,
            URISyntaxException {
        this.configuration = configuration;
        this.environment = environment;

        connectorNodeUrl = configuration.getConnectorNodeUrl().toString();
        connectorNodeMetadata = createConnectorNodeMetadata();

        hubResponseTranslator = new HubResponseTranslator(
                configuration.getProxyNodeEntityId(),
                connectorNodeUrl
        );
        hubAuthnRequestGenerator = createHubAuthnRequestGenerator();
        samlFormViewBuilder = new SamlFormViewBuilder();
        assertionDecrypter = createDecrypter();

        registerProviders();
        registerResources();
    }

    private void registerProviders() {
        environment.jersey().register(AuthnRequestParameterProvider.class);
        environment.jersey().register(ResponseParameterProvider.class);
    }

    private void registerResources() {
        environment.jersey().register(new EidasAuthnRequestResource(
                configuration,
                hubAuthnRequestGenerator,
                samlFormViewBuilder
        ));
        environment.jersey().register(new HubResponseResource(
                hubResponseTranslator,
                samlFormViewBuilder,
                assertionDecrypter,
                connectorNodeUrl,
                connectorNodeMetadata));
        environment.jersey().register(new HubMetadataResource());
        environment.jersey().register(new ConnectorNodeMetadataResource());
        environment.jersey().register(new StubConnectorNodeResource());
    }

    private HubAuthnRequestGenerator createHubAuthnRequestGenerator() {
        EidasAuthnRequestTranslator eidasAuthnRequestTranslator = new EidasAuthnRequestTranslator(
                configuration.getProxyNodeEntityId(),
                configuration.getHubUrl().toString());
        SigningCredential hubFacingSigningCredential = CredentialBuilder
                .withKeyPairConfiguration(configuration.getHubFacingSigningKeyPair())
                .buildSigningCredential();
        SamlObjectSigner hubAuthnRequestSigner = new SamlObjectSigner(hubFacingSigningCredential);
        return new HubAuthnRequestGenerator(
                eidasAuthnRequestTranslator,
                hubAuthnRequestSigner);
    }

    private ResponseAssertionDecrypter createDecrypter() {
        DecryptionCredential hubFacingDecryptingCredential = CredentialBuilder
                .withKeyPairConfiguration(configuration.getHubFacingEncryptionKeyPair())
                .buildDecryptionCredential();
        
        return new ResponseAssertionDecrypter(hubFacingDecryptingCredential);
    }

    private ConnectorNodeMetadata createConnectorNodeMetadata() throws URISyntaxException, ComponentInitializationException, InitializationException {
        String connectorNodeEntityId = configuration.getConnectorNodeEntityId();

        JerseyClientMetadataResolver metadataResolver = initialiseJerseyClientMetadataResolver();
        MetadataCredentialResolver metadataCredentialResolver = new MetadataCredentialResolverBuilder(metadataResolver).build();

        return new ConnectorNodeMetadata(metadataCredentialResolver, connectorNodeEntityId);
    }

    private JerseyClientMetadataResolver initialiseJerseyClientMetadataResolver() throws InitializationException, ComponentInitializationException {
        URI connectorNodeMetadataUrl = configuration.getConnectorNodeMetadataUrl();

        Client client = new JerseyClientBuilder(environment).using(configuration.getHttpClientConfiguration()).build(this.getName());

        JerseyClientMetadataResolver metadataResolver = new JerseyClientMetadataResolver(new Timer(), client, connectorNodeMetadataUrl);
        BasicParserPool parserPool = new BasicParserPool();
        parserPool.initialize();
        metadataResolver.setParserPool(parserPool);
        metadataResolver.setRequireValidMetadata(true);
        metadataResolver.setId(CONNECTOR_NODE_METADATA_RESOLVER_ID);
        metadataResolver.initialize();

        return metadataResolver;
    }
}
