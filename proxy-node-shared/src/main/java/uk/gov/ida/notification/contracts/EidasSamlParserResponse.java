package uk.gov.ida.notification.contracts;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import java.security.cert.X509Certificate;

public class EidasSamlParserResponse {

    @JsonProperty
    @NotBlank
    private String requestId;

    @JsonProperty
    @NotBlank
    private String issuer;

    @NotNull
    @JsonProperty
    @JsonSerialize(using = X509CertificateSerializer.class)
    @JsonDeserialize(using = X509CertificateDeserializer.class)
    private X509Certificate connectorEncryptionPublicCertificate;

    @JsonProperty
    @NotBlank
    private String destination;

    private EidasSamlParserResponse() {
    }

    public EidasSamlParserResponse(String requestId, String issuer, X509Certificate connectorEncryptionPublicCertificate, String destination) {
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

    public X509Certificate getConnectorEncryptionPublicCertificate() {
        return connectorEncryptionPublicCertificate;
    }

    public String getDestination() {
        return destination;
    }
}
