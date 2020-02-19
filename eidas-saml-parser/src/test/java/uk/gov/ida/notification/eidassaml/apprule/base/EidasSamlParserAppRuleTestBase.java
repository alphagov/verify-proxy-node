package uk.gov.ida.notification.eidassaml.apprule.base;

import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit.DropwizardClientRule;
import org.junit.ClassRule;
import org.junit.rules.RuleChain;
import org.opensaml.saml.saml2.core.AuthnRequest;
import uk.gov.ida.notification.apprule.rules.AbstractSamlAppRuleTestBase;
import uk.gov.ida.notification.apprule.rules.TestMetatronResource;
import uk.gov.ida.notification.contracts.EidasSamlParserRequest;
import uk.gov.ida.notification.eidassaml.apprule.rules.EidasSamlParserAppRule;
import uk.gov.ida.notification.saml.SamlObjectMarshaller;
import uk.gov.ida.notification.saml.SamlObjectSigner;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URISyntaxException;
import java.util.Base64;

public class EidasSamlParserAppRuleTestBase extends AbstractSamlAppRuleTestBase {

    private static final SamlObjectMarshaller SAML_OBJECT_MARSHALLER = new SamlObjectMarshaller();
    protected static final SamlObjectSigner SAML_OBJECT_SIGNER = createSamlObjectSigner();
    private static final DropwizardClientRule metadataClientRule = createTestMetadataClientRule();
    private static final DropwizardClientRule metatronService = createInitialisedClientRule(new TestMetatronResource());
    private static final EidasSamlParserAppRule eidasSamlParserAppRule = createEidasSamlParserRule(metadataClientRule);

    @ClassRule
    public static final RuleChain orderedRules = RuleChain
            .outerRule(metadataClientRule)
            .around(metatronService)
            .around(eidasSamlParserAppRule);

    protected static Response postEidasAuthnRequest(AuthnRequest authnRequest) throws URISyntaxException {
        return eidasSamlParserAppRule
                .target("/eidasAuthnRequest")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(createEspRequest(authnRequest), MediaType.APPLICATION_JSON_TYPE));
    }

    protected Response postBlankEidasAuthnRequest() throws URISyntaxException {
        final String eidasAuthnRequest = Base64.getEncoder().encodeToString("".getBytes());
        final EidasSamlParserRequest request = new EidasSamlParserRequest(eidasAuthnRequest);

        return eidasSamlParserAppRule.target("/eidasAuthnRequest")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));
    }

    protected static EidasSamlParserRequest createEspRequest(AuthnRequest authnRequest) {
        return new EidasSamlParserRequest(createSamlBase64EncodedRequest(authnRequest));
    }

    protected static EidasSamlParserAppRule createEidasSamlParserRule(DropwizardClientRule metadataClientRule) {
        return new EidasSamlParserAppRule(
                ConfigOverride.config("proxyNodeAuthnRequestUrl", "http://proxy-node/eidasAuthnRequest"),
                ConfigOverride.config("metatronUri", metatronService.baseUri().toString())
        )
        {
            @Override
            protected void before() {
                super.before();
            }
        };
    }

    private static String createSamlBase64EncodedRequest(AuthnRequest authnRequest) {
        return Base64.getEncoder().encodeToString(SAML_OBJECT_MARSHALLER.transformToString(authnRequest).getBytes());
    }
}
