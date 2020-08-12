package uk.gov.ida.eidas.metatron;

import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import uk.gov.ida.dropwizard.logstash.LogstashBundle;
import uk.gov.ida.eidas.metatron.domain.ConfigLoaderUtil;
import uk.gov.ida.eidas.metatron.domain.EidasConfig;
import uk.gov.ida.eidas.metatron.domain.KeyStoreModule;
import uk.gov.ida.eidas.metatron.domain.MetadataResolverService;
import uk.gov.ida.eidas.metatron.exceptions.MetatronClientExceptionMapper;
import uk.gov.ida.eidas.metatron.exceptions.MetatronServerExceptionMapper;
import uk.gov.ida.eidas.metatron.health.CountryMetadataHealthMetrics;
import uk.gov.ida.eidas.metatron.health.MetatronHealthCheck;
import uk.gov.ida.eidas.metatron.resources.MetatronResource;
import uk.gov.ida.notification.shared.istio.IstioHeaderMapperFilter;
import uk.gov.ida.notification.shared.istio.IstioHeaderStorage;
import uk.gov.ida.notification.shared.logging.ProxyNodeLoggingFilter;
import uk.gov.ida.saml.metadata.factories.CredentialResolverFactory;
import uk.gov.ida.saml.metadata.factories.MetadataResolverFactory;

import java.io.IOException;
import java.security.Security;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MetatronApplication extends Application<MetatronConfiguration> {

    public static void main(final String[] args) throws Exception {
        if (args == null || args.length == 0) {
            String configFile = System.getenv("CONFIG_FILE");

            if (configFile == null) {
                throw new RuntimeException("CONFIG_FILE environment variable should be set with path to configuration file");
            }

            new MetatronApplication().run("server", configFile);
        } else {
            new MetatronApplication().run(args);
        }
    }

    @Override
    public String getName() {
        return "Metatron - Provides all of your CountryMetadataResponse needs. Does not provide MetatronResponses";
    }

    @Override
    public void initialize(final Bootstrap<MetatronConfiguration> bootstrap) {
        // Needed to correctly interpolate environment variables in config file
        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(bootstrap.getConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor(false)
                )
        );

        bootstrap.addBundle(new LogstashBundle());
        bootstrap.getObjectMapper().registerModule(new KeyStoreModule());
    }

    @Override
    public void run(final MetatronConfiguration configuration,
                    final Environment environment) throws IOException, InitializationException {

        Security.addProvider(new BouncyCastleProvider());

        InitializationService.initialize();

        EidasConfig countriesConfig = ConfigLoaderUtil.loadConfig(configuration.getCountriesConfig());
        MetadataResolverFactory metadataResolverFactory = new MetadataResolverFactory();
        CredentialResolverFactory credentialResolverFactory = new CredentialResolverFactory();
        MetadataResolverService resolverService = new MetadataResolverService(countriesConfig, metadataResolverFactory, credentialResolverFactory);

        final MetatronHealthCheck healthCheck = new MetatronHealthCheck();
        environment.healthChecks().register("Metatron", healthCheck);
        scheduleMetadataHealthChecker(environment, resolverService);

        environment.jersey().register(new MetatronResource(resolverService));
        environment.jersey().register(MetatronClientExceptionMapper.class);
        environment.jersey().register(MetatronServerExceptionMapper.class);

        environment.jersey().register(IstioHeaderMapperFilter.class);
        environment.jersey().register(ProxyNodeLoggingFilter.class);
        registerInjections(environment);


    }

    private void scheduleMetadataHealthChecker(Environment environment, MetadataResolverService resolverService) {
        ScheduledExecutorService scheduledExecutorService = environment
                .lifecycle()
                .scheduledExecutorService("CountryMetadataHealthMetrics")
                .build();
        Runnable metrics = new CountryMetadataHealthMetrics(resolverService);
        scheduledExecutorService.scheduleWithFixedDelay(metrics, 2, 5, TimeUnit.MINUTES);
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
