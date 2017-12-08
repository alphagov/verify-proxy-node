package uk.gov.ida.notification;

import org.junit.Before;
import org.junit.Test;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.impl.AuthnRequestBuilder;
import org.opensaml.saml.saml2.core.impl.ResponseBuilder;
import org.opensaml.security.credential.Credential;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.SignatureConstants;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.SignatureValidator;
import uk.gov.ida.notification.helpers.SamlMarshallerHelper;
import uk.gov.ida.notification.saml.XmlObjectMarshaller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.ida.notification.helpers.PKIHelpers.buildAnyCredential;

public class ProxyNodeSignerTest {

    private ProxyNodeSigner proxyNodeSigner;
    private XmlObjectMarshaller marshaller;
    private SamlMarshallerHelper marshallerHelper = new SamlMarshallerHelper();

    @Before
    public void before() throws InitializationException {
        InitializationService.initialize();
        marshaller = mock(XmlObjectMarshaller.class);
        proxyNodeSigner = new ProxyNodeSigner(marshaller);
    }

    @Test
    public void shouldSignAuthRequest () throws Throwable {
        String requestId = "requestId";
        AuthnRequestBuilder authnRequestBuilder = new AuthnRequestBuilder();
        AuthnRequest authRequest = authnRequestBuilder.buildObject();
        authRequest.setID(requestId);
        Credential credential = buildAnyCredential();
        when(marshaller.marshall(authRequest)).thenAnswer(invocation -> marshallerHelper.mashall(authRequest));

        AuthnRequest signedAuthnRequest = proxyNodeSigner.sign(authRequest, credential);

        assertEquals(requestId, signedAuthnRequest.getID());
        Signature signature = signedAuthnRequest.getSignature();
        signatureShouldBeValid(credential, signature);
    }

    @Test
    public void shouldSignResponse () throws Throwable {
        String responseId = "responsetId";
        ResponseBuilder responseBuilder = new ResponseBuilder();
        Response response = responseBuilder.buildObject();
        response.setID(responseId);
        Credential credential = buildAnyCredential();
        XmlObjectMarshaller marshaller = mock(XmlObjectMarshaller.class);
        when(marshaller.marshall(response)).thenAnswer(invocation -> marshallerHelper.mashall(response));
        ProxyNodeSigner proxyNodeSigner = new ProxyNodeSigner(marshaller);

        Response signedResponse = proxyNodeSigner.sign(response, credential);

        assertEquals(responseId, signedResponse.getID());
        Signature signature = signedResponse.getSignature();
        signatureShouldBeValid(credential, signature);
    }

    private void signatureShouldBeValid(Credential credential, Signature signature) throws SignatureException {
        assertNotNull(signature);
        String algoIdSignatureRsaSha256 = SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256;
        assertEquals(signature.getSignatureAlgorithm(), algoIdSignatureRsaSha256);
        assertEquals(signature.getSigningCredential(), credential);
        assertEquals(signature.getCanonicalizationAlgorithm(), SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
        SignatureValidator.validate(signature, credential);
    }
}
