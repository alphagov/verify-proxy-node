package uk.gov.ida.notification.contracts.verifyserviceprovider;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;

public class AuthnRequestGenerationBody {

    @NotNull
    @JsonProperty
    private String levelOfAssurance;

    @JsonCreator
    public AuthnRequestGenerationBody(
            @JsonProperty("levelOfAssurance") String levelOfAssurance) {

        this.levelOfAssurance = levelOfAssurance;
    }

    public String getLevelOfAssurance() {
        return levelOfAssurance;
    }
}
