package uk.gov.ida;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import uk.gov.ida.resources.VerifyResource;

public class EidasProxyNodeApplication extends Application<EidasProxyNodeConfiguration> {

    public static void main(final String[] args) throws Exception {
        new EidasProxyNodeApplication().run(args);
    }

    @Override
    public String getName() {
        return "EidasProxyNode";
    }

    @Override
    public void initialize(final Bootstrap<EidasProxyNodeConfiguration> bootstrap) {
        // TODO: application initialization
    }

    @Override
    public void run(final EidasProxyNodeConfiguration configuration,
                    final Environment environment) {
        final VerifyResource resource = new VerifyResource();
        environment.jersey().register(resource);
    }

}
