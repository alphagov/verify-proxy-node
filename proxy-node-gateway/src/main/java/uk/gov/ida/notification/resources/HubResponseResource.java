package uk.gov.ida.notification.resources;

import io.dropwizard.jersey.sessions.Session;
import io.dropwizard.views.View;
import uk.gov.ida.notification.SamlFormViewBuilder;
import uk.gov.ida.notification.proxy.TranslatorProxy;
import uk.gov.ida.notification.contracts.HubResponseTranslatorRequest;
import uk.gov.ida.notification.saml.SamlFormMessageType;
import uk.gov.ida.notification.session.GatewaySessionData;
import uk.gov.ida.notification.session.GatewaySessionDataValidator;
import uk.gov.ida.notification.shared.Urls;

import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.util.logging.Logger;


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

        GatewaySessionData sessionData = GatewaySessionDataValidator.getValidatedSessionData(session);

        LOG.info(
            String.format(
                "[HUB Response] received for session '%s', hub authn request ID '%s', eIDAS authn request ID '%s'",
                session.getId(),
                sessionData.getHubRequestId(),
                sessionData.getEidasRequestId()
            )
        );

        HubResponseTranslatorRequest translatorRequest = new HubResponseTranslatorRequest(
            hubResponse,
            sessionData.getHubRequestId(),
            sessionData.getEidasRequestId(),
            LEVEL_OF_ASSURANCE,
            UriBuilder.fromUri(sessionData.getEidasDestination()).build(),
            sessionData.getEidasConnectorPublicKey()
        );

        String eidasResponse = translatorProxy.getTranslatedResponse(translatorRequest);

        LOG.info(
            String.format(
                "[eIDAS Response] received for session '%s', hub authn request ID '%s', eIDAS authn request ID '%s'",
                session.getId(),
                sessionData.getHubRequestId(),
                sessionData.getEidasRequestId()
            )
        );

        return samlFormViewBuilder.buildResponse(
            sessionData.getEidasDestination(),
            eidasResponse,
            SUBMIT_TEXT,
            sessionData.getEidasRelayState()
        );
    }
}
