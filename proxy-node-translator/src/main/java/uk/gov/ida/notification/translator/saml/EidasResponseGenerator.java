package uk.gov.ida.notification.translator.saml;

import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.security.SecurityException;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.xmlsec.signature.support.SignatureException;
import uk.gov.ida.notification.exceptions.hubresponse.ResponseSigningException;
import uk.gov.ida.notification.saml.ResponseAssertionEncrypter;
import uk.gov.ida.notification.saml.SamlObjectSigner;

import java.security.cert.X509Certificate;

public class EidasResponseGenerator {
    private final HubResponseTranslator hubResponseTranslator;
    private final EidasFailureResponseGenerator failureResponseGenerator;
    private final SamlObjectSigner samlObjectSigner;

    public EidasResponseGenerator(HubResponseTranslator hubResponseTranslator, EidasFailureResponseGenerator failureResponseGenerator, SamlObjectSigner samlObjectSigner) {
        this.hubResponseTranslator = hubResponseTranslator;
        this.failureResponseGenerator = failureResponseGenerator;
        this.samlObjectSigner = samlObjectSigner;
    }

    public Response generateFromHubResponse(HubResponseContainer hubResponseContainer, X509Certificate encryptionCertificate) {
        final Response eidasResponse = hubResponseTranslator.getTranslatedHubResponse(hubResponseContainer);
        final Response eidasResponseWithEncryptedAssertion = encryptAssertions(eidasResponse, encryptionCertificate);
        return signSamlResponse(eidasResponseWithEncryptedAssertion, hubResponseContainer.getEidasRequestId());
    }

    public Response generateFailureResponse(javax.ws.rs.core.Response.Status responseStatus, String eidasRequestId, String destinationUrl) {
        final Response eidasResponse = failureResponseGenerator.generateFailureSamlResponse(responseStatus, eidasRequestId, destinationUrl);
        return signSamlResponse(eidasResponse, eidasRequestId);
    }

    private Response encryptAssertions(Response eidasResponse, X509Certificate encryptionCertificate) {
        final BasicX509Credential encryptionCredential = new BasicX509Credential(encryptionCertificate);
        final ResponseAssertionEncrypter assertionEncrypter = new ResponseAssertionEncrypter(encryptionCredential);
        return assertionEncrypter.encrypt(eidasResponse);
    }

    private Response signSamlResponse(Response eidasResponse, String eidasRequestId) {
        try {
            samlObjectSigner.sign(eidasResponse, eidasRequestId);
        } catch (MarshallingException | SecurityException | SignatureException e) {
            throw new ResponseSigningException(e);
        }

        return eidasResponse;
    }
}
