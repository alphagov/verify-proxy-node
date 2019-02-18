package uk.gov.ida.notification.apprule.base;

import org.glassfish.jersey.internal.util.Base64;
import org.opensaml.core.config.InitializationService;
import org.opensaml.saml.saml2.core.AuthnRequest;
import uk.gov.ida.notification.VerifySamlInitializer;
import uk.gov.ida.notification.apprule.rules.GatewayAppRule;
import uk.gov.ida.notification.saml.SamlFormMessageType;
import uk.gov.ida.notification.saml.SamlObjectMarshaller;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;
import java.net.URISyntaxException;

public class GatewayAppRuleTestBase {

    protected static final String CONNECTOR_NODE_ENTITY_ID = "http://connector-node:8080/ConnectorResponderMetadata";

    static {
        try {
            InitializationService.initialize();
            VerifySamlInitializer.init();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private final SamlObjectMarshaller marshaller = new SamlObjectMarshaller();

    protected Response postEidasAuthnRequest(AuthnRequest eidasAuthnRequest, GatewayAppRule proxyNodeAppRule) throws URISyntaxException {
        String encodedRequest = Base64.encodeAsString(marshaller.transformToString(eidasAuthnRequest));
        Form postForm = new Form().param(SamlFormMessageType.SAML_REQUEST, encodedRequest);
        return proxyNodeAppRule.target("/SAML2/SSO/POST").request().post(Entity.form(postForm));
    }

    protected Response redirectEidasAuthnRequest(AuthnRequest eidasAuthnRequest, GatewayAppRule proxyNodeAppRule) throws URISyntaxException {
        String encodedRequest = Base64.encodeAsString(marshaller.transformToString(eidasAuthnRequest));
        return proxyNodeAppRule.target("/SAML2/SSO/Redirect")
                .queryParam(SamlFormMessageType.SAML_REQUEST, encodedRequest)
                .request()
                .get();
    }
}
