package uk.gov.ida.eidas.metadataservice;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class MetadataServiceApplication extends Application<MetadataServiceConfiguration> {

    public static void main(final String[] args) throws Exception {
        new MetadataServiceApplication().run(args);
    }

    @Override
    public String getName() {
        return "MetadataService";
    }

    @Override
    public void initialize(final Bootstrap<MetadataServiceConfiguration> bootstrap) {
        // TODO: application initialization
    }

    @Override
    public void run(final MetadataServiceConfiguration configuration,
                    final Environment environment) {
        // TODO: implement application
    }

}
