package uk.gov.ida.notification.helpers;

import io.dropwizard.testing.ResourceHelpers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileHelpers {
    public static String readFileAsString(String resourceFilename) throws IOException {
        Path authnRequestPath = Paths.get(ResourceHelpers.resourceFilePath(resourceFilename));
        return new String(Files.readAllBytes(authnRequestPath));
    }
}
