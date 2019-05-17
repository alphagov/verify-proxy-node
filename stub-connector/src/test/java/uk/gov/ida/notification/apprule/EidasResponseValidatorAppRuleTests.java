package uk.gov.ida.notification.apprule;

import org.joda.time.DateTime;
import org.junit.Test;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.StatusCode;
import org.opensaml.xmlsec.signature.support.SignatureConstants;
import se.litsec.eidas.opensaml.common.EidasConstants;
import se.litsec.eidas.opensaml.ext.attributes.AttributeConstants;
import se.litsec.eidas.opensaml.ext.attributes.CurrentGivenNameType;
import uk.gov.ida.notification.apprule.base.StubConnectorAppRuleTestBase;
import uk.gov.ida.notification.helpers.HtmlHelpers;
import uk.gov.ida.notification.helpers.X509CredentialFactory;
import uk.gov.ida.notification.saml.EidasAttributeBuilder;
import uk.gov.ida.notification.saml.EidasResponseBuilder;
import uk.gov.ida.notification.saml.SamlObjectMarshaller;
import uk.gov.ida.notification.saml.SamlObjectSigner;
import uk.gov.ida.notification.saml.SamlParser;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PRIVATE_SIGNING_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PUBLIC_SIGNING_CERT;


public class EidasResponseValidatorAppRuleTests extends StubConnectorAppRuleTestBase {

    @Test
    public void shouldReturnValidSamlResponse() throws Exception {
        String authnId = getAuthnRequestIdFromSession();

        Response unsignedSamlResponse = getEidasSamlMessage(authnId);
        Response signedSamlResponse = signResponse(unsignedSamlResponse);

        String validSamlMessage = responseToString(signedSamlResponse);

        hasValidity(validSamlMessage, "VALID");
    }

    @Test
    public void shouldReturnInvalidSamlResponse() throws Exception {
        String authnId = getAuthnRequestIdFromSession();

        Response unsignedSamlResponse = getEidasSamlMessage(authnId);
        String invalidSamlMessage = responseToString(unsignedSamlResponse);

        hasValidity(invalidSamlMessage, "INVALID");
    }

    @Test
    public void shouldBeInvalidAsNoneSeenAuthId() throws Exception {
        String authnId = UUID.randomUUID().toString();

        Response unsignedSamlResponse = getEidasSamlMessage(authnId);
        Response signedSamlResponse = signResponse(unsignedSamlResponse);

        String validSamlMessage = responseToString(signedSamlResponse);

        hasValidity(validSamlMessage, "INDETERMINATE");
    }


    private String getAuthnRequestIdFromSession() throws URISyntaxException, IOException {
        String html = getEidasRequest();
        String decodedSaml = HtmlHelpers.getValueFromForm(html, "SAMLRequest");
        AuthnRequest request = new SamlParser().parseSamlString(decodedSaml);
        return request.getID();
    }

    private void hasValidity(String samlMessage, String validity) throws URISyntaxException {
        String html = postEidasResponse(samlMessage);
        assertThat(html).contains("Saml Validity: " + validity);
    }

    private Response getEidasSamlMessage(String authid) {

        Attribute firstname = new EidasAttributeBuilder(
                AttributeConstants.EIDAS_CURRENT_GIVEN_NAME_ATTRIBUTE_NAME,
                AttributeConstants.EIDAS_CURRENT_GIVEN_NAME_ATTRIBUTE_FRIENDLY_NAME,
                CurrentGivenNameType.TYPE_NAME,
                "Jazzy Harrold"
        ).build();

        ArrayList<Attribute> eidasAttributes = new ArrayList<>();

        eidasAttributes.add(firstname);

        DateTime now = DateTime.now();

        return EidasResponseBuilder.instance()
                .withIssuer("http://stub-connector/")
                .withStatus(StatusCode.SUCCESS)
                .withAssertionSubject(UUID.randomUUID().toString())
                .addAssertionAuthnStatement(EidasConstants.EIDAS_LOA_SUBSTANTIAL, now)
                .addAssertionAttributeStatement(eidasAttributes)
                .withInResponseTo(authid)
                .withIssueInstant(now)
                .withDestination("http://stub-connector/SAML2/Response/POST")
                .withAssertionConditions("http://localhost:5000/Metadata")
                .build();
    }

    private Response signResponse(Response response) throws Exception {
        SamlObjectSigner signer = new SamlObjectSigner(X509CredentialFactory.build(
                TEST_RP_PUBLIC_SIGNING_CERT, TEST_RP_PRIVATE_SIGNING_KEY),
                SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256);

        signer.sign(response, "response-id");

        return response;
    }

    private String responseToString(Response response) {
        SamlObjectMarshaller marshaller = new SamlObjectMarshaller();
        return marshaller.transformToString(response);
    }
}
