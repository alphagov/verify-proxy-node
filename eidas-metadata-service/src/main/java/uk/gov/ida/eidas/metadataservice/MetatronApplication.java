package uk.gov.ida.eidas.metadataservice;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class MetatronApplication extends Application<MetatronConfiguration> {

    public static void main(final String[] args) throws Exception {
        new MetatronApplication().run(args);
    }

    @Override
    public String getName() {
        return "Metatron - service in disguise";
    }

    @Override
    public void initialize(final Bootstrap<MetatronConfiguration> bootstrap) {
        // TODO: application initialization
    }

    @Override
    public void run(final MetatronConfiguration configuration,
                    final Environment environment) {
        // TODO: implement application
    }

}
