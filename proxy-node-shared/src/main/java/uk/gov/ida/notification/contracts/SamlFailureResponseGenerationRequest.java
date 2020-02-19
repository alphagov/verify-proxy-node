package uk.gov.ida.notification.contracts;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotBlank;
import uk.gov.ida.notification.validations.ValidDestinationUriString;
import uk.gov.ida.notification.validations.ValidSamlId;

import java.net.URI;

import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Response;

public class SamlFailureResponseGenerationRequest {

    @NotNull
    @JsonProperty
    private Response.Status responseStatus;

    @NotBlank
    @JsonProperty
    @ValidSamlId
    private String eidasRequestId;

    @NotBlank
    @JsonProperty
    @ValidDestinationUriString
    private String destinationUrl;

    @NotNull
    @JsonProperty
    private URI entityId;

    @SuppressWarnings("Needed for JSON serialisation")
    public SamlFailureResponseGenerationRequest() {
    }

    public SamlFailureResponseGenerationRequest(
            Response.Status responseStatus,
            String eidasRequestId,
            String destinationUrl,
            URI entityId) {
        this.responseStatus = responseStatus;
        this.eidasRequestId = eidasRequestId;
        this.destinationUrl = destinationUrl;
        this.entityId = entityId;
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

    public URI getEntityId() {
        return entityId;
    }
}
