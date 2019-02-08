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
}
