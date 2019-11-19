package uk.gov.ida.notification.contracts.verifyserviceprovider;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.ida.verifyserviceprovider.dto.NonMatchingAttributes;

import javax.validation.constraints.NotNull;

import java.util.Optional;

import static uk.gov.ida.notification.contracts.verifyserviceprovider.Attributes.fromNonMatchingAttributes;

public class TranslatedHubResponse {

    @NotNull
    @JsonProperty
    private VspScenario scenario;

    @JsonProperty
    private String pid;

    @JsonProperty
    private VspLevelOfAssurance levelOfAssurance;

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

    public Optional<String> getPid() {
        return Optional.ofNullable(pid);
    }

    public Optional<VspLevelOfAssurance> getLevelOfAssurance() {
        return Optional.ofNullable(levelOfAssurance);
    }

    public Optional<Attributes> getAttributes() {
        return fromNonMatchingAttributes(attributes);
    }
}
