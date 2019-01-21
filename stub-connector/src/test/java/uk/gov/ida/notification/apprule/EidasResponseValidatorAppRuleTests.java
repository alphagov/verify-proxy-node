package uk.gov.ida.notification.apprule;

import org.joda.time.DateTime;
import org.junit.Test;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.StatusCode;
import org.opensaml.security.credential.Credential;
import se.litsec.eidas.opensaml.common.EidasConstants;
import se.litsec.eidas.opensaml.ext.attributes.AttributeConstants;
import se.litsec.eidas.opensaml.ext.attributes.CurrentGivenNameType;
import uk.gov.ida.notification.apprule.base.StubConnectorAppRuleTestBase;
import uk.gov.ida.notification.helpers.HtmlHelpers;
import uk.gov.ida.notification.saml.*;
import uk.gov.ida.saml.core.test.TestCredentialFactory;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
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


    private String getAuthnRequestIdFromSession() throws URISyntaxException, IOException, ParserConfigurationException {
        String html = getEidasRequest();
        String decodedSaml = HtmlHelpers.getValueFromForm(html, "SAMLRequest");
        AuthnRequest request = new SamlParser().parseSamlString(decodedSaml);
        return request.getID();
    }

    private void hasValidity(String samlMessage, String validity) throws URISyntaxException {
        String html = postEidasResponse(samlMessage);
        assertThat(html, containsString("Saml Validity: "+validity));
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

        Response response = EidasResponseBuilder.createEidasResponse(
                "http://stub-connector/",
                StatusCode.SUCCESS,
                UUID.randomUUID().toString(),
                EidasConstants.EIDAS_LOA_SUBSTANTIAL,
                eidasAttributes,
                authid,
                now,
                now,
                now,
                "http://stub-connector/SAML2/Response/POST",
                "http://localhost:5000/Metadata"
        );

        return response;
    }

    private Response signResponse(Response response) {
        Credential signingCredential = new TestCredentialFactory(
                TEST_RP_PUBLIC_SIGNING_CERT,
                TEST_RP_PRIVATE_SIGNING_KEY
        ).getSigningCredential();

        SamlObjectSigner signer = new SamlObjectSigner(
                signingCredential.getPublicKey(),
                signingCredential.getPrivateKey(),
                TEST_RP_PUBLIC_SIGNING_CERT
        );

        signer.sign(response);

        return response;
    }

    private String responseToString(Response response) {
        SamlObjectMarshaller marshaller = new SamlObjectMarshaller();
        return marshaller.transformToString(response);
    }
}
