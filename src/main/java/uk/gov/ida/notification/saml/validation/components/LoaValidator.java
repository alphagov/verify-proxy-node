package uk.gov.ida.notification.saml.validation.components;

import com.google.common.base.Strings;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.RequestedAuthnContext;
import se.litsec.eidas.opensaml.common.EidasConstants;
import uk.gov.ida.notification.exceptions.authnrequest.InvalidAuthnRequestException;

import java.text.MessageFormat;
import java.util.List;

public class LoaValidator {
    public void validate(RequestedAuthnContext requestedAuthnContext) {
        if (requestedAuthnContext == null) throw new InvalidAuthnRequestException("Missing RequestedAuthnContext");

        List<AuthnContextClassRef> authnContextClassRefs = requestedAuthnContext.getAuthnContextClassRefs();
        if (authnContextClassRefs.isEmpty()) throw new InvalidAuthnRequestException("Missing LoA");

        String loa = authnContextClassRefs.get(0).getAuthnContextClassRef();
        if (Strings.isNullOrEmpty(loa)) throw new InvalidAuthnRequestException("Missing LoA");

        if (!EidasConstants.EIDAS_LOA_SUBSTANTIAL.equals(loa)) {
            String message = MessageFormat.format("Invalid LoA ''{0}''", loa);
            throw new InvalidAuthnRequestException(message);
        }
    }
}
