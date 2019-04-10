package uk.gov.ida.notification.eidassaml;

import net.shibboleth.utilities.java.support.xml.XMLParserException;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import se.litsec.opensaml.utils.ObjectUtils;
import uk.gov.ida.notification.contracts.EidasSamlParserRequest;
import uk.gov.ida.notification.contracts.EidasSamlParserResponse;
import uk.gov.ida.notification.eidassaml.saml.validation.EidasAuthnRequestValidator;
import uk.gov.ida.notification.shared.ProxyNodeLogger;
import uk.gov.ida.notification.shared.ProxyNodeMDCKey;
import uk.gov.ida.notification.shared.Urls;
import uk.gov.ida.saml.security.validators.signature.SamlRequestSignatureValidator;

import javax.validation.Valid;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.logging.Level;

@Path(Urls.EidasSamlParserUrls.EIDAS_AUTHN_REQUEST_PATH)
@Produces(MediaType.APPLICATION_JSON)
public class EidasSamlResource {
    private EidasAuthnRequestValidator eidasAuthnRequestValidator;
    private SamlRequestSignatureValidator samlRequestSignatureValidator;
    private String x509EncryptionCertString;
    private String destination;
    private ProxyNodeLogger proxyNodeLogger;

    public EidasSamlResource(EidasAuthnRequestValidator eidasAuthnRequestValidator,
                             SamlRequestSignatureValidator samlRequestSignatureValidator,
                             String x509EncryptionCertString,
                             String destination,
                             ProxyNodeLogger proxyNodeLogger) {
        this.eidasAuthnRequestValidator = eidasAuthnRequestValidator;
        this.samlRequestSignatureValidator = samlRequestSignatureValidator;
        this.x509EncryptionCertString = x509EncryptionCertString;
        this.destination = destination;
        this.proxyNodeLogger = proxyNodeLogger;
    }

    @POST
    @Valid
    public EidasSamlParserResponse post(EidasSamlParserRequest request) throws UnmarshallingException, XMLParserException {

        AuthnRequest authnRequest = ObjectUtils.unmarshall(
                new ByteArrayInputStream(Base64.getDecoder().decode(request.getAuthnRequest().getBytes())),
                AuthnRequest.class);

        samlRequestSignatureValidator.validate(authnRequest, SPSSODescriptor.DEFAULT_ELEMENT_NAME);
        eidasAuthnRequestValidator.validate(authnRequest);

        proxyNodeLogger.addContext(ProxyNodeMDCKey.eidasRequestId, authnRequest.getID());
        proxyNodeLogger.addContext(ProxyNodeMDCKey.eidasDestination, authnRequest.getDestination());
        proxyNodeLogger.addContext(ProxyNodeMDCKey.eidasIssuer, authnRequest.getIssuer().getValue());
        proxyNodeLogger.addContext(ProxyNodeMDCKey.eidasIssueInstant, authnRequest.getIssueInstant().toString());
        proxyNodeLogger.log(Level.INFO, "Authn request validated by ESP");

        return new EidasSamlParserResponse(
                authnRequest.getID(),
                authnRequest.getIssuer().getValue(),
                x509EncryptionCertString,
                destination);
    }
}
