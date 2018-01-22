package uk.gov.ida.notification.saml.translation;

import org.joda.time.DateTime;
import org.opensaml.saml.saml2.core.*;
import uk.gov.ida.notification.exceptions.HubResponseException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HubResponse {
    private final String pid;
    private final String statusCode;
    private final String providedLoa;
    private final String responseId;
    private final String inResponseTo;
    private final Map<String, AttributeValue> mdsAttributes;
    private DateTime issueInstant;
    private DateTime assertionIssueInstant;
    private DateTime authnStatementAuthnInstant;

    public HubResponse(String pid, String statusCode, String providedLoa, String responseId, String inResponseTo, Map<String, AttributeValue> mdsAttributes, DateTime issueInstant, DateTime assertionIssueInstant, DateTime authnStatementAuthnInstant) {
        this.pid = pid;
        this.statusCode = statusCode;
        this.providedLoa = providedLoa;
        this.responseId = responseId;
        this.inResponseTo = inResponseTo;
        this.mdsAttributes = mdsAttributes;
        this.issueInstant = issueInstant;
        this.assertionIssueInstant = assertionIssueInstant;
        this.authnStatementAuthnInstant = authnStatementAuthnInstant;
    }

    public static HubResponse fromResponse(Response response) {
        List<Assertion> assertions = response.getAssertions();
        Assertion authnAssertion = assertions
                .stream()
                .filter(a -> !a.getAuthnStatements().isEmpty())
                .findFirst()
                .orElseThrow(() -> new HubResponseException("Hub Response has no authn assertion"));

        Assertion mdsAssertion = assertions
                .stream()
                .filter(a -> a.getAuthnStatements().isEmpty() && !a.getAttributeStatements().isEmpty())
                .findFirst()
                .orElseThrow(() -> new HubResponseException("Hub Response has no MDS assertion"));

        String pid = authnAssertion.getSubject().getNameID().getValue();

        AuthnStatement authnStatement = authnAssertion.getAuthnStatements().get(0);
        String providedLoa = authnStatement.getAuthnContext().getAuthnContextClassRef().getAuthnContextClassRef();

        DateTime assertionIssueInstant = response.getAssertions().get(0).getIssueInstant();

        Map<String, AttributeValue> mdsAttributes = mdsAssertion.getAttributeStatements().get(0).getAttributes().stream()
                .collect(Collectors.toMap(
                        Attribute::getName,
                        a -> (AttributeValue) a.getAttributeValues().get(0)));

        String statusCode = response.getStatus().getStatusCode().getValue();

        String responseId = response.getID();

        String inResponseTo = response.getInResponseTo();

        DateTime issueInstant = response.getIssueInstant();

        DateTime authnStatementAuthnInstant = authnStatement.getAuthnInstant();

        return new HubResponse(pid, statusCode, providedLoa, responseId, inResponseTo, mdsAttributes, issueInstant, assertionIssueInstant, authnStatementAuthnInstant);
    }

    public String getPid() {
        return pid;
    }

    public <T extends AttributeValue> T getMdsAttribute(String key, Class<T> castClazz) {
        return castClazz.cast(mdsAttributes.get(key));
    }

    public String getStatusCode() {
        return statusCode;
    }

    public String getProvidedLoa() {
        return providedLoa;
    }

    public String getInResponseTo() {
        return inResponseTo;
    }

    public String getResponseId() {
        return responseId;
    }

    public DateTime getIssueInstant() {
        return issueInstant;
    }

    public DateTime getAssertionIssueInstant() {
        return assertionIssueInstant;
    }

    public DateTime getAuthnStatementAuthnInstant() {
        return authnStatementAuthnInstant;
    }
}
