package uk.gov.ida.notification.saml;

import org.junit.Before;
import org.junit.Test;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.SignatureConstants;
import org.opensaml.xmlsec.signature.support.SignatureValidator;
import uk.gov.ida.notification.SamlInitializedTest;
import uk.gov.ida.notification.helpers.TestKeyPair;

import static org.assertj.core.api.Assertions.assertThat;

public class SamlObjectSignerTest extends SamlInitializedTest {
    private TestKeyPair testKeyPair;

    @Before
    public void setup() throws Throwable {
        testKeyPair = new TestKeyPair();
    }

    @Test
    public void shouldSignAuthRequest() throws Throwable {
        BasicX509Credential signingCredential = testKeyPair.getX509Credential();
        SamlObjectSigner samlObjectSigner = new SamlObjectSigner(signingCredential, SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256);
        AuthnRequest authnRequest = SamlBuilder.build(AuthnRequest.DEFAULT_ELEMENT_NAME);
        samlObjectSigner.sign(authnRequest);
        Signature signature = authnRequest.getSignature();

        String actualCertificate = signature.getKeyInfo().getX509Datas().get(0).getX509Certificates().get(0).getValue();

        assertThat(testKeyPair.getEncodedCertificate()).isEqualTo(actualCertificate.replaceAll("\\s+", ""));
        assertThat(signature).isNotNull();
        String algoIdSignatureRsaSha256 = SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256;
        assertThat(signature.getSignatureAlgorithm()).isEqualTo(algoIdSignatureRsaSha256);
        assertThat(signature.getSigningCredential().getPublicKey()).isEqualTo(signingCredential.getPublicKey());
        assertThat(signature.getSigningCredential().getPrivateKey()).isEqualTo(signingCredential.getPrivateKey());
        assertThat(signature.getCanonicalizationAlgorithm()).isEqualTo(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
        SignatureValidator.validate(signature, signingCredential);
    }
}
