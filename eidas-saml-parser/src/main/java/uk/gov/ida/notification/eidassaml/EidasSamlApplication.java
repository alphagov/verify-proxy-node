package uk.gov.ida.notification.eidassaml;

import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import uk.gov.ida.dropwizard.logstash.LogstashBundle;
import uk.gov.ida.notification.VerifySamlInitializer;

public class EidasSamlApplication extends Application<EidasSamlConfiguration> {
    @Override
    public void run(EidasSamlConfiguration configuration, Environment environment) {

        environment.jersey().register(new EidasSamlResource());
    }

    public static void main() throws Exception {
        new EidasSamlApplication().run("server", "config.yml");
    }

    public void initialize(final Bootstrap<EidasSamlConfiguration> bootstrap) {

        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(bootstrap.getConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor(false)
                )
        );

        try {
            InitializationService.initialize();
        } catch (InitializationException e) {
            throw new RuntimeException(e);
        }

        VerifySamlInitializer.init();

        bootstrap.addBundle(new LogstashBundle());
    }
}
