package uk.gov.ida.notification.eidassaml;

import org.glassfish.jersey.internal.util.Base64;
import org.joda.time.DateTime;
import org.junit.Test;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Issuer;
import se.litsec.opensaml.utils.ObjectUtils;
import uk.gov.ida.notification.contracts.EidasSamlParserRequest;
import uk.gov.ida.notification.eidassaml.apprule.base.EidasSamlParserAppRuleTestBase;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class IstioHeaderTest extends EidasSamlParserAppRuleTestBase {

    private static final String X_REQUEST_ID = "x-request-id";
    private static final String X_B3_TRACEID = "x-b3-traceid";
    private static final String X_B3_SPANID = "x-b3-spanid";
    private static final String X_B3_PARENTSPANID = "x-b3-parentspanid";
    private static final String X_B3_SAMPLED = "x-b3-sampled";
    private static final String X_B3_FLAGS = "x-b3-flags";
    private static final String X_OT_SPAN_CONTEXT = "x-ot-span-context";
    private static final String SOME_RANDOM_HEADER = "some-random-header";

    @Test
    public void checkIfHeadersPersist() throws Exception {
        AuthnRequest authnRequest = ObjectUtils.createSamlObject(AuthnRequest.class);
        Issuer issuer = ObjectUtils.createSamlObject(Issuer.class);
        issuer.setValue("issuer");
        authnRequest.setID("request_id");
        authnRequest.setIssuer(issuer);
        authnRequest.setDestination("destination");
        authnRequest.setIssueInstant(new DateTime(2019, 02, 28, 9, 54));


        final String xRequestId = "x-request-id";
        final String xB3Traceid = "x-b3-traceid";
        final String xB3Spanid = "x-b3-spanid";
        final String xB3Parentspanid = "x-b3-parentspanid";
        final String xB3Sampled = "x-b3-sampled";
        final String xB3Flags = "x-b3-flags";
        final String xOtSpanContext = "x-ot-span-context";
        final String someRandomHeader = "somerandomheader";

        EidasSamlParserRequest request = new EidasSamlParserRequest(Base64.encodeAsString(ObjectUtils.toString(authnRequest)));
        Response response = eidasSamlParserAppRule.target("/eidasAuthnRequest")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header(X_REQUEST_ID, xRequestId)
                .header(X_B3_TRACEID, xB3Traceid)
                .header(X_B3_SPANID, xB3Spanid)
                .header(X_B3_PARENTSPANID, xB3Parentspanid)
                .header(X_B3_SAMPLED, xB3Sampled)
                .header(X_B3_FLAGS, xB3Flags)
                .header(X_OT_SPAN_CONTEXT, xOtSpanContext)
                .header(SOME_RANDOM_HEADER, someRandomHeader)
                .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));



        assertThat(response.getHeaders().getFirst(X_REQUEST_ID)).isEqualTo(xRequestId);
        assertThat(response.getHeaders().getFirst(X_B3_TRACEID)).isEqualTo(xB3Traceid);
        assertThat(response.getHeaders().getFirst(X_B3_SPANID)).isEqualTo(xB3Spanid);
        assertThat(response.getHeaders().getFirst(X_B3_PARENTSPANID)).isEqualTo(xB3Parentspanid);
        assertThat(response.getHeaders().getFirst(X_B3_SAMPLED)).isEqualTo(xB3Sampled);
        assertThat(response.getHeaders().getFirst(X_B3_FLAGS)).isEqualTo(xB3Flags);
        assertThat(response.getHeaders().getFirst(X_OT_SPAN_CONTEXT)).isEqualTo(xOtSpanContext);
        assertThat(response.getHeaders().containsKey(SOME_RANDOM_HEADER)).isFalse();
    }
}
