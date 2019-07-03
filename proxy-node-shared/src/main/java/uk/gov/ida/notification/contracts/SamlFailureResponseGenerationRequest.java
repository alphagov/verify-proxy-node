package uk.gov.ida.notification.contracts;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.ida.notification.validations.ValidDestinationUriString;
import uk.gov.ida.notification.validations.ValidSamlId;

import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Response;

public class SamlFailureResponseGenerationRequest {

    @JsonProperty
    @NotNull
    private Response.Status responseStatus;

    @JsonProperty
    @NotNull
    @ValidSamlId
    private String eidasRequestId;

    @JsonProperty
    @NotNull
    @ValidDestinationUriString
    private String destinationUrl;

    @SuppressWarnings("Needed for JSON serialisation")
    public SamlFailureResponseGenerationRequest() {
    }

    public SamlFailureResponseGenerationRequest(Response.Status responseStatus, String eidasRequestId, String destinationUrl) {
        this.responseStatus = responseStatus;
        this.eidasRequestId = eidasRequestId;
        this.destinationUrl = destinationUrl;
    }

    public Response.Status getResponseStatus() {
        return responseStatus;
    }

    public String getEidasRequestId() {
        return eidasRequestId;
    }

    public String getDestinationUrl() {
        return destinationUrl;
    }
}
