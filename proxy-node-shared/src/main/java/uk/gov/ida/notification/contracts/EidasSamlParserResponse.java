package uk.gov.ida.notification.contracts;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;

public class EidasSamlParserResponse {

    @JsonProperty
    @NotBlank
    private String requestId;

    @JsonProperty
    @NotBlank
    private String issuer;

    @NotNull
    @JsonProperty
    private String connectorEncryptionPublicCertificate;

    @JsonProperty
    @NotBlank
    private String destination;

    @SuppressWarnings("Needed for serialisaiton")
    public EidasSamlParserResponse() {
    }

    public EidasSamlParserResponse(String requestId, String issuer, String connectorEncryptionPublicCertificate, String destination) {
        this.requestId = requestId;
        this.issuer = issuer;
        this.connectorEncryptionPublicCertificate = connectorEncryptionPublicCertificate;
        this.destination = destination;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getIssuer() {
        return issuer;
    }

    public String getConnectorEncryptionPublicCertificate() {
        return connectorEncryptionPublicCertificate;
    }

    public String getDestination() {
        return destination;
    }
}
