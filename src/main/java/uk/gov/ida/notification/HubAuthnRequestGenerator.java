package uk.gov.ida.notification;

import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.security.credential.Credential;
import uk.gov.ida.notification.saml.translation.EidasAuthnRequest;
import uk.gov.ida.notification.saml.translation.EidasAuthnRequestTranslator;

public class HubAuthnRequestGenerator {
    private EidasAuthnRequestTranslator translator;
    private ProxyNodeSigner signer;
    private CredentialRepository credentialRepository;

    public HubAuthnRequestGenerator(EidasAuthnRequestTranslator translator, ProxyNodeSigner signer, CredentialRepository credentialRepository) {
        this.translator = translator;
        this.signer = signer;
        this.credentialRepository = credentialRepository;
    }

    public AuthnRequest generate(EidasAuthnRequest eidasAuthnRequest) throws Throwable {
        AuthnRequest authnRequest = translator.translate(eidasAuthnRequest);
        Credential hubCredential = credentialRepository.getHubCredential();
        return (AuthnRequest) signer.sign(authnRequest, hubCredential);
    }
}
