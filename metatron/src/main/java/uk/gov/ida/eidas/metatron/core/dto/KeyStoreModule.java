package uk.gov.ida.eidas.metatron.core.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Base64;

public class KeyStoreModule extends SimpleModule {
    public KeyStoreModule() {
        addDeserializer(KeyStore.class, new KeyStoreDeserializer());
    }

    public class KeyStoreDeserializer extends StdDeserializer<KeyStore> {

        protected KeyStoreDeserializer() {
            super((JavaType) null);
        }

        protected KeyStoreDeserializer(Class<?> vc) {
            super(vc);
        }

        protected KeyStoreDeserializer(JavaType valueType) {
            super(valueType);
        }

        protected KeyStoreDeserializer(StdDeserializer<?> src) {
            super(src);
        }

        @Override
        public KeyStore deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            jp.setCodec(new ObjectMapper());
            JsonNode node = jp.getCodec().readTree(jp);
            KeyStore keyStore;
            try {
                byte[] decoded = Base64.getDecoder().decode(node.asText());
                keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                keyStore.load(new ByteArrayInputStream(decoded), "marshmallow".toCharArray());
            } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException e) {
                keyStore = null;
                // Skeleton app
                System.out.println("Things didn't go the way you thought they would");
            }

            return keyStore;
        }
    }
}
