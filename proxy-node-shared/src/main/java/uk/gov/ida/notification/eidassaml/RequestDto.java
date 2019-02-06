package uk.gov.ida.notification.eidassaml;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;

public class RequestDto {
    @JsonProperty @NotNull
    public String authnRequest;
}
