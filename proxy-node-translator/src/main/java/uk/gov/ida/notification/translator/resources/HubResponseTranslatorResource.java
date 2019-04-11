package uk.gov.ida.notification.translator.resources;

import org.glassfish.jersey.internal.util.Base64;
import uk.gov.ida.common.shared.security.X509CertificateFactory;
import uk.gov.ida.notification.contracts.HubResponseTranslatorRequest;
import uk.gov.ida.notification.contracts.SamlFailureResponseGenerationRequest;
import uk.gov.ida.notification.contracts.verifyserviceprovider.TranslatedHubResponse;
import uk.gov.ida.notification.contracts.verifyserviceprovider.VerifyServiceProviderTranslationRequest;
import uk.gov.ida.notification.saml.SamlObjectMarshaller;
import uk.gov.ida.notification.shared.ProxyNodeLogger;
import uk.gov.ida.notification.shared.ProxyNodeMDCKey;
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
import java.util.Objects;
import java.util.logging.Level;

import static uk.gov.ida.notification.translator.logging.HubResponseTranslatorLogger.logResponseAttributesHash;

@Path(Urls.TranslatorUrls.TRANSLATOR_ROOT)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class HubResponseTranslatorResource {

    private static final SamlObjectMarshaller MARSHALLER = new SamlObjectMarshaller();
    private static final X509CertificateFactory X_509_CERTIFICATE_FACTORY = new X509CertificateFactory();
    public static final String EIDAS_RESPONSE_LOGGER_MESSAGE = "eIDAS Response Attributes";

    private final EidasResponseGenerator eidasResponseGenerator;
    private final VerifyServiceProviderProxy verifyServiceProviderProxy;
    private ProxyNodeLogger proxyNodeLogger;

    public HubResponseTranslatorResource(EidasResponseGenerator eidasResponseGenerator, VerifyServiceProviderProxy verifyServiceProviderProxy, ProxyNodeLogger proxyNodeLogger) {
        this.eidasResponseGenerator = eidasResponseGenerator;
        this.verifyServiceProviderProxy = verifyServiceProviderProxy;
        this.proxyNodeLogger = proxyNodeLogger;
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

    private void logSamlResponse(org.opensaml.saml.saml2.core.Response samlResponse) {
//          todo discuss removing this id  MDC.put(HubResponseTranslatorLogger.HubResponseTranslatorLoggerAttributes.EIDAS_RESPONSE_ID, samlResponse.getID() != null ? samlResponse.getID() : "");

            proxyNodeLogger.addContext(ProxyNodeMDCKey.EIDAS_REQUEST_ID, Objects.requireNonNullElse(samlResponse.getInResponseTo(), ""));
            proxyNodeLogger.addContext(ProxyNodeMDCKey.EIDAS_DESTINATION, Objects.requireNonNullElse(samlResponse.getDestination(), ""));
            proxyNodeLogger.addContext(ProxyNodeMDCKey.EIDAS_ISSUER, samlResponse.getIssuer() != null ? samlResponse.getIssuer().getValue() : "");
            proxyNodeLogger.log(Level.INFO, EIDAS_RESPONSE_LOGGER_MESSAGE);

        }
    }
