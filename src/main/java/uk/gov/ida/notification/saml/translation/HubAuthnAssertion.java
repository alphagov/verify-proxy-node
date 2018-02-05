package uk.gov.ida.notification.saml.translation;

import org.joda.time.DateTime;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthnStatement;
import uk.gov.ida.notification.exceptions.HubResponseTranslationException;

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

    public static HubAuthnAssertion fromAssertions(List<Assertion> assertions) {
        Assertion authnAssertion = assertions
                .stream()
                .filter(a -> !a.getAuthnStatements().isEmpty())
                .findFirst()
                .orElseThrow(() -> new HubResponseTranslationException("Hub Response has no authn assertion"));

        String pid = authnAssertion.getSubject().getNameID().getValue();

        AuthnStatement authnStatement = authnAssertion.getAuthnStatements().get(0);
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
