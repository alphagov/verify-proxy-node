package uk.gov.ida.eidas.metadataservice.core.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.IOException;

public class ConfigLoader {
    public static EidasConfig loadConfig(String configLocation) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        objectMapper.registerModule(new KeyStoreModule());

        return objectMapper.readValue(new File(configLocation), EidasConfig.class);
    }
}
