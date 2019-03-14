package uk.gov.ida.notification.translator.resources;

import org.glassfish.jersey.internal.util.Base64;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.security.SecurityException;
import org.opensaml.xmlsec.signature.support.SignatureException;
import uk.gov.ida.common.shared.security.X509CertificateFactory;
import uk.gov.ida.notification.contracts.HubResponseTranslatorRequest;
import uk.gov.ida.notification.contracts.verifyserviceprovider.TranslatedHubResponse;
import uk.gov.ida.notification.contracts.verifyserviceprovider.VerifyServiceProviderTranslationRequest;
import uk.gov.ida.notification.saml.SamlObjectMarshaller;
import uk.gov.ida.notification.shared.Urls;
import uk.gov.ida.notification.shared.proxy.VerifyServiceProviderProxy;
import uk.gov.ida.notification.translator.logging.EidasResponseAttributesHashLoggerHelper;
import uk.gov.ida.notification.translator.logging.HubResponseTranslatorLoggerHelper;
import uk.gov.ida.notification.translator.saml.EidasResponseGenerator;
import uk.gov.ida.notification.translator.saml.HubResponseContainer;
import uk.gov.ida.saml.core.transformers.EidasResponseAttributesHashLogger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
    public Response hubResponse(HubResponseTranslatorRequest hubResponseTranslatorRequest) throws MarshallingException, SecurityException, SignatureException {

        final TranslatedHubResponse translatedHubResponse =
                verifyServiceProviderProxy.getTranslatedHubResponse(
                        new VerifyServiceProviderTranslationRequest(
                                hubResponseTranslatorRequest.getSamlResponse(),
                                hubResponseTranslatorRequest.getRequestId(),
                                hubResponseTranslatorRequest.getLevelOfAssurance()
                        )
                );

        final EidasResponseAttributesHashLoggerHelper eidasResponseAttributesHashLoggerHelper =
                new EidasResponseAttributesHashLoggerHelper(EidasResponseAttributesHashLogger.instance());

        eidasResponseAttributesHashLoggerHelper.applyAttributesToHashLogger(
                translatedHubResponse.getAttributes(),
                translatedHubResponse.getPid()
        ).logHashFor(
                hubResponseTranslatorRequest.getRequestId(),
                hubResponseTranslatorRequest.getDestinationUrl().toString()
        );

        final org.opensaml.saml.saml2.core.Response eidasResponse =
                eidasResponseGenerator.generate(
                        new HubResponseContainer(hubResponseTranslatorRequest, translatedHubResponse),
                        X_509_CERTIFICATE_FACTORY.createCertificate(hubResponseTranslatorRequest.getConnectorEncryptionCertificate())
                );

        logResponse(eidasResponse);

        final String samlMessage = Base64.encodeAsString(MARSHALLER.transformToString(eidasResponse));

        return Response.ok().entity(samlMessage).build();
    }

    private void logResponse(final org.opensaml.saml.saml2.core.Response eidasResponse) {
        HubResponseTranslatorLoggerHelper.logEidasResponse(eidasResponse);
    }
}
