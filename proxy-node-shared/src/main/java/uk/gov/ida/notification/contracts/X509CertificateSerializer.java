package uk.gov.ida.notification.contracts;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.apache.commons.lang.SerializationUtils;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.Base64;

public class X509CertificateSerializer extends StdSerializer<X509Certificate> {

    public X509CertificateSerializer() {
        super(X509Certificate.class);
    }

    @Override
    public void serialize(X509Certificate value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        byte[] serialize = SerializationUtils.serialize(value);
        gen.writeString(Base64.getEncoder().encodeToString(serialize));
    }
}
