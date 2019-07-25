package uk.gov.ida.notification.contracts;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotBlank;
import uk.gov.ida.notification.validations.ValidDestinationUriString;
import uk.gov.ida.notification.validations.ValidPEM;
import uk.gov.ida.notification.validations.ValidSamlId;

public class EidasSamlParserResponse {

    @JsonProperty
    @NotBlank
    @ValidSamlId
    private String requestId;

    @JsonProperty
    @NotBlank
    private String issuer;

    @JsonProperty
    @NotBlank
    @ValidPEM
    private String connectorEncryptionPublicCertificate;

    @JsonProperty
    @NotBlank
    @ValidDestinationUriString
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
