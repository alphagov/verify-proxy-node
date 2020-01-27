package uk.gov.ida.eidas.metatron.apprule.rules;

import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit.DropwizardAppRule;
import uk.gov.ida.eidas.metatron.MetatronApplication;
import uk.gov.ida.eidas.metatron.MetatronConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;

public class MetatronAppRule extends DropwizardAppRule<MetatronConfiguration> {

    public MetatronAppRule(ConfigOverride... configOverrides) {
        super(
                MetatronApplication.class,
                resourceFilePath("config.yml"),
                getConfigOverrides(configOverrides)
        );
    }
    private static ConfigOverride[] getConfigOverrides(ConfigOverride ... configOverrides) {
        List<ConfigOverride> overrides = new ArrayList<>(Arrays.asList(configOverrides));
        overrides.add(ConfigOverride.config("server.applicationConnectors[0].port", "0"));
        overrides.add(ConfigOverride.config("server.adminConnectors[0].port", "0"));
        overrides.add(ConfigOverride.config("server.adminConnectors[0].port", "0"));
        overrides.add(ConfigOverride.config("logging.appenders[0].type", "console"));
        overrides.add(ConfigOverride.config("countriesConfig", CreatesConfigDropwizardClientRule.getTempConfigFilePath()));
        return overrides.toArray(new ConfigOverride[0]);
    }
}
