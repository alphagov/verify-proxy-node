package uk.gov.ida.eidas.metatron.apprule.rules;

import io.dropwizard.testing.junit.DropwizardClientRule;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

public class CountryMetadataClientRule extends DropwizardClientRule {
    private final String tempConfigLocation;

    public CountryMetadataClientRule(TestCountryMetadataResource testCountryMetadataResource) {
        super(testCountryMetadataResource);
        try {
            this.before();
            tempConfigLocation = Files.createTempDirectory(null).toString();
            writeConfigFile();
            testCountryMetadataResource.initialiseCountryMetadatas(this.getPort());
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    public int getPort() {
        return super.baseUri().getPort();
    }

    public String getTempConfigFilePath() {
        return Path.of(tempConfigLocation, "countriesConfig.yaml").toString();
    }

    private void writeConfigFile() throws IOException {
        URI uri = super.baseUri();
        String templateFilePath = this.getClass().getClassLoader().getResource("countriesConfig.yaml").getFile();
        String templateConfig = new String(new FileInputStream(new File(templateFilePath)).readAllBytes());
        String configWithHost = templateConfig.replace("{host_holder}", uri.toString());

        try (FileWriter writer = new FileWriter(getTempConfigFilePath())) {
            writer.write(configWithHost);
            writer.flush();
        }
    }
}
