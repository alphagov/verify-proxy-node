package uk.gov.ida.notification.contracts;


import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.Test;
import uk.gov.ida.common.shared.security.X509CertificateFactory;

import java.io.IOException;
import java.security.cert.X509Certificate;

import static org.junit.Assert.assertEquals;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.UNCHAINED_PUBLIC_CERT;

public class X509CertificateSerializerTest {

    @Test
    public void testThatACertCanBeSerializedAndBack() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule testModule = new SimpleModule("TestModule", Version.unknownVersion());
        testModule.addSerializer(new X509CertificateSerializer());
        testModule.addDeserializer(X509Certificate.class, new X509CertificateDeserializer());
        objectMapper.registerModule(testModule);
        X509Certificate cert = new X509CertificateFactory().createCertificate(UNCHAINED_PUBLIC_CERT);
        String base64Cert = objectMapper.writeValueAsString(cert);
        assertEquals(cert, objectMapper.readValue(base64Cert, X509Certificate.class));
    }

    @Test(expected = InvalidDefinitionException.class)
    public void testThatACertCannotBeSerializedWithoutCustomSerializer() throws IOException {
        X509Certificate cert = new X509CertificateFactory().createCertificate(UNCHAINED_PUBLIC_CERT);
        new ObjectMapper().writeValueAsString(cert);
    }

    @Test(expected = InvalidDefinitionException.class)
    public void testThatACertCannotBeDeserializedWithoutCustomDeserializer() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule testModule = new SimpleModule("TestModule", Version.unknownVersion());
        testModule.addSerializer(new X509CertificateSerializer());
        objectMapper.registerModule(testModule);
        X509Certificate cert = new X509CertificateFactory().createCertificate(UNCHAINED_PUBLIC_CERT);
        String base64Cert = new ObjectMapper().writeValueAsString(cert);
        objectMapper.readValue(base64Cert, X509Certificate.class);
    }
}