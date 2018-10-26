package uk.gov.ida.notification.saml.translation;

import org.joda.time.DateTime;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthnStatement;
import uk.gov.ida.notification.exceptions.hubresponse.HubResponseTranslationException;

import java.util.List;

public class HubAuthnAssertion {
    private final String pid;
    private final String providedLoa;
    private final DateTime authnInstant;

    public HubAuthnAssertion(String pid, String providedLoa, DateTime authnInstant) {
        this.pid = pid;
        this.providedLoa = providedLoa;
        this.authnInstant = authnInstant;
    }

    public static HubAuthnAssertion fromAssertion(Assertion assertion) {
        String pid = assertion.getSubject().getNameID().getValue();

        AuthnStatement authnStatement = assertion.getAuthnStatements().get(0);
        String providedLoa = authnStatement.getAuthnContext().getAuthnContextClassRef().getAuthnContextClassRef();

        DateTime authnInstant = authnStatement.getAuthnInstant();

        return new HubAuthnAssertion(pid, providedLoa, authnInstant);
    }


    public String getPid() {
        return pid;
    }

    public String getProvidedLoa() {
        return providedLoa;
    }

    public DateTime getAuthnInstant() {
        return authnInstant;
    }
}
