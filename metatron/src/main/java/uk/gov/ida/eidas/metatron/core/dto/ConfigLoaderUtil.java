package uk.gov.ida.eidas.metatron.core.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.IOException;

public class ConfigLoaderUtil {
    public static EidasConfig loadConfig(String configLocation) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        objectMapper.registerModule(new KeyStoreModule());

        return objectMapper.readValue(new File(configLocation), EidasConfig.class);
    }
}
