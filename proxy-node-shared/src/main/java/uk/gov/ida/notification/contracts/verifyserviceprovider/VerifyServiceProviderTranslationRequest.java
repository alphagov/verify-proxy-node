package uk.gov.ida.notification.contracts.verifyserviceprovider;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;

public class VerifyServiceProviderTranslationRequest {

    @NotNull
    @JsonProperty
    private String samlResponse;

    @NotNull
    @JsonProperty
    private String requestId;

    @NotNull
    @JsonProperty
    private String levelOfAssurance;

    @SuppressWarnings("Default Constructor Needed for JSON serialisation")
    public VerifyServiceProviderTranslationRequest() {
    }

    public VerifyServiceProviderTranslationRequest(String samlResponse, String requestId, String levelOfAssurance) {
        this.samlResponse = samlResponse;
        this.requestId = requestId;
        this.levelOfAssurance = levelOfAssurance;
    }

    public String getSamlResponse() {
        return samlResponse;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getLevelOfAssurance() {
        return levelOfAssurance;
    }
}