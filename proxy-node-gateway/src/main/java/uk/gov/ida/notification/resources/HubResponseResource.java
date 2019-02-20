package uk.gov.ida.notification.resources;

import io.dropwizard.jersey.sessions.Session;
import io.dropwizard.views.View;
import uk.gov.ida.notification.SamlFormViewBuilder;
import uk.gov.ida.notification.exceptions.SessionAttributeException;
import uk.gov.ida.notification.proxy.TranslatorProxy;
import uk.gov.ida.notification.contracts.HubResponseTranslatorRequest;
import uk.gov.ida.notification.saml.SamlFormMessageType;
import uk.gov.ida.notification.shared.Urls;

import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.util.logging.Logger;

import static uk.gov.ida.notification.session.SessionKeys.SESSION_KEY_EIDAS_CONNECTOR_PUBLIC_CERT;
import static uk.gov.ida.notification.session.SessionKeys.SESSION_KEY_EIDAS_DESTINATION;
import static uk.gov.ida.notification.session.SessionKeys.SESSION_KEY_EIDAS_RELAY_STATE;
import static uk.gov.ida.notification.session.SessionKeys.SESSION_KEY_EIDAS_REQUEST_ID;
import static uk.gov.ida.notification.session.SessionKeys.SESSION_KEY_HUB_REQUEST_ID;

@Path(Urls.GatewayUrls.GATEWAY_ROOT)
public class HubResponseResource {
    private static final Logger LOG = Logger.getLogger(HubResponseResource.class.getName());
    public static final String LEVEL_OF_ASSURANCE = "LEVEL_2";
    public static final String SUBMIT_TEXT = "Post eIDAS Response SAML to Connector Node";

    private final SamlFormViewBuilder samlFormViewBuilder;
    private final TranslatorProxy translatorProxy;

    public HubResponseResource(
            SamlFormViewBuilder samlFormViewBuilder,
            TranslatorProxy translatorProxy) {
        this.samlFormViewBuilder = samlFormViewBuilder;
        this.translatorProxy = translatorProxy;
    }

    @POST
    @Path(Urls.GatewayUrls.GATEWAY_HUB_RESPONSE_PATH)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public View hubResponse(
        @FormParam(SamlFormMessageType.SAML_RESPONSE) String hubResponse,
        @FormParam("RelayState") String relayState,
        @Session HttpSession session) {

        String hubRequestId;
        String eidasRequestId;
        String connectorEncrpytionCredential;
        String connectorNodeUrl;
        String eidasRelayState;

        try {
            hubRequestId = session.getAttribute(SESSION_KEY_HUB_REQUEST_ID).toString();
            eidasRequestId = session.getAttribute(SESSION_KEY_EIDAS_REQUEST_ID).toString();
            connectorEncrpytionCredential = session.getAttribute(SESSION_KEY_EIDAS_CONNECTOR_PUBLIC_CERT).toString();
            connectorNodeUrl = session.getAttribute(SESSION_KEY_EIDAS_DESTINATION).toString();
            eidasRelayState = session.getAttribute(SESSION_KEY_EIDAS_RELAY_STATE).toString();
        } catch (NullPointerException e) {
            throw new SessionAttributeException(e);
        }

        LOG.info(String.format("[HUB Response] received for hub authn request ID '%s', eIDAS authn request ID '%s'", hubRequestId, eidasRequestId));

        HubResponseTranslatorRequest translatorRequest = new HubResponseTranslatorRequest(
            hubResponse,
            hubRequestId,
            eidasRequestId,
            LEVEL_OF_ASSURANCE,
            UriBuilder.fromUri(connectorNodeUrl).build(),
            connectorEncrpytionCredential
        );

        String eidasResponse = translatorProxy.getTranslatedResponse(translatorRequest);

        LOG.info(String.format("[eIDAS Response] received for hub authn request ID '%s', eIDAS authn request ID '%s'", hubRequestId, eidasRequestId));

        return samlFormViewBuilder.buildResponse(
            connectorNodeUrl,
            eidasResponse,
            SUBMIT_TEXT,
            eidasRelayState
        );
    }
}
