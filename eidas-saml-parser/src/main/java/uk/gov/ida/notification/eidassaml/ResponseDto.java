package uk.gov.ida.notification.eidassaml;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ResponseDto {

    @JsonProperty
    public String requestId;

    @JsonProperty
    public String issuer;

    @JsonProperty
    public String connectorPublicEncryptionKey;

    public ResponseDto() {}

    public ResponseDto(String requestId, String issuer, String connectorPublicEncryptionKey) {
        this.requestId = requestId;
        this.issuer = issuer;
        this.connectorPublicEncryptionKey = connectorPublicEncryptionKey;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getIssuer() {
        return issuer;
    }

    public String getConnectorPublicEncryptionKey() {
        return connectorPublicEncryptionKey;
    }
}
