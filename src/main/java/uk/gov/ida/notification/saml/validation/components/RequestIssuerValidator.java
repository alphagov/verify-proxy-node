package uk.gov.ida.notification.saml.validation.components;

import com.google.common.base.Strings;
import org.opensaml.saml.saml2.core.Issuer;
import uk.gov.ida.notification.exceptions.authnrequest.InvalidAuthnRequestException;

public class RequestIssuerValidator {
    public void validate(Issuer issuer) {
        if (issuer == null || Strings.isNullOrEmpty(issuer.getValue()))
            throw new InvalidAuthnRequestException("Missing Issuer");
    }
}
