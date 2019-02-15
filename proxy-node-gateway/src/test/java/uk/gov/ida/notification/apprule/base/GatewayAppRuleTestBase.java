package uk.gov.ida.notification.apprule.base;

import io.dropwizard.testing.ConfigOverride;
import org.glassfish.jersey.internal.util.Base64;
import org.junit.ClassRule;
import org.junit.Rule;
import org.opensaml.core.config.InitializationService;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.security.credential.Credential;
import uk.gov.ida.notification.VerifySamlInitializer;
import uk.gov.ida.notification.apprule.rules.EidasSamlParserClientRule;
import uk.gov.ida.notification.apprule.rules.GatewayAppRule;
import uk.gov.ida.notification.apprule.rules.TranslatorClientRule;
import uk.gov.ida.notification.apprule.rules.VerifyServiceProviderClientRule;
import uk.gov.ida.notification.saml.SamlFormMessageType;
import uk.gov.ida.notification.saml.SamlObjectMarshaller;
import uk.gov.ida.notification.saml.SamlObjectSigner;
import uk.gov.ida.saml.core.test.TestCredentialFactory;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.net.URISyntaxException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.fail;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PRIVATE_SIGNING_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PUBLIC_SIGNING_CERT;

public class GatewayAppRuleTestBase {

    private Map<String, NewCookie> cookies;

    protected static final String CONNECTOR_NODE_ENTITY_ID = "http://connector-node:8080/ConnectorResponderMetadata";

    @ClassRule
    public static final TranslatorClientRule translatorClientRule;

    @ClassRule
    public static final EidasSamlParserClientRule espClientRule = new EidasSamlParserClientRule();

    @ClassRule
    public static final VerifyServiceProviderClientRule vspClientRule = new VerifyServiceProviderClientRule();

    static {
        try {
            InitializationService.initialize();
            VerifySamlInitializer.init();
            translatorClientRule = new TranslatorClientRule();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected final SamlObjectMarshaller marshaller = new SamlObjectMarshaller();
    protected final Credential countrySigningCredential = new TestCredentialFactory(TEST_RP_PUBLIC_SIGNING_CERT, TEST_RP_PRIVATE_SIGNING_KEY).getSigningCredential();
    protected final SamlObjectSigner samlObjectSigner = new SamlObjectSigner(
            countrySigningCredential.getPublicKey(),
            countrySigningCredential.getPrivateKey(),
            TEST_RP_PUBLIC_SIGNING_CERT);

    @Rule
    public GatewayAppRule proxyNodeAppRule = new GatewayAppRule(
            ConfigOverride.config("eidasSamlParserService.url", espClientRule.baseUri().toString()),
            ConfigOverride.config("verifyServiceProviderService.url", vspClientRule.baseUri().toString()),
            ConfigOverride.config("translatorService.url", translatorClientRule.baseUri() + "/translator/SAML2/SSO/Response")
    );

    protected Response postEidasAuthnRequest(AuthnRequest eidasAuthnRequest) {
        System.out.println(marshaller.transformToString(eidasAuthnRequest));
        String encodedRequest = Base64.encodeAsString(marshaller.transformToString(eidasAuthnRequest));
        Form postForm = new Form().param(SamlFormMessageType.SAML_REQUEST, encodedRequest);

        Response response = null;

        try {
            response = proxyNodeAppRule.target("/SAML2/SSO/POST").request().post(Entity.form(postForm));
        } catch (URISyntaxException e) {
            fail(e);
        }

        if (response != null) {
            cookies = response.getCookies();
        }

        return response;
    }

    protected Response redirectEidasAuthnRequest(AuthnRequest eidasAuthnRequest) {
        String encodedRequest = Base64.encodeAsString(marshaller.transformToString(eidasAuthnRequest));

        try {
            return proxyNodeAppRule.target("/SAML2/SSO/Redirect")
                    .queryParam(SamlFormMessageType.SAML_REQUEST, encodedRequest)
                    .request()
                    .get();
        } catch (URISyntaxException e) {
            fail(e);
            return null;
        }
    }
}
