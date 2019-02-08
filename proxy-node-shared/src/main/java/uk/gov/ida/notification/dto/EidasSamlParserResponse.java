package uk.gov.ida.notification.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotBlank;

public class EidasSamlParserResponse {

    @JsonProperty
    @NotBlank
    private String requestId;

    @JsonProperty
    @NotBlank
    private String issuer;

    @JsonProperty
    @NotBlank
    private String connectorPublicEncryptionKey;

    @JsonProperty
    @NotBlank
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
