package uk.gov.ida.notification.resources;

import com.codahale.metrics.annotation.ResponseMetered;
import io.dropwizard.jersey.sessions.Session;
import io.dropwizard.views.View;
import io.prometheus.client.Counter;
import org.opensaml.saml.saml2.ecp.RelayState;
import uk.gov.ida.notification.MetricsUtils;
import uk.gov.ida.notification.SamlFormViewBuilder;
import uk.gov.ida.notification.contracts.EidasSamlParserRequest;
import uk.gov.ida.notification.contracts.EidasSamlParserResponse;
import uk.gov.ida.notification.contracts.verifyserviceprovider.AuthnRequestResponse;
import uk.gov.ida.notification.proxy.EidasSamlParserProxy;
import uk.gov.ida.notification.saml.SamlFormMessageType;
import uk.gov.ida.notification.session.GatewaySessionData;
import uk.gov.ida.notification.session.storage.SessionStore;
import uk.gov.ida.notification.shared.Urls;
import uk.gov.ida.notification.shared.logging.IngressEgressLogging;
import uk.gov.ida.notification.shared.logging.ProxyNodeLogger;
import uk.gov.ida.notification.shared.logging.ProxyNodeMDCKey;
import uk.gov.ida.notification.shared.proxy.VerifyServiceProviderProxy;
import uk.gov.ida.notification.validations.ValidBase64Xml;
import uk.gov.ida.notification.views.SamlFormView;

import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.net.URI;

@IngressEgressLogging
@Path(Urls.GatewayUrls.GATEWAY_ROOT)
public class EidasAuthnRequestResource {

    private static final Counter REQUESTS = Counter.build(
            MetricsUtils.LABEL_PREFIX + "_requests_total",
            "Number of eIDAS SAML requests to Verify Proxy Node ")
            .labelNames("issuer")
            .register();

    private static final Counter REQUESTS_SUCCESSFUL = Counter.build(
            MetricsUtils.LABEL_PREFIX + "_successful_requests_total",
            "Number of successful eIDAS SAML requests to Verify Proxy Node")
            .labelNames("issuer")
            .register();

    private final EidasSamlParserProxy eidasSamlParserService;
    private final VerifyServiceProviderProxy vspProxy;
    private final SamlFormViewBuilder samlFormViewBuilder;
    private final SessionStore sessionStorage;

    public EidasAuthnRequestResource(
            EidasSamlParserProxy eidasSamlParserService,
            VerifyServiceProviderProxy vspProxy,
            SamlFormViewBuilder samlFormViewBuilder,
            SessionStore sessionStorage) {
        this.eidasSamlParserService = eidasSamlParserService;
        this.vspProxy = vspProxy;
        this.samlFormViewBuilder = samlFormViewBuilder;
        this.sessionStorage = sessionStorage;
    }

    @GET
    @Path(Urls.GatewayUrls.GATEWAY_EIDAS_AUTHN_REQUEST_REDIRECT_PATH)
    @ResponseMetered
    public View handleRedirectBinding(
            @QueryParam(SamlFormMessageType.SAML_REQUEST) @ValidBase64Xml String encodedEidasAuthnRequest,
            @QueryParam("RelayState") String relayState,
            @Session HttpSession session) {
        return handleAuthnRequest(encodedEidasAuthnRequest, relayState, session);
    }

    @POST
    @Path(Urls.GatewayUrls.GATEWAY_EIDAS_AUTHN_REQUEST_POST_PATH)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @ResponseMetered
    public View handlePostBinding(
            @FormParam(SamlFormMessageType.SAML_REQUEST) @ValidBase64Xml String encodedEidasAuthnRequest,
            @FormParam(RelayState.DEFAULT_ELEMENT_LOCAL_NAME) String eidasRelayState,
            @Session HttpSession session) {
        return handleAuthnRequest(encodedEidasAuthnRequest, eidasRelayState, session);
    }

    private View handleAuthnRequest(String encodedEidasAuthnRequest, String eidasRelayState, HttpSession session) {
        final String sessionId = session.getId();
        final EidasSamlParserResponse eidasSamlParserResponse = parseEidasRequest(encodedEidasAuthnRequest, sessionId);
        final String issuerEntityId = eidasSamlParserResponse.getIssuerEntityId();
        REQUESTS.labels(issuerEntityId).inc();
        final AuthnRequestResponse vspResponse = generateHubRequestWithVsp(sessionId);

        sessionStorage.createOrUpdateSession(
            sessionId,
            new GatewaySessionData(
                eidasSamlParserResponse,
                vspResponse,
                eidasRelayState
            )
        );

        ProxyNodeLogger.addContext(ProxyNodeMDCKey.EIDAS_REQUEST_ID, eidasSamlParserResponse.getRequestId());
        ProxyNodeLogger.addContext(ProxyNodeMDCKey.EIDAS_ISSUER, issuerEntityId);
        ProxyNodeLogger.addContext(ProxyNodeMDCKey.EIDAS_DESTINATION, eidasSamlParserResponse.getAssertionConsumerServiceLocation());
        ProxyNodeLogger.addContext(ProxyNodeMDCKey.HUB_REQUEST_ID, vspResponse.getRequestId());
        ProxyNodeLogger.addContext(ProxyNodeMDCKey.HUB_URL, vspResponse.getSsoLocation().toString());
        ProxyNodeLogger.info("Authn requests received from ESP and VSP");
        SamlFormView samlFormView = buildSamlFormView(vspResponse, (String) session.getAttribute(ProxyNodeMDCKey.PROXY_NODE_JOURNEY_ID.name()));
        REQUESTS_SUCCESSFUL.labels(issuerEntityId).inc();
        return samlFormView;
    }

    private EidasSamlParserResponse parseEidasRequest(String encodedEidasAuthnRequest, String sessionId) {
        return eidasSamlParserService.parse(new EidasSamlParserRequest(encodedEidasAuthnRequest), sessionId);
    }

    private AuthnRequestResponse generateHubRequestWithVsp(String sessionId) {
        return vspProxy.generateAuthnRequest(sessionId);
    }

    private SamlFormView buildSamlFormView(AuthnRequestResponse vspResponse, String relayState) {
        URI hubUrl = vspResponse.getSsoLocation();
        String samlRequest = vspResponse.getSamlRequest();
        return samlFormViewBuilder.buildRequest(hubUrl.toString(), samlRequest, relayState);
    }
}
