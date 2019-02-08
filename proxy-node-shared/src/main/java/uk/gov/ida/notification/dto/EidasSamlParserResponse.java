package uk.gov.ida.notification.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;

public class EidasSamlParserResponse {

    @JsonProperty
    @NotNull
    private String requestId;

    @JsonProperty
    @NotNull
    private String issuer;

    @JsonProperty
    @NotNull
    private String connectorPublicEncryptionKey;

    @JsonProperty
    @NotNull
    private String destination;

    public EidasSamlParserResponse() {
    }

    public EidasSamlParserResponse(String requestId, String issuer, String connectorPublicEncryptionKey, String destination) {
        this.requestId = requestId;
        this.issuer = issuer;
        this.connectorPublicEncryptionKey = connectorPublicEncryptionKey;
        this.destination = destination;
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

    public String getDestination() {
        return destination;
    }
}
