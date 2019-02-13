package uk.gov.ida.notification.contracts;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;

public class X509CertificateDeserializer extends StdDeserializer<X509Certificate> {

    private final CertificateFactory certificateFactory;

    public X509CertificateDeserializer() throws CertificateException {
        super(X509Certificate.class);

        this.certificateFactory = CertificateFactory.getInstance("X.509");
    }

    @Override
    public X509Certificate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {

        final byte[] decodedString = Base64.getDecoder().decode(p.getValueAsString());
        final ByteArrayInputStream byteArr = new ByteArrayInputStream(decodedString);

        try {
            return (X509Certificate) certificateFactory.generateCertificate(byteArr);
        } catch (CertificateException e) {
            throw new IOException("Could not decode X509 certificate from byte array", e);
        }
    }
}
