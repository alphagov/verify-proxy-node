package uk.gov.ida.notification.contracts;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;

public class VSPAuthnRequestResponse {
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

    public VSPAuthnRequestResponse() {
    }

    public VSPAuthnRequestResponse(String samlRequest, String requestId, URI ssoLocation) {
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
