package uk.gov.ida.notification.translator.apprule;

import org.apache.http.HttpStatus;
import org.glassfish.jersey.internal.util.Base64;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.security.credential.BasicCredential;
import org.opensaml.security.credential.Credential;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.SignatureConstants;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.w3c.dom.Element;
import uk.gov.ida.notification.helpers.BasicCredentialBuilder;
import uk.gov.ida.notification.helpers.HubAssertionBuilder;
import uk.gov.ida.notification.helpers.HubResponseBuilder;
import uk.gov.ida.notification.pki.KeyPairConfiguration;
import uk.gov.ida.notification.saml.ResponseAssertionDecrypter;
import uk.gov.ida.notification.saml.SamlFormMessageType;
import uk.gov.ida.notification.saml.SamlObjectMarshaller;
import uk.gov.ida.notification.saml.SamlParser;
import uk.gov.ida.notification.translator.apprule.base.TranslatorAppRuleTestBase;
import uk.gov.ida.saml.core.test.TestCredentialFactory;
import uk.gov.ida.saml.core.test.TestEntityIds;
import uk.gov.ida.saml.core.test.builders.ResponseBuilder;
import uk.gov.ida.saml.security.CredentialFactorySignatureValidator;
import uk.gov.ida.saml.security.SignatureValidator;
import uk.gov.ida.saml.security.SigningCredentialFactory;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import java.net.URISyntaxException;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.Assert.*;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.*;

public class HubResponseTranslatorAppRuleTests extends TranslatorAppRuleTestBase {
    private static final String PROXY_NODE_ENTITY_ID = "http://proxy-node.uk";
    private SamlObjectMarshaller marshaller;
    private BasicCredential hubSigningCredential;
    private BasicCredential idpSigningCredential;
    private EncryptedAssertion authnAssertion;
    private EncryptedAssertion matchingDatasetAssertion;

    @Before
    public void setup() throws Throwable {
        KeyPairConfiguration hubFacingEncryptionKeyPair = translatorAppRule.getConfiguration().getHubFacingEncryptionKeyPair();
        Credential hubAssertionsEncryptionCredential = new BasicCredential(
            hubFacingEncryptionKeyPair.getPublicKey().getPublicKey()
        );
        marshaller = new SamlObjectMarshaller();

        hubSigningCredential = BasicCredentialBuilder.instance()
                .withPublicSigningCert(HUB_TEST_PUBLIC_SIGNING_CERT)
                .withPrivateSigningKey(HUB_TEST_PRIVATE_SIGNING_KEY)
                .build();

        idpSigningCredential = BasicCredentialBuilder.instance()
                .withPublicSigningCert(STUB_IDP_PUBLIC_PRIMARY_CERT)
                .withPrivateSigningKey(STUB_IDP_PUBLIC_PRIMARY_PRIVATE_KEY)
                .build();

        authnAssertion = HubAssertionBuilder.anAuthnStatementAssertion()
            .withSignature(idpSigningCredential, STUB_IDP_PUBLIC_PRIMARY_CERT)
            .withIssuer(TestEntityIds.STUB_IDP_ONE)
            .withSubject(PROXY_NODE_ENTITY_ID, ResponseBuilder.DEFAULT_REQUEST_ID)
            .buildEncrypted(hubAssertionsEncryptionCredential);
        matchingDatasetAssertion = HubAssertionBuilder.aMatchingDatasetAssertion()
            .withSignature(idpSigningCredential, STUB_IDP_PUBLIC_PRIMARY_CERT)
            .withIssuer(TestEntityIds.STUB_IDP_ONE)
            .withSubject(PROXY_NODE_ENTITY_ID, ResponseBuilder.DEFAULT_REQUEST_ID)
            .buildEncrypted(hubAssertionsEncryptionCredential);
    }

    @Test
    @Ignore
    public void shouldReturnASignedEidasResponse() throws Exception {
        KeyPairConfiguration signingKeyPair = translatorAppRule.getConfiguration().getConnectorFacingSigningKeyPair();
        SignatureValidator signatureValidator = new CredentialFactorySignatureValidator(new SigningCredentialFactory(entityId -> singletonList(signingKeyPair.getPublicKey().getPublicKey())));

        Response eidasResponse = extractEidasResponse(buildSignedHubResponse());

        Signature signature = eidasResponse.getSignature();

        assertNotNull("SAML Response needs to be signed", signature);
        assertTrue("Invalid signature", signatureValidator.validate(eidasResponse, null, Response.DEFAULT_ELEMENT_NAME));
        assertEquals(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256, signature.getSignatureAlgorithm());
    }

