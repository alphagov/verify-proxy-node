package uk.gov.ida.notification.helpers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;

public class FileHelpers {
    public static String readFileAsString(String resourceFilename) throws IOException {
        return IOUtils.toString(FileHelpers.class.getResourceAsStream("/" + resourceFilename), StandardCharsets.UTF_8);
    }

    public static byte[] readFileAsBytes(String resourceFilename) throws IOException {
        return IOUtils.toByteArray(FileHelpers.class.getResourceAsStream("/" + resourceFilename));
    }
}
