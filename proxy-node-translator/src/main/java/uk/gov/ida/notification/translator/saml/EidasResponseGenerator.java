package uk.gov.ida.notification.translator.saml;

import org.opensaml.saml.saml2.core.Response;
import uk.gov.ida.notification.saml.ResponseAssertionEncrypter;
import uk.gov.ida.notification.saml.SamlObjectSigner;
import uk.gov.ida.notification.saml.HubResponseContainer;
import uk.gov.ida.notification.saml.HubResponseTranslator;

public class EidasResponseGenerator {
    private final HubResponseTranslator translator;
    private final SamlObjectSigner samlObjectSigner;

    public EidasResponseGenerator(HubResponseTranslator translator, SamlObjectSigner samlObjectSigner) {
        this.translator = translator;
        this.samlObjectSigner = samlObjectSigner;
    }

    public Response generate(HubResponseContainer hubResponseContainer, ResponseAssertionEncrypter assertionEncrypter) {
        Response eidasResponse = translator.translate(hubResponseContainer);
        Response eidasResponseWithEncryptedAssertion = assertionEncrypter.encrypt(eidasResponse);
        samlObjectSigner.sign(eidasResponseWithEncryptedAssertion);
        return eidasResponseWithEncryptedAssertion;
    }
}
