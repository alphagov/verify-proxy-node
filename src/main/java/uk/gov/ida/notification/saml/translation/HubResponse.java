package uk.gov.ida.notification.saml.translation;

import org.joda.time.DateTime;
import uk.gov.ida.saml.security.validators.ValidatedResponse;

public class HubResponse {
    private final String statusCode;
    private final String responseId;
    private final String inResponseTo;
    private final DateTime issueInstant;

    public HubResponse(String statusCode, String responseId, String inResponseTo, DateTime issueInstant) {
        this.statusCode = statusCode;
        this.responseId = responseId;
        this.inResponseTo = inResponseTo;
        this.issueInstant = issueInstant;
    }

    public static HubResponse from(ValidatedResponse response) {
        String statusCode = response.getStatus().getStatusCode().getValue();

        String responseId = response.getID();

        String inResponseTo = response.getInResponseTo();

        DateTime issueInstant = response.getIssueInstant();

        return new HubResponse(statusCode, responseId, inResponseTo, issueInstant);
    }

    public String getStatusCode() {
        return statusCode;
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
}
