package uk.gov.ida.notification.saml.validation;

import com.google.common.base.Strings;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.saml2.core.AuthnRequest;
import se.litsec.eidas.opensaml.ext.RequestedAttributes;
import se.litsec.eidas.opensaml.ext.SPType;
import uk.gov.ida.notification.exceptions.authnrequest.InvalidAuthnRequestException;
import uk.gov.ida.notification.saml.validation.components.LoaValidator;
import uk.gov.ida.notification.saml.validation.components.RequestIssuerValidator;
import uk.gov.ida.notification.saml.validation.components.RequestedAttributesValidator;
import uk.gov.ida.notification.saml.validation.components.SpTypeValidator;

import javax.xml.namespace.QName;

public class EidasAuthnRequestValidator {

    private SpTypeValidator spTypeValidator;
    private LoaValidator loaValidator;
    private RequestedAttributesValidator requestedAttributesValidator;
    private RequestIssuerValidator requestIssuerValidator;

    public EidasAuthnRequestValidator(RequestIssuerValidator requestIssuerValidator,
                                      SpTypeValidator spTypeValidator,
                                      LoaValidator loaValidator,
                                      RequestedAttributesValidator requestedAttributesValidator)
    {
        this.requestIssuerValidator = requestIssuerValidator;
        this.spTypeValidator = spTypeValidator;
        this.loaValidator = loaValidator;
        this.requestedAttributesValidator = requestedAttributesValidator;
    }

    public void validate(AuthnRequest request) {
        if (request == null)
            throw new InvalidAuthnRequestException("Null request");
        if (Strings.isNullOrEmpty(request.getID()))
            throw new InvalidAuthnRequestException("Missing Request ID");
        if (request.getExtensions() == null)
            throw new InvalidAuthnRequestException("Missing Extensions");

        SPType spTypeElement = (SPType) getExtension(request, SPType.DEFAULT_ELEMENT_NAME);
        RequestedAttributes requestedAttributesElement = (RequestedAttributes) getExtension(request, RequestedAttributes.DEFAULT_ELEMENT_NAME);

        requestIssuerValidator.validate(request.getIssuer());
        spTypeValidator.validate(spTypeElement);
        loaValidator.validate(request.getRequestedAuthnContext());
        requestedAttributesValidator.validate(requestedAttributesElement);
    }

    private XMLObject getExtension(AuthnRequest request, QName elementName) {
        return request.getExtensions()
                .getUnknownXMLObjects(elementName)
                .stream()
                .findFirst()
                .orElse(null);
    }
}
