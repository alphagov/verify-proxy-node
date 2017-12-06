package uk.gov.ida.notification;

import org.junit.Test;
import org.opensaml.core.config.InitializationService;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.impl.AuthnRequestBuilder;
import org.opensaml.security.credential.Credential;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.SignatureConstants;
import org.opensaml.xmlsec.signature.support.SignatureValidator;
import org.w3c.dom.Element;
import uk.gov.ida.notification.saml.XmlObjectMarshaller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static uk.gov.ida.notification.helpers.PKIHelpers.buildCredential;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ProxyNodeSignerTest {

    @Test
    public void shouldBuildProxyNodeSignature () throws Throwable {
        InitializationService.initialize();
        String requestId = "requestId";
        AuthnRequestBuilder authnRequestBuilder = new AuthnRequestBuilder();
        AuthnRequest authRequest = authnRequestBuilder.buildObject();
        authRequest.setID(requestId);
        Credential credential = buildCredential(
                "local/hub_signing_primary.crt",
                "local/hub_signing_primary.pk8");

        XmlObjectMarshaller marshaller = mock(XmlObjectMarshaller.class);
        when(marshaller.marshall(authRequest)).thenAnswer(invocation -> mashall(authRequest));
        ProxyNodeSigner proxyNodeSigner = new ProxyNodeSigner(marshaller);

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

    private Element mashall(SAMLObject samlObject) throws Throwable {
        return XMLObjectProviderRegistrySupport.getMarshallerFactory()
                .getMarshaller(samlObject)
                .marshall(samlObject);
    }
}
