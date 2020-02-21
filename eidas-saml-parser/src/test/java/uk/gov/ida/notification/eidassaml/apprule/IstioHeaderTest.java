package uk.gov.ida.notification.eidassaml.apprule;

import io.dropwizard.testing.junit.DropwizardClientRule;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.opensaml.saml.saml2.core.AuthnRequest;
import uk.gov.ida.notification.apprule.rules.TestMetadataResource;
import uk.gov.ida.notification.apprule.rules.TestMetatronResource;
import uk.gov.ida.notification.eidassaml.apprule.base.EidasSamlParserAppRuleTestBase;
import uk.gov.ida.notification.eidassaml.apprule.rules.EidasSamlParserAppRule;
import uk.gov.ida.notification.helpers.EidasAuthnRequestBuilder;
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

    private static final String RANDOM_HEADER = "random-header";

    @ClassRule
    public static final DropwizardClientRule metatronClientRule = createInitialisedClientRule(new TestMetatronResource());

    @ClassRule
    public static final EidasSamlParserAppRule eidasSamlParserAppRule = createEidasSamlParserRule(metatronClientRule);

    @ClassRule
    public static final RuleChain orderedRules = RuleChain.outerRule(metatronClientRule).around(eidasSamlParserAppRule);

    @Test
    public void headersShouldPersist() throws Exception {
        AuthnRequest authnRequest = new EidasAuthnRequestBuilder()
                .withIssuer(TestMetadataResource.CONNECTOR_ENTITY_ID)
                .withDestination("http://proxy-node/eidasAuthnRequest")
                .withRandomRequestId()
                .build();

        SAML_OBJECT_SIGNER.sign(authnRequest, "response-id");

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
                .header(RANDOM_HEADER, RANDOM_HEADER)
                .post(Entity.entity(createEspRequest(authnRequest), MediaType.APPLICATION_JSON_TYPE));

        assertThat(response.getHeaders().getFirst(X_REQUEST_ID)).isEqualTo(X_REQUEST_ID);
        assertThat(response.getHeaders().getFirst(X_B3_TRACEID)).isEqualTo(X_B3_TRACEID);
        assertThat(response.getHeaders().getFirst(X_B3_SPANID)).isEqualTo(X_B3_SPANID);
        assertThat(response.getHeaders().getFirst(X_B3_PARENTSPANID)).isEqualTo(X_B3_PARENTSPANID);
        assertThat(response.getHeaders().getFirst(X_B3_SAMPLED)).isEqualTo(X_B3_SAMPLED);
        assertThat(response.getHeaders().getFirst(X_B3_FLAGS)).isEqualTo(X_B3_FLAGS);
        assertThat(response.getHeaders().getFirst(X_OT_SPAN_CONTEXT)).isEqualTo(X_OT_SPAN_CONTEXT);
        assertThat(response.getHeaders().containsKey(RANDOM_HEADER)).isFalse();
    }
}
