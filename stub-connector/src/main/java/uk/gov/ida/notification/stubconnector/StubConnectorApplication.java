package uk.gov.ida.notification.stubconnector;

import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import org.eclipse.jetty.server.session.SessionHandler;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.security.SecurityException;
import org.opensaml.security.credential.BasicCredential;
import uk.gov.ida.notification.SamlFormViewBuilder;
import uk.gov.ida.notification.VerifySamlInitializer;
import uk.gov.ida.notification.exceptions.mappers.AuthnRequestExceptionMapper;
import uk.gov.ida.notification.exceptions.mappers.HubResponseExceptionMapper;
import uk.gov.ida.notification.healthcheck.ProxyNodeHealthCheck;
import uk.gov.ida.notification.pki.KeyPairConfiguration;
import uk.gov.ida.notification.saml.ResponseAssertionDecrypter;
import uk.gov.ida.notification.saml.SamlObjectSigner;
import uk.gov.ida.notification.saml.converters.AuthnRequestParameterProvider;
import uk.gov.ida.notification.saml.converters.ResponseParameterProvider;
import uk.gov.ida.notification.saml.metadata.Metadata;
import uk.gov.ida.notification.saml.metadata.MetadataFactory;
import uk.gov.ida.notification.stubconnector.resources.MetadataResource;
import uk.gov.ida.notification.stubconnector.resources.ReceiveResponseResource;
import uk.gov.ida.notification.stubconnector.resources.SendAuthnRequestResource;

public class StubConnectorApplication extends Application<uk.gov.ida.notification.stubconnector.StubConnectorConfiguration> {
    private MetadataFactory<StubConnectorConfiguration> proxyNodeMetadataFactory;

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
        proxyNodeMetadataFactory = new MetadataFactory<>(StubConnectorConfiguration::getProxyNodeMetadataConfiguration);
        bootstrap.addBundle(proxyNodeMetadataFactory.getBundle());
    }

    @Override
    public void run(final StubConnectorConfiguration configuration,
                    final Environment environment) {

        ProxyNodeHealthCheck proxyNodeHealthCheck = new ProxyNodeHealthCheck("stub-connector");
        environment.healthChecks().register(proxyNodeHealthCheck.getName(), proxyNodeHealthCheck);
        environment.healthChecks().register("proxy-node-metadata", proxyNodeMetadataFactory.buildHealthCheck(configuration));

        registerProviders(environment);
        registerExceptionMappers(environment);
        registerResources(configuration, environment);
    }

    private void registerProviders(Environment environment) {
        environment.jersey().register(AuthnRequestParameterProvider.class);
        environment.jersey().register(ResponseParameterProvider.class);

        SessionHandler sessionHandler = new SessionHandler();
        sessionHandler.setSessionCookie("stub-connector-session");
        environment.servlets().setSessionHandler(sessionHandler);
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
                proxyNodeMetadataFactory.getMetadata(),
                authnRequestGenerator,
                samlFormViewBuilder));

        try {
            environment.jersey().register(new MetadataResource(
                    configuration,
                    signer));
        } catch (MarshallingException | SecurityException e) {
            e.printStackTrace();
        }

        environment.jersey().register(
                new ReceiveResponseResource(
                        configuration,
                        createDecrypter(configuration.getEncryptionKeyPair()),
                        proxyNodeMetadataFactory.getBundle()
                )
        );
    }

    private ResponseAssertionDecrypter createDecrypter(KeyPairConfiguration configuration) {
        BasicCredential decryptionCredential = new BasicCredential(
            configuration.getPublicKey().getPublicKey(),
            configuration.getPrivateKey().getPrivateKey()
        );
        return new ResponseAssertionDecrypter(decryptionCredential);
    }

    private EidasAuthnRequestGenerator createEidasAuthnRequestGenerator(SamlObjectSigner signer) {
        return new EidasAuthnRequestGenerator(signer);
    }
}
