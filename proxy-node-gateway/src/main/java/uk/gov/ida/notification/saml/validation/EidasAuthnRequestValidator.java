package uk.gov.ida.notification.saml.validation;

import com.google.common.base.Strings;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.saml2.core.AuthnRequest;
import se.litsec.eidas.opensaml.ext.RequestedAttributes;
import se.litsec.eidas.opensaml.ext.SPType;
import uk.gov.ida.notification.exceptions.authnrequest.InvalidAuthnRequestException;
import uk.gov.ida.notification.saml.validation.components.AssertionConsumerServiceValidator;
import uk.gov.ida.notification.saml.validation.components.ComparisonValidator;
import uk.gov.ida.notification.saml.validation.components.LoaValidator;
import uk.gov.ida.notification.saml.validation.components.RequestIssuerValidator;
import uk.gov.ida.notification.saml.validation.components.RequestedAttributesValidator;
import uk.gov.ida.notification.saml.validation.components.SpTypeValidator;
import uk.gov.ida.saml.core.validators.DestinationValidator;
import uk.gov.ida.saml.hub.exception.SamlValidationException;
import uk.gov.ida.saml.hub.validators.authnrequest.DuplicateAuthnRequestValidator;

import javax.xml.namespace.QName;

public class EidasAuthnRequestValidator {

    private SpTypeValidator spTypeValidator;
    private LoaValidator loaValidator;
    private RequestedAttributesValidator requestedAttributesValidator;
    private RequestIssuerValidator requestIssuerValidator;
    private final DuplicateAuthnRequestValidator duplicateAuthnRequestValidator;
    private final ComparisonValidator comparisonValidator;
    private final DestinationValidator destinationValidator;
    private final AssertionConsumerServiceValidator assertionConsumerServiceValidator;

    public EidasAuthnRequestValidator(RequestIssuerValidator requestIssuerValidator,
                                      SpTypeValidator spTypeValidator,
                                      LoaValidator loaValidator,
                                      RequestedAttributesValidator requestedAttributesValidator,
                                      DuplicateAuthnRequestValidator duplicateAuthnRequestValidator,
                                      ComparisonValidator comparisonValidator,
                                      DestinationValidator destinationValidator,
                                      AssertionConsumerServiceValidator assertionConsumerServiceValidator)
    {
        this.requestIssuerValidator = requestIssuerValidator;
        this.spTypeValidator = spTypeValidator;
        this.loaValidator = loaValidator;
        this.requestedAttributesValidator = requestedAttributesValidator;
        this.duplicateAuthnRequestValidator = duplicateAuthnRequestValidator;
        this.comparisonValidator = comparisonValidator;
        this.destinationValidator = destinationValidator;
        this.assertionConsumerServiceValidator = assertionConsumerServiceValidator;
    }

    public void validate(AuthnRequest request) {
        if (request == null)
            throw new InvalidAuthnRequestException("Null request");
        if (Strings.isNullOrEmpty(request.getID()))
            throw new InvalidAuthnRequestException("Missing Request ID");
        if (request.getExtensions() == null)
            throw new InvalidAuthnRequestException("Missing Extensions");
        if (request.isPassive() == true)
            throw new InvalidAuthnRequestException("Request should not require zero user interaction (isPassive should be missing or false)");
        if (request.isForceAuthn() != true)
            throw new InvalidAuthnRequestException("Request should require fresh authentication (forceAuthn should be true)");
        if (!Strings.isNullOrEmpty(request.getProtocolBinding()))
            throw new InvalidAuthnRequestException("Request should not specify protocol binding");
        if (request.getVersion() != SAMLVersion.VERSION_20)
            throw new InvalidAuthnRequestException("SAML Version should be " + SAMLVersion.VERSION_20.toString());

        SPType spTypeElement = (SPType) getExtension(request, SPType.DEFAULT_ELEMENT_NAME);
        RequestedAttributes requestedAttributesElement = (RequestedAttributes) getExtension(request, RequestedAttributes.DEFAULT_ELEMENT_NAME);

        requestIssuerValidator.validate(request.getIssuer());
        spTypeValidator.validate(spTypeElement);
        loaValidator.validate(request.getRequestedAuthnContext());
        requestedAttributesValidator.validate(requestedAttributesElement);
        assertionConsumerServiceValidator.validate(request);
        comparisonValidator.validate(request.getRequestedAuthnContext());

        if (!duplicateAuthnRequestValidator.valid(request.getID())) {
            throw new InvalidAuthnRequestException(String.format("Request ID has been seen before: %s", request.getID()));
        }

        try {
            destinationValidator.validate(request.getDestination());
        } catch (SamlValidationException e) {
            throw new InvalidAuthnRequestException(e.getMessage(), e);
        }
    }

    private XMLObject getExtension(AuthnRequest request, QName elementName) {
        return request.getExtensions()
                .getUnknownXMLObjects(elementName)
                .stream()
                .findFirst()
                .orElse(null);
    }
}
