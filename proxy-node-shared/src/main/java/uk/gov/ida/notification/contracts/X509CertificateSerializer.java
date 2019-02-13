package uk.gov.ida.notification.contracts;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Base64;

public class X509CertificateSerializer extends StdSerializer<X509Certificate> {

    public X509CertificateSerializer() {
        super(X509Certificate.class);
    }

    @Override
    public void serialize(X509Certificate value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        final String encodedString;

        try {
            encodedString = Base64.getEncoder().encodeToString(value.getEncoded());
        } catch (CertificateEncodingException e) {
            throw new IOException("Could not encode the X509 cert", e);
        }

        gen.writeString(encodedString);
    }
}
