package uk.gov.ida.notification.contracts;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.apache.commons.lang.SerializationUtils;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.Base64;

public class X509CertificateDeserializer extends StdDeserializer<X509Certificate> {


    public X509CertificateDeserializer() {
        super(X509Certificate.class);
    }

    @Override
    public X509Certificate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode node = p.getCodec().readTree(p);
        String base64 = node.asText();
        byte[] decoded = Base64.getDecoder().decode(base64);
        return (X509Certificate) SerializationUtils.deserialize(decoded);
    }
}
