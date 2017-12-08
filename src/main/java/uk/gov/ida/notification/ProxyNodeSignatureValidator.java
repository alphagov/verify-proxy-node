package uk.gov.ida.notification;

import org.opensaml.saml.saml2.core.Response;
import org.opensaml.security.credential.Credential;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.SignatureValidator;

public class ProxyNodeSignatureValidator {

    public void validate(Response response, Credential credential) throws SignatureException {
        SignatureValidator.validate(response.getSignature(), credential);
    }
}
