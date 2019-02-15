package uk.gov.ida.notification.contracts.verifyserviceprovider;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;

public class TranslatedHubResponse {

    @NotNull
    @JsonProperty
    private VspScenario scenario;

    @NotNull
    @JsonProperty
    private String pid;

    @NotNull
    @JsonProperty
    private VspLevelOfAssurance levelOfAssurance;

    @NotNull
    @JsonProperty
    private Attributes attributes;

    @SuppressWarnings("Needed for JSON serialisation")
    public TranslatedHubResponse() {
    }

    public TranslatedHubResponse(
            VspScenario scenario,
            String pid,
            VspLevelOfAssurance levelOfAssurance,
            Attributes attributes) {
        this.scenario = scenario;
        this.pid = pid;
        this.levelOfAssurance = levelOfAssurance;
        this.attributes = attributes;
    }

    public VspScenario getScenario() {
        return scenario;
    }

    public String getPid() {
        return pid;
    }

    public VspLevelOfAssurance getLevelOfAssurance() {
        return levelOfAssurance;
    }

    public Attributes getAttributes() {
        return attributes;
    }
}
