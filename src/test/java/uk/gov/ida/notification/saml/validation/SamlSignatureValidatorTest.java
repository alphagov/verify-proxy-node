package uk.gov.ida.notification.saml.validation;

import org.junit.Before;
import org.junit.Test;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.security.credential.BasicCredential;
import org.opensaml.xmlsec.signature.support.SignatureException;
import uk.gov.ida.notification.SamlInitializedTest;
import uk.gov.ida.notification.helpers.HubResponseBuilder;
import uk.gov.ida.notification.helpers.TestKeyPair;
import uk.gov.ida.notification.saml.SamlObjectSigner;
import uk.gov.ida.saml.core.validation.SamlTransformationErrorException;

import java.security.cert.CertificateEncodingException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public class SamlSignatureValidatorTest extends SamlInitializedTest{

    private TestKeyPair testKeyPair;
    private TestKeyPair anotherTestKeyPair;

    @Before
    public void setup() throws Throwable {
        testKeyPair = new TestKeyPair();
        anotherTestKeyPair = new TestKeyPair("test_certificate_b.crt", "test_private_key_b.pk8");
    }

    @Test
    public void shouldValidateValidSignature() throws MarshallingException, SignatureException, CertificateEncodingException {
        SamlSignatureValidator samlSignatureValidator = new SamlSignatureValidator();

        HubResponseBuilder hubResponseBuilder = new HubResponseBuilder();
        Response response = hubResponseBuilder.build();
        SamlObjectSigner samlObjectSigner = new SamlObjectSigner(testKeyPair.publicKey, testKeyPair.privateKey, testKeyPair.getEncodedCertificate());
        BasicCredential signingCredential = new BasicCredential(testKeyPair.publicKey, testKeyPair.privateKey);

        samlObjectSigner.sign(response);
        samlSignatureValidator.validateResponse(signingCredential, response);
    }

    @Test
    public void shouldRaiseExceptionForInvalidSignature() throws MarshallingException, SignatureException, CertificateEncodingException {
        SamlSignatureValidator samlSignatureValidator = new SamlSignatureValidator();

        BasicCredential signingCredential = new BasicCredential(testKeyPair.publicKey, testKeyPair.privateKey);
        HubResponseBuilder hubResponseBuilder = new HubResponseBuilder();
        Response response = hubResponseBuilder.build();
        SamlObjectSigner samlObjectSigner = new SamlObjectSigner(anotherTestKeyPair.publicKey, anotherTestKeyPair.privateKey, testKeyPair.getEncodedCertificate());

        samlObjectSigner.sign(response);

        try {
            samlSignatureValidator.validateResponse(signingCredential, response);
            fail("Should have thrown SamlTransformationErrorException");
        } catch(SamlTransformationErrorException e) {
            assertThat(e.getMessage()).contains("Signature was not valid");
        }
    }

}
