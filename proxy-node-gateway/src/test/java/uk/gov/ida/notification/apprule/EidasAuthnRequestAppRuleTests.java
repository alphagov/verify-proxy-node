package uk.gov.ida.notification.apprule;

import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.saml2.core.AuthnContextComparisonTypeEnumeration;
import org.opensaml.saml.saml2.core.AuthnRequest;
import se.litsec.eidas.opensaml.common.EidasConstants;
import uk.gov.ida.notification.apprule.base.GatewayAppRuleTestBase;
import uk.gov.ida.notification.helpers.EidasAuthnRequestBuilder;
import uk.gov.ida.notification.helpers.HtmlHelpers;
import uk.gov.ida.notification.saml.SamlFormMessageType;
import uk.gov.ida.notification.saml.SamlParser;

import javax.ws.rs.core.Response;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class EidasAuthnRequestAppRuleTests extends GatewayAppRuleTestBase {

    private SamlParser parser;
    private EidasAuthnRequestBuilder request;

    @Before
    public void setup() throws Throwable {
        parser = new SamlParser();
        request = new EidasAuthnRequestBuilder().withIssuer(CONNECTOR_NODE_ENTITY_ID);
    }

    @Ignore
    @Test
    public void bindingsReturnHubAuthnRequestForm() throws Throwable {
        assertGoodRequest(request);
    }

    @Ignore
    @Test
    public void bindingsValidateAuthnRequestHasRequestId() throws Throwable {
        AuthnRequest requestWithoutId = request.withoutRequestId().build();
        samlObjectSigner.sign(requestWithoutId);

        assertErrorResponse(postEidasAuthnRequest(requestWithoutId));
        assertErrorResponse(redirectEidasAuthnRequest(requestWithoutId));
    }

    @Ignore
    @Test
    public void bindingsValidateAuthnRequestHasSignature() throws Throwable {
        AuthnRequest postedRequest = request.withRandomRequestId().build();
        assertErrorResponse(postEidasAuthnRequest(postedRequest));

        AuthnRequest redirectedRequest = request.withRandomRequestId().build();
        assertErrorResponse(redirectEidasAuthnRequest(redirectedRequest));
    }

    @Ignore
    @Test
    public void bindingsValidateAuthnRequestHasForceAuthn() throws Throwable {
        assertBadRequest(request.withForceAuthn(false));
    }

    @Ignore
    @Test
    public void bindingsValidateAuthnRequestIsPassiveIsNotTrue() throws Throwable {
        assertBadRequest(request.withIsPassive(true));
    }

    @Ignore
    @Test
    public void bindingsValidateAuthnRequestWhenIsPassiveIsMissing() throws Throwable {
        assertGoodRequest(request.withoutIsPassive());
    }

    @Ignore
    @Test
    public void bindingsValidateAuthnRequestHasCorrectDestination() throws Throwable {
        assertBadRequest(request.withDestination("https://bogus.eu/"));
    }

    @Ignore
    @Test
    public void bindingsValidateAuthnRequestHasCorrectComparison() throws Throwable {
        assertBadRequest(request.withComparison(AuthnContextComparisonTypeEnumeration.MAXIMUM));
    }

    @Ignore
    @Test
    public void bindingsValidateAuthnRequestHasExtensions() throws Throwable {
        assertBadRequest(request.withoutExtensions());
    }

    @Ignore
    @Test
    public void bindingsValidateAuthnRequestHasNoProtocolBinding() throws Throwable {
        assertBadRequest(request.withProtocolBinding("protocol-binding-attribute"));
    }

    @Ignore
    @Test
    public void bindingsValidateAuthnRequestHasCorrectSamlVersion() throws Throwable {
        assertBadRequest(request.withSamlVersion(SAMLVersion.VERSION_10));
        assertBadRequest(request.withSamlVersion(SAMLVersion.VERSION_11));
        assertGoodRequest(request.withSamlVersion(SAMLVersion.VERSION_20));
    }

    @Ignore
    @Test
    public void bindingsValidateAuthnRequestHasIssuer() throws Throwable {
        assertBadRequest(request.withIssuer(""));
    }

    @Ignore
    @Test
    public void bindingsValidateAuthnRequestHasSPTypePublicOrMissing() throws Throwable {
        assertBadRequest(request.withSpType("private"));
        assertGoodRequest(request.withSpType("public"));
        assertGoodRequest(request.withoutSpType());
    }

    @Ignore
    @Test
    public void bindingsValidateAuthnRequestHasSupportedLOA() throws Throwable {
        assertGoodRequest(request.withLoa(EidasConstants.EIDAS_LOA_SUBSTANTIAL));

        assertBadRequest(request.withLoa(EidasConstants.EIDAS_LOA_LOW));
        assertBadRequest(request.withLoa(EidasConstants.EIDAS_LOA_HIGH));
        assertBadRequest(request.withoutLoa());
    }

    @Ignore
    @Test
    public void bindingsValidateAuthnRequestHasRequestedAttributes() throws Throwable {
        assertBadRequest(request.withoutRequestedAttributes());
    }

    @Ignore
    @Test
    public void bindingsValidateAuthnRequestHasCorrectAssertionConsumerServiceUrl() throws Throwable {
        assertBadRequest(request.withAssertionConsumerServiceURL("invalid-assertion-consumer-service-url"));
    }

    @Ignore
    @Test
    public void bindingsValidateAuthnRequestIsNotDuplicated() throws Throwable {
        AuthnRequest duplicatedRequest = request.build();
        samlObjectSigner.sign(duplicatedRequest);

        assertGoodResponse(duplicatedRequest, postEidasAuthnRequest(duplicatedRequest));
        assertErrorResponse(postEidasAuthnRequest(duplicatedRequest));
    }

    private AuthnRequest getHubAuthnRequestFromHtml(String html) throws IOException {
        String decodedHubAuthnRequest = HtmlHelpers.getValueFromForm(html, SamlFormMessageType.SAML_REQUEST);
        return parser.parseSamlString(decodedHubAuthnRequest);
    }

    private void assertGoodRequest(EidasAuthnRequestBuilder builder) throws Throwable {
        AuthnRequest postedRequest = builder.withRandomRequestId().build();
        samlObjectSigner.sign(postedRequest);
        assertGoodResponse(postedRequest, postEidasAuthnRequest(postedRequest));

        AuthnRequest redirectedRequest = builder.withRandomRequestId().build();
        samlObjectSigner.sign(redirectedRequest);
        assertGoodResponse(redirectedRequest, redirectEidasAuthnRequest(redirectedRequest));
    }

    private void assertBadRequest(EidasAuthnRequestBuilder builder) throws Throwable {
        AuthnRequest postedRequest = builder.withRandomRequestId().build();
        samlObjectSigner.sign(postedRequest);
        assertErrorResponse(postEidasAuthnRequest(postedRequest));

        AuthnRequest redirectedRequest = builder.withRandomRequestId().build();
        samlObjectSigner.sign(redirectedRequest);
        assertErrorResponse(redirectEidasAuthnRequest(redirectedRequest));
    }

    private void assertGoodResponse(AuthnRequest eidasAuthnRequest, Response response) throws IOException {
        String html = response.readEntity(String.class);
        AuthnRequest hubAuthnRequest = getHubAuthnRequestFromHtml(html);

        assertEquals(eidasAuthnRequest.getID(), hubAuthnRequest.getID());
    }

    private void assertErrorResponse(Response response) {
        assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatus());
        assertThat(response.readEntity(String.class), containsString("Sorry, something went wrong"));
    }
}
