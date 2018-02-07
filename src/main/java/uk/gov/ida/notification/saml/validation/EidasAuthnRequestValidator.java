package uk.gov.ida.notification.saml.validation;

import com.google.common.base.Strings;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.saml2.core.AuthnRequest;
import se.litsec.eidas.opensaml.ext.impl.SPTypeImpl;
import uk.gov.ida.notification.exceptions.authnrequest.InvalidAuthnRequestException;
import uk.gov.ida.notification.saml.validation.components.LoaValidator;
import uk.gov.ida.notification.saml.validation.components.NameIdPolicyValidator;
import uk.gov.ida.notification.saml.validation.components.RequestIssuerValidator;
import uk.gov.ida.notification.saml.validation.components.SpTypeValidator;

import java.util.Optional;

public class EidasAuthnRequestValidator {

    private SpTypeValidator spTypeValidator;
    private LoaValidator loaValidator;
    private NameIdPolicyValidator nameIdPolicyValidator;
    private RequestIssuerValidator requestIssuerValidator;

    public EidasAuthnRequestValidator(RequestIssuerValidator requestIssuerValidator,
                                      SpTypeValidator spTypeValidator,
                                      LoaValidator loaValidator,
                                      NameIdPolicyValidator nameIdPolicyValidator) {
        this.requestIssuerValidator = requestIssuerValidator;
        this.spTypeValidator = spTypeValidator;
        this.loaValidator = loaValidator;
        this.nameIdPolicyValidator = nameIdPolicyValidator;
    }

    public void validate(AuthnRequest request) {
        if (request == null)
            throw new InvalidAuthnRequestException("Null request");
        if (Strings.isNullOrEmpty(request.getID()))
            throw new InvalidAuthnRequestException("Missing Request ID");
        if (request.getExtensions() == null)
            throw new InvalidAuthnRequestException("Missing Extensions");

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
