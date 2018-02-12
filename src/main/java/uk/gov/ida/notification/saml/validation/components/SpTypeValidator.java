package uk.gov.ida.notification.saml.validation.components;

import se.litsec.eidas.opensaml.ext.SPType;
import se.litsec.eidas.opensaml.ext.SPTypeEnumeration;
import uk.gov.ida.notification.exceptions.authnrequest.InvalidAuthnRequestException;

import java.text.MessageFormat;

public class SpTypeValidator {
    public void validate(SPType spType) {
        if(spType == null) return;

        SPTypeEnumeration spTypeType = spType.getType();
        if (!SPTypeEnumeration.PUBLIC.equals(spTypeType)) {
            String message = MessageFormat.format("Invalid SPType ''{0}''", spTypeType);
            throw new InvalidAuthnRequestException(message);
        }
    }
}
