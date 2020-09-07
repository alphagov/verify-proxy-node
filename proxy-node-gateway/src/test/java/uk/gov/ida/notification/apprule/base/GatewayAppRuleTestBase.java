package uk.gov.ida.notification.apprule.base;

import com.github.fppt.jedismock.RedisServer;
import org.opensaml.saml.saml2.core.AuthnRequest;
import uk.gov.ida.Base64;
import uk.gov.ida.notification.apprule.rules.AbstractSamlAppRuleTestBase;
import uk.gov.ida.notification.helpers.EidasAuthnRequestBuilder;
import uk.gov.ida.notification.saml.SamlFormMessageType;
import uk.gov.ida.notification.saml.SamlObjectMarshaller;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.atomic.AtomicInteger;

public class GatewayAppRuleTestBase extends AbstractSamlAppRuleTestBase {

    private static final String CONNECTOR_NODE_ENTITY_ID = "http://connector-node:8080/ConnectorResponderMetadata";
    private static final SamlObjectMarshaller SAML_OBJECT_MARSHALLER = new SamlObjectMarshaller();
    private static final AtomicInteger REDIS_USERS = new AtomicInteger(0);

    private static RedisServer redisServer = null;

    protected Response postEidasAuthnRequest(AuthnRequest eidasAuthnRequest, Client client, int port) {
        String encodedRequest = Base64.encodeToString(SAML_OBJECT_MARSHALLER.transformToString(eidasAuthnRequest));
        Form postForm = new Form().param(SamlFormMessageType.SAML_REQUEST, encodedRequest).param("RelayState", "relay-state");
        String url = String.format("http://localhost:%d/SAML2/SSO/POST", port);
        return client.target(url).request().post(Entity.form(postForm));
    }

    protected Response postInvalidEidasAuthnRequest(AuthnRequest eidasAuthnRequest, Client client) {
        String encodedRequest = "not-a-base64-xml-opening-tag" + Base64.encodeToString(SAML_OBJECT_MARSHALLER.transformToString(eidasAuthnRequest));
        Form postForm = new Form().param(SamlFormMessageType.SAML_REQUEST, encodedRequest).param("RelayState", "relay-state");
        return client.target("/SAML2/SSO/POST").request().post(Entity.form(postForm));
    }

    protected Response redirectEidasAuthnRequest(AuthnRequest eidasAuthnRequest, Client client) throws URISyntaxException {
        String encodedRequest = Base64.encodeToString(SAML_OBJECT_MARSHALLER.transformToString(eidasAuthnRequest));
        return client.target("/SAML2/SSO/Redirect")
                .queryParam(SamlFormMessageType.SAML_REQUEST, encodedRequest)
                .queryParam("RelayState", "relay-state")
                .request()
                .get();
    }

    protected AuthnRequest buildAuthnRequestWithoutId() throws Exception {
        return new EidasAuthnRequestBuilder()
            .withIssuer(CONNECTOR_NODE_ENTITY_ID)
            .withRequestId(null)
            .build();
    }

    protected AuthnRequest buildAuthnRequest() throws Exception {
        return new EidasAuthnRequestBuilder()
                .withIssuer(CONNECTOR_NODE_ENTITY_ID)
                .withRandomRequestId()
                .build();
    }

    protected static String setupTestRedis() {
        synchronized (REDIS_USERS) {
            if (redisServer == null) {
                try {
                    redisServer = RedisServer.newRedisServer();
                    redisServer.start();
                } catch (IOException e) {
                    throw new RuntimeException();
                }
            }

            REDIS_USERS.incrementAndGet();
        }

        return "redis://" + redisServer.getHost() + ":" + redisServer.getBindPort() + "/";
    }

    protected static void killTestRedis() {
        synchronized (REDIS_USERS) {
            if (REDIS_USERS.decrementAndGet() == 0) {
                redisServer.stop();
                redisServer = null;
            }
        }
    }
}
