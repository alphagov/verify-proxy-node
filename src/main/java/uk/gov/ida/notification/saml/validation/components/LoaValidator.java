package uk.gov.ida.notification.saml.validation.components;

import org.opensaml.saml.saml2.core.RequestedAuthnContext;
import se.litsec.eidas.opensaml.common.EidasConstants;
import uk.gov.ida.notification.exceptions.authnrequest.InvalidAuthnRequestException;

import java.text.MessageFormat;

public class LoaValidator {
    public void validate(RequestedAuthnContext requestedAuthnContext) {
        String loa = requestedAuthnContext.getAuthnContextClassRefs().get(0).getAuthnContextClassRef();
        if (!EidasConstants.EIDAS_LOA_SUBSTANTIAL.equals(loa)) {
            String message = MessageFormat.format("Invalid LoA ''{0}''", loa);
            throw new InvalidAuthnRequestException(message);
        }
    }
}
