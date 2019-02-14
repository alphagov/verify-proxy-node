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

    public VerifyServiceProviderTranslationRequest(String samlResponse, String requestId, String levelOfAssurance) {
        this.samlResponse = samlResponse;
        this.requestId = requestId;
        this.levelOfAssurance = levelOfAssurance;
    }
}
