package uk.gov.ida.notification.translator.saml;

import org.opensaml.saml.saml2.core.Response;
import org.opensaml.security.x509.X509Credential;
import uk.gov.ida.notification.saml.HubResponseContainer;
import uk.gov.ida.notification.saml.ResponseAssertionEncrypter;
import uk.gov.ida.notification.saml.SamlObjectSigner;

public class EidasResponseGenerator {
    private final HubResponseTranslator translator;
    private final SamlObjectSigner samlObjectSigner;

    public EidasResponseGenerator(HubResponseTranslator translator, SamlObjectSigner samlObjectSigner) {
        this.translator = translator;
        this.samlObjectSigner = samlObjectSigner;
    }

    public Response generate(HubResponseContainer hubResponseContainer, X509Credential encryptionCredential) {
        Response eidasResponse = translator.translate(hubResponseContainer);
        ResponseAssertionEncrypter assertionEncrypter = new ResponseAssertionEncrypter(encryptionCredential);
        Response eidasResponseWithEncryptedAssertion = assertionEncrypter.encrypt(eidasResponse);
        samlObjectSigner.sign(eidasResponseWithEncryptedAssertion);
        return eidasResponseWithEncryptedAssertion;
    }
}
