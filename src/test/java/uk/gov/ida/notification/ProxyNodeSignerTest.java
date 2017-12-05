package uk.gov.ida.notification;

import org.junit.Test;
import org.opensaml.core.config.InitializationService;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.impl.AuthnRequestBuilder;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.SignatureConstants;
import org.opensaml.xmlsec.signature.support.SignatureValidator;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ProxyNodeSignerTest {

    @Test
    public void shouldBuildProxyNodeSignature () throws Throwable {
        InitializationService.initialize();
        String requestId = "requestId";
        AuthnRequestBuilder authnRequestBuilder = new AuthnRequestBuilder();
        AuthnRequest authRequest = authnRequestBuilder.buildObject();
        authRequest.setID(requestId);
        Credential credential = buildCredential();
        ProxyNodeSigner proxyNodeSigner = new ProxyNodeSigner();

        AuthnRequest signedAuthnRequest = proxyNodeSigner.sign(authRequest, credential);

        assertEquals(requestId, signedAuthnRequest.getID());
        Signature signature = signedAuthnRequest.getSignature();
        assertNotNull(signature);
        String algoIdSignatureRsaSha256 = SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256;
        assertEquals(signature.getSignatureAlgorithm(), algoIdSignatureRsaSha256);
        assertEquals(signature.getSigningCredential(), credential);
        assertEquals(signature.getCanonicalizationAlgorithm(), SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
        SignatureValidator.validate(signature, credential);
    }

    private Credential buildCredential() throws Throwable {
        String publicKey = "src/main/resources/pki/hub_signing.crt";
        String privateKey = "src/main/resources/pki/hub_signing.pk8";
        InputStream publicKeyInputStream = new FileInputStream(publicKey);
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        X509Certificate publicKeyCert = (X509Certificate)certificateFactory.generateCertificate(publicKeyInputStream);
        publicKeyInputStream.close();

        RandomAccessFile raf = new RandomAccessFile(privateKey, "r");
        byte[] privateKeyAsBytes = new byte[(int)raf.length()];
        raf.readFully(privateKeyAsBytes);
        raf.close();

        PKCS8EncodedKeySpec kspec = new PKCS8EncodedKeySpec(privateKeyAsBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PrivateKey privateKeyCert = kf.generatePrivate(kspec);

        return new BasicX509Credential(publicKeyCert, privateKeyCert);
    }

}
