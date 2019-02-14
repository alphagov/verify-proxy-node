package uk.gov.ida.notification.translator.saml;

import org.opensaml.saml.saml2.core.Response;
import org.opensaml.security.x509.BasicX509Credential;
import uk.gov.ida.notification.saml.ResponseAssertionEncrypter;
import uk.gov.ida.notification.saml.SamlObjectSigner;

import java.security.cert.X509Certificate;

public class EidasResponseGenerator {
    private final HubResponseTranslator translator;
    private final SamlObjectSigner samlObjectSigner;

    public EidasResponseGenerator(HubResponseTranslator translator, SamlObjectSigner samlObjectSigner) {
        this.translator = translator;
        this.samlObjectSigner = samlObjectSigner;
    }

    public Response generate(HubResponseContainer hubResponseContainer, X509Certificate encryptionCertificate) {
        final Response eidasResponse = translator.translate(hubResponseContainer);
        final BasicX509Credential encryptionCredential = new BasicX509Credential(encryptionCertificate);
        final ResponseAssertionEncrypter assertionEncrypter = new ResponseAssertionEncrypter(encryptionCredential);

        Response eidasResponseWithEncryptedAssertion = assertionEncrypter.encrypt(eidasResponse);
        samlObjectSigner.sign(eidasResponseWithEncryptedAssertion);

        return eidasResponseWithEncryptedAssertion;
    }
}
