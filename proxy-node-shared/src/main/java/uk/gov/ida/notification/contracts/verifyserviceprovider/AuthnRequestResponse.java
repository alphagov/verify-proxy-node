package uk.gov.ida.notification.contracts.verifyserviceprovider;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;

public class AuthnRequestResponse {

    @JsonProperty
    @Valid
    @NotNull
    private String samlRequest;

    @JsonProperty
    @Valid
    @NotNull
    private String requestId;

    @JsonProperty
    @Valid
    @NotNull
    private URI ssoLocation;

    @SuppressWarnings("Needed for JSON serialisation")
    public AuthnRequestResponse() {
    }

    public AuthnRequestResponse(
            String samlRequest,
            String requestId,
            URI ssoLocation) {
        this.samlRequest = samlRequest;
        this.requestId = requestId;
        this.ssoLocation = ssoLocation;
    }

    public String getSamlRequest() {
        return samlRequest;
    }

    public String getRequestId() {
        return requestId;
    }

    public URI getSsoLocation() {
        return ssoLocation;
    }
}
