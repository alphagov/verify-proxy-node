package uk.gov.ida.notification.resources;

import io.dropwizard.jersey.sessions.Session;
import io.dropwizard.views.View;
import org.apache.commons.lang.StringUtils;
import org.opensaml.saml.saml2.ecp.RelayState;
import uk.gov.ida.notification.EidasSamlParserService;
import uk.gov.ida.notification.SamlFormViewBuilder;
import uk.gov.ida.notification.VSPService;
import uk.gov.ida.notification.dto.EidasSamlParserRequest;
import uk.gov.ida.notification.dto.EidasSamlParserResponse;
import uk.gov.ida.notification.saml.SamlFormMessageType;
import uk.gov.ida.notification.dto.VSPAuthnRequestResponse;
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
import java.util.logging.Logger;

@Path("/SAML2/SSO")
public class EidasAuthnRequestResourceV2 {

    private final Logger log = Logger.getLogger(getClass().getName());
    public static final String SESSION_KEY_EIDAS_REQUEST_ID = "eidas_request_id";
    public static final String SESSION_KEY_EIDAS_CONNECTOR_PUBLIC_KEY = "eidas_connector_public_key";
    public static final String SESSION_KEY_EIDAS_DESTINATION = "eidas_destination";
    public static final String SESSION_KEY_HUB_REQUEST_ID = "hub_request_id";
    public static final String SUBMIT_BUTTON_TEXT = "Post Verify Authn Request to Hub";

    private final EidasSamlParserService eidasSamlParserService;
    private final VSPService vspService;
    private final SamlFormViewBuilder samlFormViewBuilder;

    public EidasAuthnRequestResourceV2(
            EidasSamlParserService eidasSamlParserService,
            VSPService vspService,
            SamlFormViewBuilder samlFormViewBuilder) {
        this.eidasSamlParserService = eidasSamlParserService;
        this.vspService = vspService;
        this.samlFormViewBuilder = samlFormViewBuilder;
    }

    @GET
    @Path("/Redirect")
    public View handleRedirectBinding(
            @QueryParam(SamlFormMessageType.SAML_REQUEST) String encodedEidasAuthnRequest,
            @QueryParam("RelayState") String relayState,
            @Session HttpSession session) {
        return handlePostBinding(encodedEidasAuthnRequest, relayState, session);
    }

    @POST
    @Path("/POST")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public View handlePostBinding(
            @FormParam(SamlFormMessageType.SAML_REQUEST) String encodedEidasAuthnRequest,
            @FormParam(RelayState.DEFAULT_ELEMENT_LOCAL_NAME) String eidasRelayState,
            @Session HttpSession session) {
        return handleAuthnRequest(encodedEidasAuthnRequest, eidasRelayState, session);
    }

    private View handleAuthnRequest(String encodedEidasAuthnRequest, String eidasRelayState, HttpSession session) {
        final EidasSamlParserResponse eidasSamlParserResponse = parseEidasRequest(encodedEidasAuthnRequest);
        VSPAuthnRequestResponse vspResponse = generateHubRequestWithVSP();
        logAuthnRequestInformation(session, eidasSamlParserResponse, vspResponse);
        setResponseDataInSession(session, eidasSamlParserResponse, vspResponse);
        return buildSamlFormView(vspResponse, eidasRelayState);
    }

    private void setResponseDataInSession(HttpSession session, EidasSamlParserResponse eidasSamlParserResponse, VSPAuthnRequestResponse vspResponse) {
        session.setAttribute(SESSION_KEY_EIDAS_REQUEST_ID, eidasSamlParserResponse.getRequestId());
        session.setAttribute(SESSION_KEY_EIDAS_CONNECTOR_PUBLIC_KEY, eidasSamlParserResponse.getConnectorPublicEncryptionKey());
        session.setAttribute(SESSION_KEY_EIDAS_DESTINATION, eidasSamlParserResponse.getDestination());
        session.setAttribute(SESSION_KEY_HUB_REQUEST_ID, vspResponse.getRequestId());
    }

    private EidasSamlParserResponse parseEidasRequest(String encodedEidasAuthnRequest) {
        return eidasSamlParserService.parse(new EidasSamlParserRequest(encodedEidasAuthnRequest));
    }

    private VSPAuthnRequestResponse generateHubRequestWithVSP() {
        return vspService.generateAuthnRequest();
    }

    private SamlFormView buildSamlFormView(VSPAuthnRequestResponse vspResponse, String relayState) {
        URI hubUrl = vspResponse.getSsoLocation();
        String samlRequest = vspResponse.getSamlRequest();
        return samlFormViewBuilder.buildRequest(hubUrl.toString(), samlRequest, SUBMIT_BUTTON_TEXT, relayState);
    }

    private void logAuthnRequestInformation(HttpSession session, EidasSamlParserResponse eidasSamlResponse, VSPAuthnRequestResponse vspResponse) {
        log.info(String.format("[eIDAS AuthnRequest] Session ID: '%s'", session.getId()));
        log.info(String.format("[eIDAS AuthnRequest] eIDAS Request ID: '%s'", eidasSamlResponse.getRequestId()));
        log.info(String.format("[eIDAS AuthnRequest] eIDAS Issuer: '%s'", eidasSamlResponse.getIssuer()));
        log.info(String.format("[eIDAS AuthnRequest] eIDAS Destination: '%s'", eidasSamlResponse.getDestination()));
        log.info(String.format("[eIDAS AuthnRequest] eIDAS Connector Public Key suffix: '%s'", StringUtils.right(eidasSamlResponse.getConnectorPublicEncryptionKey(), 10)));
        log.info(String.format("[Hub AuthnRequest] Hub Request ID: '%s'", vspResponse.getRequestId()));
        log.info(String.format("[Hub AuthnRequest] Hub URL: '%s'", vspResponse.getSsoLocation()));
    }
}
