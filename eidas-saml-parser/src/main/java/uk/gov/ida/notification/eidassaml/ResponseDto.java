package uk.gov.ida.notification.eidassaml;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ResponseDto {

    @JsonProperty
    public String requestId;

    @JsonProperty
    public String issuer;

    public ResponseDto() {}

    public ResponseDto(String requestId, String issuer) {
        this.requestId = requestId;
        this.issuer = issuer;
    }
}
