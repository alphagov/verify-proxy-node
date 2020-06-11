package uk.gov.ida.notification.contracts;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotBlank;
import uk.gov.ida.notification.validations.ValidDestinationUriString;
import uk.gov.ida.notification.validations.ValidSamlId;

public class EidasSamlParserResponse {

    @JsonProperty
    @NotBlank
    @ValidSamlId
    private String requestId;

    @JsonProperty
    @NotBlank
    private String issuerEntityId;

    @JsonProperty
    @NotBlank
    @ValidDestinationUriString
    private String assertionConsumerServiceLocation;

    @JsonProperty
    private boolean transientPidRequested;

    @SuppressWarnings("Needed for serialisaiton")
    public EidasSamlParserResponse() {
    }

    public EidasSamlParserResponse(String requestId, String issuerEntityId, String assertionConsumerServiceLocation, boolean transientPidRequested) {
        this.requestId = requestId;
        this.issuerEntityId = issuerEntityId;
        this.assertionConsumerServiceLocation = assertionConsumerServiceLocation;
        this.transientPidRequested = transientPidRequested;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getIssuerEntityId() {
        return issuerEntityId;
    }

    public String getAssertionConsumerServiceLocation() {
        return assertionConsumerServiceLocation;
    }

    public boolean isTransientPidRequested() {
        return transientPidRequested;
    }
}
