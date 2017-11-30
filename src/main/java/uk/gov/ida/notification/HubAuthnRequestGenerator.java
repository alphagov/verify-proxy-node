package uk.gov.ida.notification;

import org.opensaml.saml.saml2.core.AuthnRequest;
import uk.gov.ida.notification.saml.translation.EidasAuthnRequest;
import uk.gov.ida.notification.saml.translation.EidasAuthnRequestTranslator;

public class HubAuthnRequestGenerator {
    private EidasAuthnRequestTranslator translator;
    private ProxyNodeSigner signer;

    public HubAuthnRequestGenerator(EidasAuthnRequestTranslator translator, ProxyNodeSigner signer) {
        this.translator = translator;
        this.signer = signer;
    }

    public AuthnRequest generate(EidasAuthnRequest eidasAuthnRequest) {
        AuthnRequest authnRequest = translator.translate(eidasAuthnRequest);
        return signer.sign(authnRequest);
    }
}
