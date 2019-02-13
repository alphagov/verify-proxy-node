package uk.gov.ida.notification.resources;

import io.dropwizard.jersey.sessions.Session;
import io.dropwizard.views.View;
import org.opensaml.saml.saml2.core.Response;
import uk.gov.ida.notification.SamlFormViewBuilder;
import uk.gov.ida.notification.proxy.TranslatorProxy;
import uk.gov.ida.notification.contracts.HubResponseTranslatorRequest;
import uk.gov.ida.notification.exceptions.hubresponse.HubResponseException;
import uk.gov.ida.notification.exceptions.hubresponse.InvalidHubResponseException;
import uk.gov.ida.notification.saml.HubResponseContainer;
import uk.gov.ida.notification.saml.SamlFormMessageType;
import uk.gov.ida.notification.saml.validation.HubResponseValidator;
import uk.gov.ida.notification.shared.Urls;

import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import java.util.logging.Logger;

@Path("/SAML2/SSO/Response")
public class HubResponseResource {
    private static final Logger LOG = Logger.getLogger(HubResponseResource.class.getName());

    private final SamlFormViewBuilder samlFormViewBuilder;
    private final String connectorNodeUrl;
    private final TranslatorService translatorService;
    private HubResponseValidator hubResponseValidator;

    public HubResponseResource(
            SamlFormViewBuilder samlFormViewBuilder,
            String connectorNodeUrl,
            TranslatorService translatorService,
            HubResponseValidator hubResponseValidator) {
        this.samlFormViewBuilder = samlFormViewBuilder;
        this.connectorNodeUrl = connectorNodeUrl;
        this.translatorService = translatorService;
        this.hubResponseValidator = hubResponseValidator;
    }

    @POST
    @Path("/POST")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public View hubResponse(
            @FormParam(SamlFormMessageType.SAML_RESPONSE) Response encryptedHubResponse,
            @FormParam("RelayState") String relayState,
            @Session HttpSession session) {

        try {
            String expectedRequestId = (String) session.getAttribute("gateway_request_id");
            hubResponseValidator.validate(encryptedHubResponse);

            if (!expectedRequestId.contains(encryptedHubResponse.getInResponseTo())) {
                throw new InvalidHubResponseException(
                    String.format("Received a Response from Hub for an AuthnRequest we have not seen (ID: %s)", encryptedHubResponse.getInResponseTo()));
            }

            HubResponseContainer hubResponseContainer = HubResponseContainer.from(
                    hubResponseValidator.getValidatedResponse(),
                    hubResponseValidator.getValidatedAssertions()
            );

            logHubResponse(hubResponseContainer);
            Response eidasResponse = translatorService.getTranslatedResponse(encryptedHubResponse);
            logEidasResponse(eidasResponse);

            return samlFormViewBuilder.buildResponse(
                connectorNodeUrl,
                eidasResponse,
                "Post eIDAS Response SAML to Connector Node",
                relayState
            );
        } catch (Throwable e) {
            throw new HubResponseException(e, encryptedHubResponse);
        }
    }

    private void logHubResponse(HubResponseContainer hubResponseContainer) {
        LOG.info("[Hub Response] ID: " + hubResponseContainer.getHubResponse().getResponseId());
        LOG.info("[Hub Response] In response to: " + hubResponseContainer.getHubResponse().getInResponseTo());
        LOG.info("[Hub Response] Provided level of assurance: " + hubResponseContainer.getAuthnAssertion().getProvidedLoa());
    }

    private void logEidasResponse(Response eidasResponse) {
        LOG.info("[eIDAS Response] ID: " + eidasResponse.getID());
        LOG.info("[eIDAS Response] In response to: " + eidasResponse.getInResponseTo());
    }

}
