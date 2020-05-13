package uk.gov.ida.notification.resources;

import io.dropwizard.jersey.sessions.Session;
import io.dropwizard.views.View;
import io.prometheus.client.Counter;
import uk.gov.ida.notification.MetricsUtils;
import uk.gov.ida.notification.SamlFormViewBuilder;
import uk.gov.ida.notification.contracts.HubResponseTranslatorRequest;
import uk.gov.ida.notification.proxy.TranslatorProxy;
import uk.gov.ida.notification.saml.SamlFormMessageType;
import uk.gov.ida.notification.session.GatewaySessionData;
import uk.gov.ida.notification.session.storage.SessionStore;
import uk.gov.ida.notification.shared.Urls;
import uk.gov.ida.notification.shared.logging.IngressEgressLogging;
import uk.gov.ida.notification.shared.logging.ProxyNodeLogger;
import uk.gov.ida.notification.validations.ValidBase64Xml;
import uk.gov.ida.notification.views.SamlFormView;

import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

@IngressEgressLogging
@Path(Urls.GatewayUrls.GATEWAY_ROOT)
public class HubResponseResource {
    // TODO multi-country PN will need a way to distinguish these counters per country
    private static final Counter RESPONSES = Counter.build(
            MetricsUtils.LABEL_PREFIX + "_responses_total",
            "Number of eIDAS SAML responses to Verify Proxy Node")
            .labelNames("issuer")
            .register();
    private static final Counter RESPONSES_SUCCESSFUL = Counter.build(
            MetricsUtils.LABEL_PREFIX + "_successful_responses_total",
            "Number of successful eIDAS SAML responses To Verify Proxy Node")
            .labelNames("issuer")
            .register();

    static final String LEVEL_OF_ASSURANCE = "LEVEL_2";

    private final SamlFormViewBuilder samlFormViewBuilder;
    private final TranslatorProxy translatorProxy;
    private final SessionStore sessionStorage;

    public HubResponseResource(
            SamlFormViewBuilder samlFormViewBuilder,
            TranslatorProxy translatorProxy,
            SessionStore sessionStorage) {
        this.samlFormViewBuilder = samlFormViewBuilder;
        this.translatorProxy = translatorProxy;
        this.sessionStorage = sessionStorage;
    }

    @POST
    @Path(Urls.GatewayUrls.GATEWAY_HUB_RESPONSE_PATH)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public View hubResponse(
        @FormParam(SamlFormMessageType.SAML_RESPONSE) @ValidBase64Xml String hubResponse,
        @FormParam("RelayState") String relayState,
        @Session HttpSession session) {
        GatewaySessionData sessionData = sessionStorage.getSession(session.getId());
        String issuerEntityId = sessionData.getEidasIssuerEntityId();
        RESPONSES.labels(issuerEntityId).inc();

        ProxyNodeLogger.info("Retrieved GatewaySessionData");

        HubResponseTranslatorRequest translatorRequest = new HubResponseTranslatorRequest(
            hubResponse,
            sessionData.getHubRequestId(),
            sessionData.getEidasRequestId(),
            LEVEL_OF_ASSURANCE,
            UriBuilder.fromUri(sessionData.getEidasDestination()).build(),
            UriBuilder.fromUri(issuerEntityId).build()
        );

        String eidasResponse = translatorProxy.getTranslatedHubResponse(translatorRequest, session.getId());
        ProxyNodeLogger.info("Received eIDAS response from Translator");

        SamlFormView samlFormView = samlFormViewBuilder.buildResponse(
                sessionData.getEidasDestination(),
                eidasResponse,
                sessionData.getEidasRelayState()
        );
        RESPONSES_SUCCESSFUL.labels(issuerEntityId).inc();
        return samlFormView;
    }
}
