package uk.gov.ida.notification.translator.resources;

import net.shibboleth.utilities.java.support.security.IdentifierGenerationStrategy;
import org.glassfish.jersey.internal.util.Base64;
import uk.gov.ida.eidas.logging.EidasAuthnResponseAttributesHashLogger;
import uk.gov.ida.notification.contracts.HubResponseTranslatorRequest;
import uk.gov.ida.notification.contracts.SamlFailureResponseGenerationRequest;
import uk.gov.ida.notification.contracts.verifyserviceprovider.TranslatedHubResponse;
import uk.gov.ida.notification.contracts.verifyserviceprovider.VerifyServiceProviderTranslationRequest;
import uk.gov.ida.notification.saml.SamlObjectMarshaller;
import uk.gov.ida.notification.shared.Urls;
import uk.gov.ida.notification.shared.logging.IngressEgressLogging;
import uk.gov.ida.notification.shared.logging.ProxyNodeLogger;
import uk.gov.ida.notification.shared.logging.ProxyNodeMDCKey;
import uk.gov.ida.notification.shared.proxy.VerifyServiceProviderProxy;
import uk.gov.ida.notification.translator.saml.EidasResponseGenerator;
import uk.gov.ida.notification.translator.saml.HubResponseContainer;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Objects;

@Path(Urls.TranslatorUrls.TRANSLATOR_ROOT)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class HubResponseTranslatorResource {

    private static final SamlObjectMarshaller MARSHALLER = new SamlObjectMarshaller();
    private final EidasResponseGenerator eidasResponseGenerator;
    private final VerifyServiceProviderProxy verifyServiceProviderProxy;
    private final IdentifierGenerationStrategy identifierGenerator;

    public HubResponseTranslatorResource(EidasResponseGenerator eidasResponseGenerator,
                                         VerifyServiceProviderProxy verifyServiceProviderProxy,
                                         IdentifierGenerationStrategy identifierGenerator) {
        this.eidasResponseGenerator = eidasResponseGenerator;
        this.verifyServiceProviderProxy = verifyServiceProviderProxy;
        this.identifierGenerator = identifierGenerator;
    }

    @POST
    @IngressEgressLogging
    @Path(Urls.TranslatorUrls.TRANSLATE_HUB_RESPONSE_PATH)
    public Response hubResponse(@Valid HubResponseTranslatorRequest hubResponseTranslatorRequest) {

        final TranslatedHubResponse translatedHubResponse = getAttributesFromVSP(hubResponseTranslatorRequest);

        EidasAuthnResponseAttributesHashLogger.logEidasAttributesHash(
                translatedHubResponse.getAttributes().orElse(null),
                translatedHubResponse.getPid().orElse(null),
                hubResponseTranslatorRequest.getRequestId(),
                hubResponseTranslatorRequest.getDestinationUrl());

        HubResponseContainer hubResponseContainer = new HubResponseContainer(hubResponseTranslatorRequest, translatedHubResponse, identifierGenerator);
        final org.opensaml.saml.saml2.core.Response eidasResponse = eidasResponseGenerator.generateFromHubResponse(hubResponseContainer);

        logSamlResponse(eidasResponse);

        final String samlMessage = Base64.encodeAsString(MARSHALLER.transformToString(eidasResponse));

        return Response.ok().entity(samlMessage).build();
    }

    private TranslatedHubResponse getAttributesFromVSP(@Valid HubResponseTranslatorRequest hubResponseTranslatorRequest) {
        return verifyServiceProviderProxy.getTranslatedHubResponse(
                new VerifyServiceProviderTranslationRequest(
                        hubResponseTranslatorRequest.getSamlResponse(),
                        hubResponseTranslatorRequest.getRequestId(),
                        hubResponseTranslatorRequest.getLevelOfAssurance()
                )
        );
    }

    @POST
    @IngressEgressLogging
    @Path(Urls.TranslatorUrls.GENERATE_FAILURE_RESPONSE_PATH)
    public Response failureResponse(@Valid SamlFailureResponseGenerationRequest failureResponseRequest) {

        final org.opensaml.saml.saml2.core.Response failureEidasResponse = eidasResponseGenerator.generateFailureResponse(
                failureResponseRequest.getResponseStatus(),
                failureResponseRequest.getEidasRequestId(),
                failureResponseRequest.getDestinationUrl(),
                failureResponseRequest.getEntityId());

        logSamlResponse(failureEidasResponse);

        final String samlMessage = Base64.encodeAsString(MARSHALLER.transformToString(failureEidasResponse));

        return Response.ok().entity(samlMessage).build();
    }

    // TODO: Remove once we set all the headers correctly
    private void logSamlResponse(org.opensaml.saml.saml2.core.Response samlResponse) {
        ProxyNodeLogger.addContext(ProxyNodeMDCKey.EIDAS_REQUEST_ID, Objects.requireNonNullElse(samlResponse.getInResponseTo(), ""));
        ProxyNodeLogger.addContext(ProxyNodeMDCKey.EIDAS_DESTINATION, Objects.requireNonNullElse(samlResponse.getDestination(), ""));
        ProxyNodeLogger.addContext(ProxyNodeMDCKey.EIDAS_ISSUER, samlResponse.getIssuer() != null ? samlResponse.getIssuer().getValue() : "");
        ProxyNodeLogger.info("Received eIDAS Response Attributes from VSP");
    }
}
