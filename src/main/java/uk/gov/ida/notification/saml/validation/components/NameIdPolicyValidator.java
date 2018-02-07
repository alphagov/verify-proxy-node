package uk.gov.ida.notification.saml.validation.components;

import org.opensaml.saml.saml2.core.NameIDPolicy;
import uk.gov.ida.notification.exceptions.authnrequest.InvalidAuthnRequestException;

public class NameIdPolicyValidator {
    public void validate(NameIDPolicy nameIDPolicy) {
        if (nameIDPolicy == null ) throw new InvalidAuthnRequestException("Missing NameIdPolicy");
    }
}
