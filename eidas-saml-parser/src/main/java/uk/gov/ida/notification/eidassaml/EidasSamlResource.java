package uk.gov.ida.notification.eidassaml;

import net.shibboleth.utilities.java.support.xml.XMLParserException;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.security.credential.BasicCredential;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.credential.UsageType;
import org.slf4j.event.Level;
import se.litsec.opensaml.utils.ObjectUtils;
import uk.gov.ida.notification.contracts.EidasSamlParserRequest;
import uk.gov.ida.notification.contracts.EidasSamlParserResponse;
import uk.gov.ida.notification.contracts.metadata.AssertionConsumerService;
import uk.gov.ida.notification.contracts.metadata.CountryMetadataResponse;
import uk.gov.ida.notification.eidassaml.saml.validation.EidasAuthnRequestValidator;
import uk.gov.ida.notification.shared.Urls;
import uk.gov.ida.notification.shared.logging.IngressEgressLogging;
import uk.gov.ida.notification.shared.logging.ProxyNodeLogger;
import uk.gov.ida.notification.shared.logging.ProxyNodeMDCKey;
import uk.gov.ida.notification.shared.proxy.MetatronProxy;
import uk.gov.ida.saml.core.validation.SamlTransformationErrorException;

import javax.validation.Valid;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Base64;
import java.util.Comparator;
import java.util.stream.Collectors;

@IngressEgressLogging
@Path(Urls.EidasSamlParserUrls.EIDAS_AUTHN_REQUEST_PATH)
@Produces(MediaType.APPLICATION_JSON)
public class EidasSamlResource {

    private final EidasAuthnRequestValidator eidasAuthnRequestValidator;
    private final MetatronProxy metatronProxy;

    public EidasSamlResource(EidasAuthnRequestValidator eidasAuthnRequestValidator,
                             MetatronProxy metatronProxy) {
        this.eidasAuthnRequestValidator = eidasAuthnRequestValidator;
        this.metatronProxy = metatronProxy;
    }

    @POST
    @Valid
    public EidasSamlParserResponse post(@Valid EidasSamlParserRequest request) throws UnmarshallingException, XMLParserException, CertificateException {
        final AuthnRequest authnRequest = unmarshallRequest(request);
        final CountryMetadataResponse metatronResponse = getMetatronResponse(authnRequest);

        eidasAuthnRequestValidator.validate(authnRequest, this.getSigningCredential(metatronResponse.getSamlSigningCertX509()));

        final String assertionConsumerServiceURL =
                authnRequest.getAssertionConsumerServiceURL() != null ?
                        authnRequest.getAssertionConsumerServiceURL() :
                        metatronResponse.getAssertionConsumerServices()
                                .stream()
                                .filter(AssertionConsumerService::isDefaultService)
                                .min(Comparator.comparing(AssertionConsumerService::getIndex))
                                .map(AssertionConsumerService::getLocation)
                                .map(URI::toString)
                                .orElseThrow();

        this.logAuthnRequestMdcProperties(authnRequest);
        return new EidasSamlParserResponse(
                authnRequest.getID(),
                authnRequest.getIssuer().getValue(),
                assertionConsumerServiceURL);
    }

    private AuthnRequest unmarshallRequest(EidasSamlParserRequest request) throws UnmarshallingException, XMLParserException {
        return ObjectUtils.unmarshall(
                new ByteArrayInputStream(
                        Base64.getDecoder().decode(request.getAuthnRequest().getBytes())),
                AuthnRequest.class);
    }

    private CountryMetadataResponse getMetatronResponse(AuthnRequest authnRequest) {
        this.validateIssuer(authnRequest);
        return metatronProxy.getCountryMetadata(authnRequest.getIssuer().getValue());
    }

    private void logAuthnRequestMdcProperties(AuthnRequest authnRequest) {
        ProxyNodeLogger.addContext(ProxyNodeMDCKey.EIDAS_REQUEST_ID, authnRequest.getID());
        ProxyNodeLogger.addContext(ProxyNodeMDCKey.EIDAS_DESTINATION, authnRequest.getDestination());
        ProxyNodeLogger.addContext(ProxyNodeMDCKey.EIDAS_ISSUER, authnRequest.getIssuer().getValue());
        ProxyNodeLogger.addContext(ProxyNodeMDCKey.EIDAS_ISSUE_INSTANT, authnRequest.getIssueInstant().toString());
        ProxyNodeLogger.addContext(ProxyNodeMDCKey.EIDAS_LOA, getLevelsOfAssurance(authnRequest));
        ProxyNodeLogger.info("Authn request validated by ESP");
    }

    private String getLevelsOfAssurance(AuthnRequest authnRequest) {
        return authnRequest.getRequestedAuthnContext().getAuthnContextClassRefs()
                .stream()
                .map(AuthnContextClassRef::getAuthnContextClassRef)
                .collect(Collectors.joining(","));
    }

    private void validateIssuer(AuthnRequest authnRequest) {
        if (authnRequest.getIssuer().getValue() == null) {
            throw new SamlTransformationErrorException("Issuer is missing", Level.WARN);
        }
    }

    private Credential getSigningCredential(String base64X509Certificate) throws CertificateException {
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X509");

        byte[] decodedCert = Base64.getMimeDecoder().decode(base64X509Certificate);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(decodedCert);
        Certificate certificate = certificateFactory.generateCertificate(inputStream);

        BasicCredential signingCredential = new BasicCredential(certificate.getPublicKey());
        signingCredential.setUsageType(UsageType.SIGNING);

        return signingCredential;
    }
}
