package uk.gov.ida.notification.apprule;

import org.glassfish.jersey.internal.util.Base64;
import org.junit.Before;
import org.junit.Test;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.encryption.Decrypter;
import org.opensaml.security.credential.BasicCredential;
import org.opensaml.security.credential.Credential;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.SignatureConstants;
import org.w3c.dom.Element;
import uk.gov.ida.notification.apprule.base.ProxyNodeAppRuleTestBase;
import uk.gov.ida.notification.helpers.HtmlHelpers;
import uk.gov.ida.notification.helpers.HubResponseBuilder;
import uk.gov.ida.notification.helpers.TestKeyPair;
import uk.gov.ida.notification.pki.DecryptionCredential;
import uk.gov.ida.notification.pki.KeyPairConfiguration;
import uk.gov.ida.notification.saml.SamlFormMessageType;
import uk.gov.ida.notification.saml.SamlObjectMarshaller;
import uk.gov.ida.notification.saml.SamlParser;
import uk.gov.ida.saml.security.CredentialFactorySignatureValidator;
import uk.gov.ida.saml.security.DecrypterFactory;
import uk.gov.ida.saml.security.SignatureValidator;
import uk.gov.ida.saml.security.SigningCredentialFactory;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class HubResponseAppRuleTests extends ProxyNodeAppRuleTestBase {
    private SamlObjectMarshaller marshaller;
    private Response hubResponse;

    @Before
    public void setup() throws Throwable {
        KeyPairConfiguration hubFacingEncryptionKeyPair = proxyNodeAppRule.getConfiguration().getHubFacingEncryptionKeyPair();
        Credential hubAssertionsEncryptionCredential = new BasicCredential(
                hubFacingEncryptionKeyPair.getPublicKey().getPublicKey()
        );
        marshaller = new SamlObjectMarshaller();
        hubResponse = new HubResponseBuilder()
                .addAuthnStatementAssertionUsing(hubAssertionsEncryptionCredential)
                .addMatchingDatasetAssertionUsing(hubAssertionsEncryptionCredential)
                .build();
    }

    @Test
    public void shouldReturnASignedEidasResponse() throws Exception {
        KeyPairConfiguration signingKeyPair = proxyNodeAppRule.getConfiguration().getSigningKeyPair();
        SignatureValidator signatureValidator = new CredentialFactorySignatureValidator(new SigningCredentialFactory(entityId -> singletonList(signingKeyPair.getPublicKey().getPublicKey())));

        Response eidasResponse = readResponseFromHub(hubResponse);

        Signature signature = eidasResponse.getSignature();

        assertNotNull("SAML Response needs to be signed", signature);
        assertTrue("Invalid signature", signatureValidator.validate(eidasResponse, null, Response.DEFAULT_ELEMENT_NAME));
        assertEquals(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256, signature.getSignatureAlgorithm());
    }

    @Test
    public void shouldReturnAnEncryptedEidasResponse() throws Exception {
        Response eidasResponse = readResponseFromHub(hubResponse);
        assertEquals(1, eidasResponse.getEncryptedAssertions().size());
        assert(eidasResponse.getAssertions().isEmpty());
    }

    @Test
    public void postingHubResponseShouldReturnEidasResponseForm() throws Exception {
        TestKeyPair keyPair = new TestKeyPair();
        DecryptionCredential eidasAssertionsDecryptionCredential = new DecryptionCredential(
                keyPair.publicKey, keyPair.privateKey
        );

        Response eidasResponse = readResponseFromHub(hubResponse);

        Assertion eidasAssertion = decryptAssertion(eidasResponse.getEncryptedAssertions().get(0), eidasAssertionsDecryptionCredential);
        Element attributeStatement = marshaller.marshallToElement(eidasAssertion.getAttributeStatements().get(0));

        assertEquals(hubResponse.getInResponseTo(), eidasResponse.getInResponseTo());
        assertEquals(1, eidasAssertion.getAttributeStatements().size());
        assertEquals(1, eidasAssertion.getAuthnStatements().size());

        assertEquals("Jazzy Harold", attributeStatement.getFirstChild().getTextContent());
    }

    private Response readResponseFromHub(Response hubResponse) throws Exception {
        String encodedResponse = Base64.encodeAsString(marshaller.transformToString(hubResponse));
        Form postForm = new Form().param(SamlFormMessageType.SAML_RESPONSE, encodedResponse);

        String html = proxyNodeAppRule.target("/SAML2/SSO/Response/POST").request()
                .post(Entity.form(postForm))
                .readEntity(String.class);

        String decodedEidasResponse = HtmlHelpers.getValueFromForm(html, "saml-form", SamlFormMessageType.SAML_RESPONSE);
        return new SamlParser().parseSamlString(decodedEidasResponse);
    }

    private static Assertion decryptAssertion(EncryptedAssertion encryptedAssertion, DecryptionCredential credential) throws Exception {
        DecrypterFactory decrypterFactory = new DecrypterFactory();
        Decrypter decrypter = decrypterFactory.createDecrypter(singletonList(credential));
        return decrypter.decrypt(encryptedAssertion);
    }
}
