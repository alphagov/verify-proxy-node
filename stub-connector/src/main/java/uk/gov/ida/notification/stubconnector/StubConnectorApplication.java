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
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.security.SecurityException;
import org.opensaml.security.credential.BasicCredential;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.security.x509.X509Credential;
import org.opensaml.xmlsec.signature.support.SignatureException;
import se.litsec.opensaml.utils.X509CertificateUtils;
import uk.gov.ida.bundles.LoggingBundle;
import uk.gov.ida.notification.VerifySamlInitializer;
import uk.gov.ida.notification.exceptions.mappers.AuthnRequestExceptionMapper;
import uk.gov.ida.notification.exceptions.mappers.HubResponseExceptionMapper;
import uk.gov.ida.notification.healthcheck.ProxyNodeHealthCheck;
import uk.gov.ida.notification.pki.KeyPairConfiguration;
import uk.gov.ida.notification.saml.ResponseAssertionDecrypter;
import uk.gov.ida.notification.saml.converters.AuthnRequestParameterProvider;
import uk.gov.ida.notification.saml.converters.ResponseParameterProvider;
import uk.gov.ida.notification.saml.metadata.Metadata;
import uk.gov.ida.notification.stubconnector.resources.MetadataResource;
import uk.gov.ida.notification.stubconnector.resources.ReceiveResponseResource;
import uk.gov.ida.notification.stubconnector.resources.SendAuthnRequestResource;
import uk.gov.ida.saml.metadata.MetadataConfiguration;
import uk.gov.ida.saml.metadata.MetadataHealthCheck;
import uk.gov.ida.saml.metadata.bundle.MetadataResolverBundle;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

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

        VerifySamlInitializer.init();

        bootstrap.addBundle(new ViewBundle<>());
        bootstrap.addBundle(new LoggingBundle());


        // Metadata
        proxyNodeMetadataResolverBundle = new MetadataResolverBundle<>(StubConnectorConfiguration::getProxyNodeMetadataConfiguration);
        bootstrap.addBundle(proxyNodeMetadataResolverBundle);
    }

    @Override
    public void run(final StubConnectorConfiguration configuration,
                    final Environment environment) throws Exception {

        proxyNodeMetadata = new Metadata(proxyNodeMetadataResolverBundle.getMetadataCredentialResolver());

        ProxyNodeHealthCheck proxyNodeHealthCheck = new ProxyNodeHealthCheck("stub-connector");
        environment.healthChecks().register(proxyNodeHealthCheck.getName(), proxyNodeHealthCheck);

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

    private void registerResources(StubConnectorConfiguration configuration, Environment environment) throws CertificateException, MarshallingException, SecurityException, SignatureException {
        PrivateKey signingKey = configuration.getSigningKeyPair().getPrivateKey().getPrivateKey();
        String signingCertString = configuration.getSigningKeyPair().getPublicKey().getCert();
        X509Certificate signingCert = X509CertificateUtils.decodeCertificate(new ByteArrayInputStream(signingCertString.getBytes(StandardCharsets.UTF_8)));
        X509Credential signingCredential = new BasicX509Credential(signingCert, signingKey);

        environment.jersey().register(new SendAuthnRequestResource(
            configuration,
            proxyNodeMetadata,
            signingCredential));

        environment.jersey().register(new MetadataResource(configuration, signingCredential));

        environment.jersey().register(
                new ReceiveResponseResource(
                        configuration,
                        createDecrypter(configuration.getEncryptionKeyPair()),
                        proxyNodeMetadataResolverBundle
                )
        );
    }

    private void registerMetadataHealthCheck(MetadataResolver metadataResolver, MetadataConfiguration connectorMetadataConfiguration, Environment environment, String name) {
        MetadataHealthCheck metadataHealthCheck = new MetadataHealthCheck(
                metadataResolver,
                name,
                connectorMetadataConfiguration.getExpectedEntityId()
        );

        environment.healthChecks().register(metadataHealthCheck.getName(), metadataHealthCheck);
    }

    private ResponseAssertionDecrypter createDecrypter(KeyPairConfiguration configuration) {
        BasicCredential decryptionCredential = new BasicCredential(
            configuration.getPublicKey().getPublicKey(),
            configuration.getPrivateKey().getPrivateKey()
        );
        return new ResponseAssertionDecrypter(decryptionCredential);
    }
}
