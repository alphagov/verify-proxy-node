package uk.gov.ida.notification.saml.validation.components;

import com.google.common.base.Strings;
import org.opensaml.saml.saml2.core.AuthnContext;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.RequestedAuthnContext;
import se.litsec.eidas.opensaml.common.EidasConstants;
import uk.gov.ida.notification.exceptions.authnrequest.InvalidAuthnRequestException;
import uk.gov.ida.notification.exceptions.hubresponse.InvalidHubResponseException;
import uk.gov.ida.saml.core.extensions.IdaAuthnContext;

import java.text.MessageFormat;
import java.util.List;

public class LoaValidator {
    public void validate(RequestedAuthnContext requestedAuthnContext) {
        if (requestedAuthnContext == null) throw new InvalidAuthnRequestException("Missing RequestedAuthnContext");

        List<AuthnContextClassRef> authnContextClassRefs = requestedAuthnContext.getAuthnContextClassRefs();
        if (authnContextClassRefs.isEmpty()) throw new InvalidAuthnRequestException("Missing LoA");

        String loa = authnContextClassRefs.get(0).getAuthnContextClassRef();
        if (Strings.isNullOrEmpty(loa)) throw new InvalidAuthnRequestException("Missing LoA");

        if (!EidasConstants.EIDAS_LOA_SUBSTANTIAL.equals(loa) && !EidasConstants.EIDAS_LOA_LOW.equals(loa)) {
            String message = MessageFormat.format("Invalid LoA ''{0}''", loa);
            throw new InvalidAuthnRequestException(message);
        }
    }

    public void validate(AuthnContext authnContext) {
        if (authnContext == null) throw new InvalidHubResponseException("Missing AuthnContext");

        AuthnContextClassRef authnContextClassRef = authnContext.getAuthnContextClassRef();
        if (authnContextClassRef == null) throw new InvalidHubResponseException("Missing LoA (AuthnContextClassRef)");

        String loa = authnContextClassRef.getAuthnContextClassRef();
        if (Strings.isNullOrEmpty(loa)) throw new InvalidHubResponseException("Missing LoA (AuthnContextClassRef value)");

        if (!IdaAuthnContext.LEVEL_2_AUTHN_CTX.equals(loa) && !IdaAuthnContext.LEVEL_1_AUTHN_CTX.equals(loa)) {
            throw new InvalidHubResponseException(MessageFormat.format("Invalid LoA ''{0}''", loa));
        }
   }
}
