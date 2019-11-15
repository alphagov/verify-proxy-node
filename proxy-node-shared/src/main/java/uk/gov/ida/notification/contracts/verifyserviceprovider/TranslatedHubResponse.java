package uk.gov.ida.notification.contracts.verifyserviceprovider;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.ida.verifyserviceprovider.dto.NonMatchingAttributes;

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
    private NonMatchingAttributes attributes;

    @SuppressWarnings("Needed for JSON serialisation")
    public TranslatedHubResponse() {
    }

    public TranslatedHubResponse(
            VspScenario scenario,
            String pid,
            VspLevelOfAssurance levelOfAssurance,
            NonMatchingAttributes attributes) {
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

    public NonMatchingAttributes getAttributes() {
        return attributes;
    }
}