    @Test
    @Ignore
    public void shouldReturnAnEncryptedEidasResponse() throws Exception {
        Response eidasResponse = extractEidasResponse(buildSignedHubResponse());
        assertEquals(1, eidasResponse.getEncryptedAssertions().size());
        assert(eidasResponse.getAssertions().isEmpty());
    }

    @Test
    @Ignore
    public void postingHubResponseShouldReturnEidasResponseForm() throws Exception {
        Response hubResponse = buildSignedHubResponse();
        Credential decryptingCredential = new TestCredentialFactory(TEST_RP_PUBLIC_ENCRYPTION_CERT, TEST_RP_PRIVATE_ENCRYPTION_KEY).getDecryptingCredential();
        Response eidasResponse = extractEidasResponse(hubResponse);
        Response decryptedEidasResponse = decryptResponse(eidasResponse, decryptingCredential);
        Assertion eidasAssertion = decryptedEidasResponse.getAssertions().get(0);
        Element attributeStatement = marshaller.marshallToElement(eidasAssertion.getAttributeStatements().get(0));

        assertEquals(hubResponse.getInResponseTo(), eidasResponse.getInResponseTo());
        assertEquals(1, eidasAssertion.getAttributeStatements().size());
        assertEquals(1, eidasAssertion.getAuthnStatements().size());

        assertEquals("Jazzy Harold", attributeStatement.getFirstChild().getTextContent());
    }

    @Test
    @Ignore
    public void shouldNotAcceptUnsignedHubResponse() throws Exception {
        javax.ws.rs.core.Response response = postHubResponseToTranslator(buildUnsignedHubResponse());
        String message = response.readEntity(String.class);
        assertEquals(response.getStatus(), HttpStatus.SC_BAD_REQUEST);
        assertThat(message).contains("Error handling hub response");
    }

    @Test
    @Ignore
    public void shouldValidateHubResponseMessage() throws Exception {
        Response invalidResponse = getHubResponseBuilder()
            .withIssuer(null)
            .buildSigned(hubSigningCredential);

        javax.ws.rs.core.Response response = postHubResponseToTranslator(invalidResponse);
        String message = response.readEntity(String.class);

        assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatus());
        assertThat(message).contains("Error handling hub response");
    }

    private Response extractEidasResponse(Response hubResponse) throws Exception {
        String decodedEidasResponse = postHubResponseToTranslator(hubResponse).readEntity(String.class);
        return new SamlParser().parseSamlString(decodedEidasResponse);
    }

    private javax.ws.rs.core.Response postHubResponseToTranslator(Response hubResponse) throws URISyntaxException {
        String encodedResponse = Base64.encodeAsString(marshaller.transformToString(hubResponse));
        Form postForm = new Form().param(SamlFormMessageType.SAML_RESPONSE, encodedResponse);

        return translatorAppRule
                .target("/SAML2/SSO/Response/POST")
                .request()
                .post(Entity.form(postForm));
    }

    private static Response decryptResponse(Response response, Credential credential) {
        ResponseAssertionDecrypter decrypter = new ResponseAssertionDecrypter(credential);
        return decrypter.decrypt(response);
    }

    private Response buildSignedHubResponse() throws MarshallingException, SignatureException {
        return getHubResponseBuilder().buildSigned(hubSigningCredential);
    }

    private Response buildUnsignedHubResponse() throws MarshallingException, SignatureException {
        return getHubResponseBuilder().build();
    }

    private HubResponseBuilder getHubResponseBuilder() {
        return new HubResponseBuilder()
                .withIssuer(TestEntityIds.HUB_ENTITY_ID)
                .withDestination("http://proxy-node/SAML2/SSO/Response")
                .addEncryptedAssertion(authnAssertion)
                .addEncryptedAssertion(matchingDatasetAssertion);
    }
}
