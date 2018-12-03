package uk.gov.ida.notification.apprule;

import org.glassfish.jersey.internal.util.Base64;
import org.joda.time.DateTime;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.junit.Test;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.StatusCode;
import org.opensaml.security.credential.Credential;
import se.litsec.eidas.opensaml.common.EidasConstants;
import se.litsec.eidas.opensaml.ext.attributes.AttributeConstants;
import se.litsec.eidas.opensaml.ext.attributes.CurrentGivenNameType;
import uk.gov.ida.notification.apprule.base.StubConnectorAppRuleTestBase;
import uk.gov.ida.notification.saml.EidasAttributeBuilder;
import uk.gov.ida.notification.saml.EidasResponseBuilder;
import uk.gov.ida.notification.saml.SamlObjectMarshaller;
import uk.gov.ida.notification.saml.SamlObjectSigner;
import uk.gov.ida.saml.core.test.TestCredentialFactory;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PRIVATE_SIGNING_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PUBLIC_SIGNING_CERT;


public class SamlResponseValidatorAppRuleTests extends StubConnectorAppRuleTestBase {

    @Test
    public void shouldReturnValidSamlResponse() throws Exception {
        String authnId = getAuthnRequestIdFromSession();

        Response unsignedSamlResponse = getEidasSamlMessage(authnId);
        Response signedSamlResponse = signResponse(unsignedSamlResponse);

        String validSamlMessage = responseToString(signedSamlResponse);

        String result = getValidity(validSamlMessage);
        assertEquals("VALID", result);
    }

    @Test
    public void shouldReturnInvalidSamlResponse() throws Exception {
        String authnId = getAuthnRequestIdFromSession();

        Response unsignedSamlResponse = getEidasSamlMessage(authnId);
        String invalidSamlMessage = responseToString(unsignedSamlResponse);

        String result = getValidity(invalidSamlMessage);
        assertEquals("INVALID", result);
    }

    @Test
    public void shouldBeInvalidAsNoneSeenAuthId() throws Exception {
        String authnId = UUID.randomUUID().toString();

        Response unsignedSamlResponse = getEidasSamlMessage(authnId);
        Response signedSamlResponse = signResponse(unsignedSamlResponse);

        String validSamlMessage = responseToString(signedSamlResponse);

        String result = getValidity(validSamlMessage);
        assertEquals("INDETERMINATE", result);
    }


    private String getRequestSaml() throws URISyntaxException {
        String html = getEidasRequest();

        Document document = Jsoup.parse(html);
        Element samlRequest = document.select("input[name=\"SAMLRequest\"]").first();
        return Base64.decodeAsString(samlRequest.val());
    }

    private String getAuthnRequestIdFromSession() throws URISyntaxException {
        String decodedSaml = getRequestSaml();

        Document saml = Jsoup.parse(decodedSaml, "", Parser.xmlParser());
        Element entityDescriptor = saml.select("saml2p|AuthnRequest").first();
        return entityDescriptor.attr("ID");
    }

    private String getValidity(String samlMessage) throws URISyntaxException {
        String html = postEidasResponse(samlMessage);

        Document responseDoc = Jsoup.parse(html);
        Element validityDiv = responseDoc.select("h2").first();

        String result = validityDiv.text();

        String subStringForValidity = "Saml Validity: ";
        if (result.contains(subStringForValidity))
            result = result.replace(subStringForValidity, "");

        return result;
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
