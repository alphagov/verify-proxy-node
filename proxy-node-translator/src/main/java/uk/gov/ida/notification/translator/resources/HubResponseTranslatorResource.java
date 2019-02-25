package uk.gov.ida.notification.translator.resources;

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
import uk.gov.ida.notification.translator.saml.EidasResponseGenerator;
import uk.gov.ida.notification.translator.saml.HubResponseContainer;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.security.cert.X509Certificate;
import java.util.logging.Logger;

@Path(Urls.TranslatorUrls.TRANSLATOR_ROOT)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class HubResponseTranslatorResource {
    private static final Logger LOG = Logger.getLogger(HubResponseTranslatorResource.class.getName());
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

        final VerifyServiceProviderTranslationRequest vspRequest = new VerifyServiceProviderTranslationRequest(
                hubResponseTranslatorRequest.getSamlResponse(),
                hubResponseTranslatorRequest.getRequestId(),
                hubResponseTranslatorRequest.getLevelOfAssurance());

        final TranslatedHubResponse translatedHubResponse = verifyServiceProviderProxy.getTranslatedHubResponse(vspRequest);

        final HubResponseContainer hubResponseContainer = new HubResponseContainer(hubResponseTranslatorRequest, translatedHubResponse);
        final X509Certificate encryptionCertificate = X_509_CERTIFICATE_FACTORY.createCertificate(hubResponseTranslatorRequest.getConnectorEncryptionCertificate());

        final org.opensaml.saml.saml2.core.Response eidasResponse = eidasResponseGenerator.generate(hubResponseContainer, encryptionCertificate);
        logEidasResponse(eidasResponse);

        final String samlMessage = MARSHALLER.transformToString(eidasResponse);
        return Response.ok().entity(samlMessage).build();
    }

    private void logEidasResponse(org.opensaml.saml.saml2.core.Response eidasResponse) {
        LOG.info("[eIDAS Response] ID: " + eidasResponse.getID());
        LOG.info("[eIDAS Response] In response to: " + eidasResponse.getInResponseTo());
    }
}
