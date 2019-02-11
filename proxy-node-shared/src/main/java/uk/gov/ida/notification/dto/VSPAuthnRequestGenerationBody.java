package uk.gov.ida.notification.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class VSPAuthnRequestGenerationBody {

    @JsonProperty
    @Valid
    @NotNull
    private String levelOfAssurance;

    public VSPAuthnRequestGenerationBody(String levelOfAssurance) {
        this.levelOfAssurance = levelOfAssurance;
    }

    public String getLevelOfAssurance() {
        return levelOfAssurance;
    }
}