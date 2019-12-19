package uk.gov.ida.eidas.metatron;

import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import uk.gov.ida.dropwizard.logstash.LogstashBundle;
import uk.gov.ida.eidas.metatron.core.dto.ConfigLoaderUtil;
import uk.gov.ida.eidas.metatron.core.dto.KeyStoreModule;
import uk.gov.ida.eidas.metatron.health.MetatronHealthCheck;
import uk.gov.ida.eidas.metatron.resources.MetatronResource;

import java.io.IOException;

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
        return "Metatron - service in disguise";
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
                    final Environment environment) throws IOException {
        final MetatronHealthCheck healthCheck = new MetatronHealthCheck();
        environment.healthChecks().register("Metatron", healthCheck);

        String config = System.getenv("COUNTRIES_CONFIG_FILE");

        environment.jersey().register(new MetatronResource(ConfigLoaderUtil.loadConfig(config)));
    }

}
