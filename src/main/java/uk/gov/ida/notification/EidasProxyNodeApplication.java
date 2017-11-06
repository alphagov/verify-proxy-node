package uk.gov.ida.notification;

import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import uk.gov.ida.notification.resources.StubHubResource;
import uk.gov.ida.notification.resources.VerifyResource;

public class EidasProxyNodeApplication extends Application<EidasProxyNodeConfiguration> {

    public static void main(final String[] args) throws Exception {
        if (args == null || args.length == 0) {
            new EidasProxyNodeApplication().run("server", System.getenv("CONFIG_FILE"));
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
        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(bootstrap.getConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor(false)
                )
        );
    }

    @Override
    public void run(final EidasProxyNodeConfiguration configuration,
                    final Environment environment) {
        environment.jersey().register(new VerifyResource());
        environment.jersey().register(new StubHubResource());
    }

}
