package uk.gov.ida.notification.eidassaml.apprule.base;

import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit.DropwizardClientRule;
import org.opensaml.saml.saml2.core.AuthnRequest;
import uk.gov.ida.notification.apprule.rules.AbstractSamlAppRuleTestBase;
import uk.gov.ida.notification.apprule.rules.AppRule;
import uk.gov.ida.notification.contracts.EidasSamlParserRequest;
import uk.gov.ida.notification.eidassaml.EidasSamlParserApplication;
import uk.gov.ida.notification.eidassaml.EidasSamlParserConfiguration;
import uk.gov.ida.notification.saml.SamlObjectMarshaller;
import uk.gov.ida.notification.saml.SamlObjectSigner;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URISyntaxException;
import java.util.Base64;

public class EidasSamlParserAppRuleTestBase extends AbstractSamlAppRuleTestBase {

    protected static final SamlObjectSigner SAML_OBJECT_SIGNER = createSamlObjectSigner();
    private static final SamlObjectMarshaller SAML_OBJECT_MARSHALLER = new SamlObjectMarshaller();

    protected static Response postEidasAuthnRequest(AppRule<EidasSamlParserConfiguration> eidasSamlParserAppRule, AuthnRequest authnRequest) throws URISyntaxException {
        return eidasSamlParserAppRule
                .target("/eidasAuthnRequest")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(createEspRequest(authnRequest), MediaType.APPLICATION_JSON_TYPE));
    }

    protected Response postBlankEidasAuthnRequest(AppRule<EidasSamlParserConfiguration> eidasSamlParserAppRule) throws URISyntaxException {
        final String eidasAuthnRequest = Base64.getEncoder().encodeToString("".getBytes());
        final EidasSamlParserRequest request = new EidasSamlParserRequest(eidasAuthnRequest);

        return eidasSamlParserAppRule.target("/eidasAuthnRequest")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));
    }

    protected static EidasSamlParserRequest createEspRequest(AuthnRequest authnRequest) {
        return new EidasSamlParserRequest(createSamlBase64EncodedRequest(authnRequest));
    }

    protected static AppRule<EidasSamlParserConfiguration> createEidasSamlParserRule(DropwizardClientRule metatronClientRule) {
        return new AppRule<>(
                EidasSamlParserApplication.class,
                ConfigOverride.config("proxyNodeAuthnRequestUrl", "http://proxy-node/eidasAuthnRequest"),
                ConfigOverride.config("metatronUri", metatronClientRule.baseUri().toString()),
                ConfigOverride.config("replayChecker.redisUrl", "")
        ) {
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
