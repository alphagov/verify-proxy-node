package uk.gov.ida.notification.translator.resources;

import org.glassfish.jersey.internal.util.Base64;
import uk.gov.ida.common.shared.security.X509CertificateFactory;
import uk.gov.ida.notification.contracts.HubResponseTranslatorRequest;
import uk.gov.ida.notification.contracts.SamlFailureResponseGenerationRequest;
import uk.gov.ida.notification.contracts.verifyserviceprovider.TranslatedHubResponse;
import uk.gov.ida.notification.contracts.verifyserviceprovider.VerifyServiceProviderTranslationRequest;
import uk.gov.ida.notification.saml.SamlObjectMarshaller;
import uk.gov.ida.notification.shared.Urls;
import uk.gov.ida.notification.shared.proxy.VerifyServiceProviderProxy;
import uk.gov.ida.notification.translator.saml.EidasResponseGenerator;
import uk.gov.ida.notification.translator.saml.HubResponseContainer;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static uk.gov.ida.notification.translator.logging.HubResponseTranslatorLogger.logResponseAttributesHash;
import static uk.gov.ida.notification.translator.logging.HubResponseTranslatorLogger.logSamlResponse;

@Path(Urls.TranslatorUrls.TRANSLATOR_ROOT)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class HubResponseTranslatorResource {

    private static final SamlObjectMarshaller MARSHALLER = new SamlObjectMarshaller();
    private static final X509CertificateFactory X_509_CERTIFICATE_FACTORY = new X509CertificateFactory();

    private final EidasResponseGenerator eidasResponseGenerator;
    private final VerifyServiceProviderProxy verifyServiceProviderProxy;

    public HubResponseTranslatorResource(EidasResponseGenerator eidasResponseGenerator, VerifyServiceProviderProxy verifyServiceProviderProxy) {
        this.eidasResponseGenerator = eidasResponseGenerator;
        this.verifyServiceProviderProxy = verifyServiceProviderProxy;
    }

    @POST
    @Path(Urls.TranslatorUrls.TRANSLATE_HUB_RESPONSE_PATH)
    public Response hubResponse(HubResponseTranslatorRequest hubResponseTranslatorRequest) {

        final TranslatedHubResponse translatedHubResponse =
                verifyServiceProviderProxy.getTranslatedHubResponse(
                        new VerifyServiceProviderTranslationRequest(
                                hubResponseTranslatorRequest.getSamlResponse(),
                                hubResponseTranslatorRequest.getRequestId(),
                                hubResponseTranslatorRequest.getLevelOfAssurance()
                        )
                );

        logResponseAttributesHash(hubResponseTranslatorRequest, translatedHubResponse);

        final org.opensaml.saml.saml2.core.Response eidasResponse = eidasResponseGenerator.generateFromHubResponse(
                new HubResponseContainer(hubResponseTranslatorRequest, translatedHubResponse),
                X_509_CERTIFICATE_FACTORY.createCertificate(hubResponseTranslatorRequest.getConnectorEncryptionCertificate())
        );

        logSamlResponse(eidasResponse);

        final String samlMessage = Base64.encodeAsString(MARSHALLER.transformToString(eidasResponse));

        return Response.ok().entity(samlMessage).build();
    }

    @POST
    @Path(Urls.TranslatorUrls.GENERATE_FAILURE_RESPONSE_PATH)
    public Response failureResponse(SamlFailureResponseGenerationRequest failureResponseRequest) {

        final org.opensaml.saml.saml2.core.Response failureEidasResponse = eidasResponseGenerator.generateFailureResponse(
                failureResponseRequest.getResponseStatus(),
                failureResponseRequest.getEidasRequestId(),
                failureResponseRequest.getDestinationUrl()
        );

        logSamlResponse(failureEidasResponse);

        final String samlMessage = Base64.encodeAsString(MARSHALLER.transformToString(failureEidasResponse));

        return Response.ok().entity(samlMessage).build();
    }
}
