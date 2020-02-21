package uk.gov.ida.notification.translator.saml;

import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.security.SecurityException;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.xmlsec.signature.support.SignatureException;

import uk.gov.ida.common.shared.security.X509CertificateFactory;
import uk.gov.ida.notification.contracts.metadata.CountryMetadataResponse;
import uk.gov.ida.notification.exceptions.hubresponse.ResponseSigningException;
import uk.gov.ida.notification.saml.ResponseAssertionEncrypter;
import uk.gov.ida.notification.saml.SamlObjectSigner;
import uk.gov.ida.notification.shared.proxy.MetatronProxy;

import java.net.URI;

public class EidasResponseGenerator {
    private final HubResponseTranslator hubResponseTranslator;
    private final EidasFailureResponseGenerator failureResponseGenerator;
    private final SamlObjectSigner samlObjectSigner;
    private final MetatronProxy metatronProxy;
    private final X509CertificateFactory x509CertificateFactory;

    public EidasResponseGenerator(
            final HubResponseTranslator hubResponseTranslator,
            final EidasFailureResponseGenerator failureResponseGenerator,
            final SamlObjectSigner samlObjectSigner,
            final MetatronProxy metatronProxy,
            final X509CertificateFactory x509CertificateFactory) {
        this.hubResponseTranslator = hubResponseTranslator;
        this.failureResponseGenerator = failureResponseGenerator;
        this.samlObjectSigner = samlObjectSigner;
        this.metatronProxy = metatronProxy;
        this.x509CertificateFactory = x509CertificateFactory;
    }

    public Response generateFromHubResponse(final HubResponseContainer hubResponseContainer) {
        final CountryMetadataResponse countryMetadataResponse = metatronProxy.getCountryMetadata(hubResponseContainer.getIssuer().toString());
        final Response eidasResponse = hubResponseTranslator.getTranslatedHubResponse(hubResponseContainer, countryMetadataResponse);
        final Response encryptedEidasResponse = encryptAssertions(eidasResponse, countryMetadataResponse.getSamlEncryptionCertX509());

        return signSamlResponse(encryptedEidasResponse, hubResponseContainer.getEidasRequestId());
    }

    public Response generateFailureResponse(
            final javax.ws.rs.core.Response.Status responseStatus,
            final String eidasRequestId,
            final String destinationUrl,
            final URI entityId) {

        final Response eidasResponse = failureResponseGenerator.generateFailureSamlResponse(
                responseStatus,
                eidasRequestId,
                destinationUrl,
                entityId.toString());
        return signSamlResponse(eidasResponse, eidasRequestId);
    }

    private Response encryptAssertions(final Response eidasResponse, final String encryptionCertificate) {
        return new ResponseAssertionEncrypter(
                new BasicX509Credential(
                        x509CertificateFactory.createCertificate(encryptionCertificate)
                )
        ).encrypt(eidasResponse);
    }

    private Response signSamlResponse(final Response eidasResponse, final String eidasRequestId) {
        try {
            samlObjectSigner.sign(eidasResponse, eidasRequestId);
        } catch (MarshallingException | SecurityException | SignatureException e) {
            throw new ResponseSigningException(e);
        }

        return eidasResponse;
    }
}
