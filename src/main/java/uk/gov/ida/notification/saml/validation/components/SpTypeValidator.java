package uk.gov.ida.notification.saml.validation.components;

import org.opensaml.core.xml.XMLObject;
import se.litsec.eidas.opensaml.ext.SPType;
import se.litsec.eidas.opensaml.ext.SPTypeEnumeration;
import uk.gov.ida.notification.exceptions.authnrequest.InvalidAuthnRequestException;

import java.text.MessageFormat;
import java.util.Optional;

public class SpTypeValidator {
    public void validate(Optional<XMLObject> spTypeElement) {
        SPType spType = (SPType) spTypeElement.orElse(null);
        SPTypeEnumeration spTypeType = spType.getType();
        if (!SPTypeEnumeration.PUBLIC.equals(spTypeType)) {
            String message = MessageFormat.format("Invalid SPType ''{0}''", spTypeType);
            throw new InvalidAuthnRequestException(message);
        }
    }
}
