package uk.gov.ida.notification.contracts;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;

public class TranslatedHubResponse {

    @NotNull
    @JsonProperty
    private String scenario;

    @NotNull
    @JsonProperty
    private String pid;

    @NotNull
    @JsonProperty
    private String levelOfAssurance;

    @NotNull
    @JsonProperty
    private String attributes;

    public TranslatedHubResponse(String scenario, String pid, String levelOfAssurance, String attributes) {
        this.scenario = scenario;
        this.pid = pid;
        this.levelOfAssurance = levelOfAssurance;
        this.attributes = attributes;
    }
}
