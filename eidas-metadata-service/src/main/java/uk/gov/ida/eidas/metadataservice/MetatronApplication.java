package uk.gov.ida.eidas.metadataservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import uk.gov.ida.eidas.metadataservice.core.dto.ConfigLoader;
import uk.gov.ida.eidas.metadataservice.core.dto.KeyStoreModule;
import uk.gov.ida.eidas.metadataservice.health.MetatronHealthCheck;
import uk.gov.ida.eidas.metadataservice.resources.MetatronResource;

import java.io.IOException;

public class MetatronApplication extends Application<MetatronConfiguration> {

    public static void main(final String[] args) throws Exception {
        String config;
        if ( args.length != 2) {
            config = System.getenv("CONFIG_FILE");
        } else {
            config = args[1];
        }
        new MetatronApplication().run("server", config);
    }

    @Override
    public String getName() {
        return "Metatron - service in disguise";
    }

    @Override
    public void initialize(final Bootstrap<MetatronConfiguration> bootstrap) {
        bootstrap.getObjectMapper().registerModule(new KeyStoreModule());
    }

    @Override
    public void run(final MetatronConfiguration configuration,
                    final Environment environment) throws IOException {
        final MetatronHealthCheck healthCheck = new MetatronHealthCheck();
        environment.healthChecks().register("Metatron", healthCheck);

        String config = System.getenv("COUNTRIES_CONFIG_FILE");

        environment.jersey().register(new MetatronResource(ConfigLoader.loadConfig(config)));
    }

}
