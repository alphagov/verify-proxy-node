package uk.gov.ida.notification.stubconnector;

import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import org.eclipse.jetty.server.session.SessionHandler;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import uk.gov.ida.dropwizard.logstash.LogstashBundle;
import uk.gov.ida.notification.VerifySamlInitializer;
import uk.gov.ida.notification.exceptions.mappers.CatchAllExceptionMapper;
import uk.gov.ida.notification.exceptions.mappers.MissingMetadataExceptionMapper;
import uk.gov.ida.notification.healthcheck.ProxyNodeHealthCheck;
import uk.gov.ida.notification.saml.ResponseAssertionDecrypter;
import uk.gov.ida.notification.saml.converters.AuthnRequestParameterProvider;
import uk.gov.ida.notification.saml.converters.ResponseParameterProvider;
import uk.gov.ida.notification.saml.metadata.Metadata;
import uk.gov.ida.notification.shared.istio.IstioHeaderMapperFilter;
import uk.gov.ida.notification.shared.istio.IstioHeaderStorage;
import uk.gov.ida.notification.shared.logging.ProxyNodeLoggingFilter;
import uk.gov.ida.notification.shared.metadata.MetadataPublishingBundle;
import uk.gov.ida.notification.stubconnector.resources.ReceiveResponseResource;
import uk.gov.ida.notification.stubconnector.resources.SendAuthnRequestResource;
import uk.gov.ida.saml.metadata.MetadataConfiguration;
import uk.gov.ida.saml.metadata.MetadataHealthCheck;
import uk.gov.ida.saml.metadata.bundle.MetadataResolverBundle;

import java.util.Optional;

public class StubConnectorApplication extends Application<StubConnectorConfiguration> {
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
        } catch (InitializationException e) {
            throw new RuntimeException(e);
        }

        VerifySamlInitializer.init();

        bootstrap.addBundle(new ViewBundle<>());
        bootstrap.addBundle(new LogstashBundle());
        bootstrap.addBundle(new AssetsBundle("/assets/favicon.ico", "/favicon.ico"));
        bootstrap.addBundle(new MetadataPublishingBundle<>(StubConnectorConfiguration::getMetadataPublishingConfiguration));

        proxyNodeMetadataResolverBundle = new MetadataResolverBundle<>(configuration -> Optional.of(configuration.getProxyNodeMetadataConfiguration()));
        bootstrap.addBundle(proxyNodeMetadataResolverBundle);
    }

    @Override
    public void run(final StubConnectorConfiguration configuration, final Environment environment) {
        proxyNodeMetadata = new Metadata(proxyNodeMetadataResolverBundle.getMetadataCredentialResolver());

        ProxyNodeHealthCheck proxyNodeHealthCheck = new ProxyNodeHealthCheck("stub-connector");
        environment.healthChecks().register(proxyNodeHealthCheck.getName(), proxyNodeHealthCheck);

        registerMetadataHealthCheck(
                proxyNodeMetadataResolverBundle.getMetadataResolver(),
                configuration.getProxyNodeMetadataConfiguration(),
                environment,
                "proxy-node-metadata");

        environment.jersey().register(new MissingMetadataExceptionMapper());
        environment.jersey().register(new CatchAllExceptionMapper());

        registerProviders(environment);
        registerInjections(environment);
        registerResources(configuration, environment);
    }

    private void registerProviders(Environment environment) {
        environment.jersey().register(IstioHeaderMapperFilter.class);
        environment.jersey().register(ProxyNodeLoggingFilter.class);
        environment.jersey().register(AuthnRequestParameterProvider.class);
        environment.jersey().register(ResponseParameterProvider.class);

        SessionHandler sessionHandler = new SessionHandler();
        sessionHandler.setSessionCookie("stub-connector-session");
        environment.servlets().setSessionHandler(sessionHandler);
    }

    private void registerResources(StubConnectorConfiguration configuration, Environment environment) {
        ResponseAssertionDecrypter decrypter = new ResponseAssertionDecrypter(configuration.getCredentialConfiguration().getCredential());

        environment.jersey().register(new SendAuthnRequestResource(configuration, proxyNodeMetadata));
        environment.jersey().register(new ReceiveResponseResource(configuration, decrypter, proxyNodeMetadataResolverBundle));
    }

    private void registerMetadataHealthCheck(MetadataResolver metadataResolver, MetadataConfiguration connectorMetadataConfiguration, Environment environment, String name) {
        MetadataHealthCheck metadataHealthCheck = new MetadataHealthCheck(
                metadataResolver, name, connectorMetadataConfiguration.getExpectedEntityId()
        );

        environment.healthChecks().register(metadataHealthCheck.getName(), metadataHealthCheck);
    }

    private void registerInjections(Environment environment) {
        environment.jersey().register(new AbstractBinder() {
            @Override
            protected void configure() {
                bindAsContract(IstioHeaderStorage.class);
            }
        });
    }
}
