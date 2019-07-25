package uk.gov.ida.notification.eidassaml.apprule;

import org.glassfish.jersey.internal.util.Base64;
import org.joda.time.DateTime;
import org.junit.Test;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Issuer;
import se.litsec.opensaml.utils.ObjectUtils;
import uk.gov.ida.notification.contracts.EidasSamlParserRequest;
import uk.gov.ida.notification.eidassaml.apprule.base.EidasSamlParserAppRuleTestBase;
import uk.gov.ida.notification.shared.logging.ProxyNodeMDCKey;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.notification.shared.istio.IstioHeaders.X_B3_FLAGS;
import static uk.gov.ida.notification.shared.istio.IstioHeaders.X_B3_PARENTSPANID;
import static uk.gov.ida.notification.shared.istio.IstioHeaders.X_B3_SAMPLED;
import static uk.gov.ida.notification.shared.istio.IstioHeaders.X_B3_SPANID;
import static uk.gov.ida.notification.shared.istio.IstioHeaders.X_B3_TRACEID;
import static uk.gov.ida.notification.shared.istio.IstioHeaders.X_OT_SPAN_CONTEXT;
import static uk.gov.ida.notification.shared.istio.IstioHeaders.X_REQUEST_ID;

public class IstioHeaderTest extends EidasSamlParserAppRuleTestBase {

    @Test
    public void headersShouldPersist() throws Exception {
        AuthnRequest authnRequest = ObjectUtils.createSamlObject(AuthnRequest.class);
        Issuer issuer = ObjectUtils.createSamlObject(Issuer.class);
        issuer.setValue("issuer");
        authnRequest.setID("request_id");
        authnRequest.setIssuer(issuer);
        authnRequest.setDestination("destination");
        authnRequest.setIssueInstant(new DateTime(2019, 02, 28, 9, 54));

        String SOME_RANDOM_HEADER = "some-random-header";

        EidasSamlParserRequest request = new EidasSamlParserRequest(Base64.encodeAsString(ObjectUtils.toString(authnRequest)));
        Response response = eidasSamlParserAppRule.target("/eidasAuthnRequest")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header(X_REQUEST_ID, X_REQUEST_ID)
                .header(X_B3_TRACEID, X_B3_TRACEID)
                .header(X_B3_SPANID, X_B3_SPANID)
                .header(X_B3_PARENTSPANID, X_B3_PARENTSPANID)
                .header(X_B3_SAMPLED, X_B3_SAMPLED)
                .header(X_B3_FLAGS, X_B3_FLAGS)
                .header(X_OT_SPAN_CONTEXT, X_OT_SPAN_CONTEXT)
                .header(ProxyNodeMDCKey.PROXY_NODE_JOURNEY_ID.name(), ProxyNodeMDCKey.PROXY_NODE_JOURNEY_ID.name())
                .header(SOME_RANDOM_HEADER, SOME_RANDOM_HEADER)
                .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

        assertThat(response.getHeaders().getFirst(X_REQUEST_ID)).isEqualTo(X_REQUEST_ID);
        assertThat(response.getHeaders().getFirst(X_B3_TRACEID)).isEqualTo(X_B3_TRACEID);
        assertThat(response.getHeaders().getFirst(X_B3_SPANID)).isEqualTo(X_B3_SPANID);
        assertThat(response.getHeaders().getFirst(X_B3_PARENTSPANID)).isEqualTo(X_B3_PARENTSPANID);
        assertThat(response.getHeaders().getFirst(X_B3_SAMPLED)).isEqualTo(X_B3_SAMPLED);
        assertThat(response.getHeaders().getFirst(X_B3_FLAGS)).isEqualTo(X_B3_FLAGS);
        assertThat(response.getHeaders().getFirst(X_OT_SPAN_CONTEXT)).isEqualTo(X_OT_SPAN_CONTEXT);
        assertThat(response.getHeaders().containsKey(SOME_RANDOM_HEADER)).isFalse();
    }
}
