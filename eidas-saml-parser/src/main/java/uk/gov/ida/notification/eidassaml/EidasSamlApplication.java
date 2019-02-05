package uk.gov.ida.notification.eidassaml;

import io.dropwizard.Application;
import io.dropwizard.setup.Environment;
import org.opensaml.core.config.InitializationService;

public class EidasSamlApplication extends Application<EidasSamlConfiguration> {
    @Override
    public void run(EidasSamlConfiguration configuration, Environment environment) throws Exception {
        // Initialize OpenSAML
        InitializationService.initialize();

        environment.jersey().register(new EidasSamlResource());
    }

    public static void main() throws Exception {
        new EidasSamlApplication().run("server", "config.yml");
    }
}
