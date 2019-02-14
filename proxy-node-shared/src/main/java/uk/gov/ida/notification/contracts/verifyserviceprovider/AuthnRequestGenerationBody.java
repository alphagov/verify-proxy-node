package uk.gov.ida.notification.contracts.verifyserviceprovider;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;

public class AuthnRequestGenerationBody {

    @NotNull
    @JsonProperty
    private String levelOfAssurance;

    @SuppressWarnings("Needed for JSON serialisation")
    public AuthnRequestGenerationBody() {
    }

    public AuthnRequestGenerationBody(
            String levelOfAssurance) {

        this.levelOfAssurance = levelOfAssurance;
    }

    public String getLevelOfAssurance() {
        return levelOfAssurance;
    }
}
