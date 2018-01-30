package uk.gov.ida.notification.saml.translation;

import org.joda.time.DateTime;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthnStatement;
import uk.gov.ida.notification.exceptions.HubResponseException;

import java.util.List;

public class HubAuthnStatement {
    private final String pid;
    private final String providedLoa;
    private final DateTime authnInstant;

    public HubAuthnStatement(String pid, String providedLoa, DateTime authnInstant) {
        this.pid = pid;
        this.providedLoa = providedLoa;
        this.authnInstant = authnInstant;
    }


    public static HubAuthnStatement fromAssertions(List<Assertion> assertions) {
        Assertion authnAssertion = assertions
                .stream()
                .filter(a -> !a.getAuthnStatements().isEmpty())
                .findFirst()
                .orElseThrow(() -> new HubResponseException("Hub Response has no authn assertion"));

        String pid = authnAssertion.getSubject().getNameID().getValue();

        AuthnStatement authnStatement = authnAssertion.getAuthnStatements().get(0);
        String providedLoa = authnStatement.getAuthnContext().getAuthnContextClassRef().getAuthnContextClassRef();

        DateTime authnInstant = authnStatement.getAuthnInstant();

        return new HubAuthnStatement(pid, providedLoa, authnInstant);
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
