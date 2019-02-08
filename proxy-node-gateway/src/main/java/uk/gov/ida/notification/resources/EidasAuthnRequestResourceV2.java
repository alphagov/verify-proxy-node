package uk.gov.ida.notification.resources;

import io.dropwizard.jersey.sessions.Session;
import io.dropwizard.views.View;
import org.opensaml.saml.saml2.ecp.RelayState;
import uk.gov.ida.notification.EidasSamlParserService;
import uk.gov.ida.notification.GatewayConfiguration;
import uk.gov.ida.notification.SamlFormViewBuilder;
import uk.gov.ida.notification.VSPService;
import uk.gov.ida.notification.eidassaml.RequestDto;
import uk.gov.ida.notification.eidassaml.ResponseDto;
import uk.gov.ida.notification.saml.SamlFormMessageType;
import uk.gov.ida.notification.verifyserviceprovider.RequestGenerationBody;
import uk.gov.ida.notification.verifyserviceprovider.RequestResponseBody;
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

    private final EidasSamlParserService eidasSamlParserService;
    private final VSPService vspService;
    private final SamlFormViewBuilder samlFormViewBuilder;
    private final GatewayConfiguration configuration;

    public EidasAuthnRequestResourceV2(
            EidasSamlParserService eidasSamlParserService,
            VSPService vspService,
            SamlFormViewBuilder samlFormViewBuilder,
            GatewayConfiguration configuration) {
        this.eidasSamlParserService = eidasSamlParserService;
        this.vspService = vspService;
        this.samlFormViewBuilder = samlFormViewBuilder;
        this.configuration = configuration;
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
        ResponseDto eidasResponse = parseEidasRequest(encodedEidasAuthnRequest);
        RequestResponseBody vspResponse = generateHubRequestWithVSP();
        logAuthnRequestInformation(session, eidasResponse, vspResponse);
        session.setAttribute("eidas_request_id", eidasResponse.requestId);
        session.setAttribute("hub_request_id", vspResponse.getRequestId());
        return buildSamlFormView(vspResponse, eidasRelayState);
        // todo catch throwable and throw a AuthnRequestException
    }

    private ResponseDto parseEidasRequest(String encodedEidasAuthnRequest) {
        RequestDto request = new RequestDto();
        request.authnRequest = encodedEidasAuthnRequest;
        return eidasSamlParserService.validate(request);
    }

    private RequestResponseBody generateHubRequestWithVSP() {
        String proxyNodeEntityId = configuration.getProxyNodeEntityId();
        RequestGenerationBody requestGenerationBody = new RequestGenerationBody("LEVEL_2", proxyNodeEntityId);
        return vspService.generateAuthnRequest(requestGenerationBody);
    }

    private SamlFormView buildSamlFormView(RequestResponseBody vspResponse, String relayState) {
        String submitText = "Post Verify Authn Request to Hub";
        URI hubUrl = vspResponse.getSsoLocation();
        String samlRequest = vspResponse.getSamlRequest();
        return samlFormViewBuilder.buildRequest(hubUrl.toString(), samlRequest, submitText, relayState);
    }

    private void logAuthnRequestInformation(HttpSession session, ResponseDto eidasSamlResponse, RequestResponseBody vspResponse) {
        log.info(String.format("[eIDAS AuthnRequest] Session ID: %s", session.getId()));
        log.info(String.format("[eIDAS AuthnRequest] eIDAS Request ID: %s", eidasSamlResponse.requestId));
        log.info(String.format("[eIDAS AuthnRequest] eIDAS Issuer: %s", eidasSamlResponse.issuer));
        log.info(String.format("[Hub AuthnRequest] Hub Request ID: %s", vspResponse.getRequestId()));
        log.info(String.format("[Hub AuthnRequest] Hub URL: %s", vspResponse.getSsoLocation()));
    }
}
