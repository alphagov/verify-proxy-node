package uk.gov.ida.notification.resources;

import io.dropwizard.views.View;
import uk.gov.ida.notification.SamlFormViewBuilder;
import uk.gov.ida.notification.contracts.HubResponseTranslatorRequest;
import uk.gov.ida.notification.proxy.TranslatorProxy;
import uk.gov.ida.notification.saml.SamlFormMessageType;
import uk.gov.ida.notification.session.GatewaySessionData;
import uk.gov.ida.notification.session.SessionCookieService;
import uk.gov.ida.notification.session.storage.SessionStore;
import uk.gov.ida.notification.shared.Urls;
import uk.gov.ida.notification.shared.logging.IngressEgressLogging;
import uk.gov.ida.notification.shared.logging.ProxyNodeLogger;
import uk.gov.ida.notification.validations.ValidBase64Xml;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.util.Map;
import java.util.logging.Level;

@IngressEgressLogging
@Path(Urls.GatewayUrls.GATEWAY_ROOT)
public class HubResponseResource {

    static final String LEVEL_OF_ASSURANCE = "LEVEL_2";
    static final String SUBMIT_TEXT = "Post eIDAS Response SAML to Connector Node";

    private final SamlFormViewBuilder samlFormViewBuilder;
    private final TranslatorProxy translatorProxy;
    private final SessionStore sessionStorage;
    private final SessionCookieService sessionCookieService;
    private final EntityMetadata entityMetadata;

    public HubResponseResource(
            SamlFormViewBuilder samlFormViewBuilder,
            TranslatorProxy translatorProxy,
            SessionStore sessionStorage,
            SessionCookieService sessionCookieService,
            EntityMetadata entityMetadata) {
        this.samlFormViewBuilder = samlFormViewBuilder;
        this.translatorProxy = translatorProxy;
        this.sessionStorage = sessionStorage;
        this.sessionCookieService = sessionCookieService;
        this.entityMetadata = entityMetadata;
    }

    @POST
    @Path(Urls.GatewayUrls.GATEWAY_HUB_RESPONSE_PATH)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public View hubResponse(
            @FormParam(SamlFormMessageType.SAML_RESPONSE) @ValidBase64Xml String hubResponse,
            @FormParam("RelayState") String relayState,
            @Context HttpServletRequest request) {

        HttpSession session = request.getSession();
        GatewaySessionData sessionData = sessionStorage.getSession(session.getId());
        checkClaimsMatchGatewaySessionData(request, sessionData);
        checkEntityMetadataMatchGatewaySessionData(sessionData);

        ProxyNodeLogger.info("Retrieved GatewaySessionData");

        HubResponseTranslatorRequest translatorRequest = new HubResponseTranslatorRequest(
            hubResponse,
            sessionData.getHubRequestId(),
            sessionData.getEidasRequestId(),
            LEVEL_OF_ASSURANCE,
            UriBuilder.fromUri(sessionData.getEidasDestination()).build(),
            sessionData.getEidasConnectorPublicKey()
        );

        String eidasResponse = translatorProxy.getTranslatedHubResponse(translatorRequest, session.getId());
        ProxyNodeLogger.info("Received eIDAS response from Translator");

        return samlFormViewBuilder.buildResponse(
            sessionData.getEidasDestination(),
            eidasResponse,
            SUBMIT_TEXT,
            sessionData.getEidasRelayState()
        );
    }

    private void checkClaimsMatchGatewaySessionData(HttpServletRequest request, GatewaySessionData sessionData) {
        try {
            Map<String, Object> claims = sessionCookieService.getData(request.getCookies());
            if (!sessionDataMatches(sessionData, claims)) {
                ProxyNodeLogger.warning("sessionData and sessionDataFromCookie are not equal");
            }
        } catch (Exception e) {
            // TODO remove this try/catch block when we migrate to only using the gateway session cookie
            ProxyNodeLogger.logException(e, Level.WARNING, "Error from checkClaimsMatchGatewaySessionData");
        }

    }

    private void checkEntityMetadataMatchGatewaySessionData(GatewaySessionData sessionData) {
        try {
            String destination = entityMetadata.getValue(sessionData.getIssuer(), EntityMetadata.Key.eidasDestination);
            String encryptionCertificate = entityMetadata.getValue(sessionData.getIssuer(), EntityMetadata.Key.encryptionCertificate);

            if (!sessionData.getEidasDestination().equals(destination)) {
                ProxyNodeLogger.warning("sessionData eidasDestination and entityMetaData eidasDestination do not match");
            }
            if (!sessionData.getEidasConnectorPublicKey().equals(encryptionCertificate)) {
                ProxyNodeLogger.warning("sessionData encryptionCertificate and entityMetaData encryptionCertificate do not match");
            }
        } catch (Exception e) {
            // TODO remove this try/catch block when we migrate to only using the gateway session cookie
            ProxyNodeLogger.logException(e, Level.WARNING, "Error from checkEntityMetadataMatchGatewaySessionData");
        }
    }

    private boolean sessionDataMatches(GatewaySessionData sessionData, Map<String, Object> claims) {
        return sessionData.getEidasRequestId().equals(claims.get(GatewaySessionData.Keys.eidasRequestId.name()))
                && sessionData.getHubRequestId().equals(claims.get(GatewaySessionData.Keys.hubRequestId.name()))
                && sessionData.getEidasRelayState().equals(claims.get(GatewaySessionData.Keys.eidasRelayState.name()))
                && sessionData.getIssuer().equals(claims.get(GatewaySessionData.Keys.issuer.name()));
    }
}
