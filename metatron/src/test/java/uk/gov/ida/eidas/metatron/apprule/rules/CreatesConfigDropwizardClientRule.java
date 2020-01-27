package uk.gov.ida.eidas.metatron.apprule.rules;

import io.dropwizard.testing.junit.DropwizardClientRule;
import org.junit.Assert;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

public class CreatesConfigDropwizardClientRule extends DropwizardClientRule {

    private static String tempConfigLocation;

    public CreatesConfigDropwizardClientRule(MetatronTests.TestMetadataResource testMetadataResource) {
        super(testMetadataResource);
        try {
            this.before();
        } catch (Throwable throwable) {
            Assert.fail(throwable.getMessage());
        }
        try {
            writeConfigFile();
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    private void writeConfigFile() throws IOException {
        URI uri = super.baseUri();
        String templateFilePath = this.getClass().getClassLoader().getResource("countriesConfig.yaml").getFile();
        String templateConfig = new String(new FileInputStream(new File(templateFilePath)).readAllBytes());
        String configWithHost = templateConfig.replace("{host_holder}", uri.toString());

        tempConfigLocation = Files.createTempDirectory(null).toString();

        try(FileWriter writer = new FileWriter(getTempConfigFilePath())) {
            writer.write(configWithHost);
            writer.flush();
        }
    }
    public int getPort() {
        return super.baseUri().getPort();
    }
    public static String getTempConfigFilePath() {
        return Path.of(tempConfigLocation, "countriesConfig.yaml").toString();
    }
}
