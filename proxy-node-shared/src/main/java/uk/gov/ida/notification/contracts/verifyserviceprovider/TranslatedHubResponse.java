package uk.gov.ida.notification.contracts.verifyserviceprovider;

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
    private Attributes attributes;

    @SuppressWarnings("Needed for JSON serialisation")
    public TranslatedHubResponse() {
    }

    public TranslatedHubResponse(
            String scenario,
            String pid,
            String levelOfAssurance,
            Attributes attributes) {
        this.scenario = scenario;
        this.pid = pid;
        this.levelOfAssurance = levelOfAssurance;
        this.attributes = attributes;
    }

    public String getScenario() {
        return scenario;
    }

    public String getPid() {
        return pid;
    }

    public String getLevelOfAssurance() {
        return levelOfAssurance;
    }

    public Attributes getAttributes() {
        return attributes;
    }
}
