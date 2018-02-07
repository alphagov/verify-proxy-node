package uk.gov.ida.notification.saml.validation;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.saml2.core.AuthnRequest;
import se.litsec.eidas.opensaml.ext.impl.SPTypeImpl;
import uk.gov.ida.notification.exceptions.authnrequest.InvalidAuthnRequestException;
import uk.gov.ida.notification.saml.validation.components.LoaValidator;
import uk.gov.ida.notification.saml.validation.components.NameIdPolicyValidator;
import uk.gov.ida.notification.saml.validation.components.RequestIssuerValidator;
import uk.gov.ida.notification.saml.validation.components.SpTypeValidator;

import java.util.Optional;

public class SamlAuthnRequestValidator {

    private final SpTypeValidator spTypeValidator;
    private final LoaValidator loaValidator;
    private final NameIdPolicyValidator nameIdPolicyValidator;
    private RequestIssuerValidator requestIssuerValidator;

    public SamlAuthnRequestValidator() {
        requestIssuerValidator = new RequestIssuerValidator();
        spTypeValidator = new SpTypeValidator();
        loaValidator = new LoaValidator();
        nameIdPolicyValidator = new NameIdPolicyValidator();
    }

    public void validateAuthnRequest(AuthnRequest request) {
        if (request == null) throw new InvalidAuthnRequestException("Null request");
        requestIssuerValidator.validate(request.getIssuer());
        spTypeValidator.validate(getSpType(request));
        loaValidator.validate(request.getRequestedAuthnContext());
        nameIdPolicyValidator.validate(request.getNameIDPolicy());
    }

    private Optional<XMLObject> getSpType(AuthnRequest request) {
        return request.getExtensions().getOrderedChildren()
                .stream()
                .filter(obj -> obj instanceof SPTypeImpl)
                .findFirst();
    }
}
