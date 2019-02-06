package uk.gov.ida.notification.verifyserviceprovider;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;

public class RequestGenerationBody {

    private final String levelOfAssurance;
    private final String entityId;

    @JsonCreator
    public RequestGenerationBody(
            @JsonProperty("levelOfAssurance") String levelOfAssurance,
            @JsonProperty("entityId") String entityId) {
        this.levelOfAssurance = levelOfAssurance;
        this.entityId = entityId;
    }

    @NotNull
    public String getLevelOfAssurance() {
        return levelOfAssurance;
    }

    public String getEntityId() {
        return entityId;
    }
}
