package uk.gov.ida.notification.contracts;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import java.net.URI;

public class HubResponseTranslatorRequest {

    @NotNull
    @JsonProperty
    private String samlResponse;

    @NotNull
    @JsonProperty
    private String requestId;

    @NotNull
    @JsonProperty
    private String eidasRequestId;

    @NotNull
    @JsonProperty
    private String levelOfAssurance;

    @NotNull
    @JsonProperty
    private URI destinationUrl;

    @NotNull
    @JsonProperty
    private String connectorEncryptionCertificate;

    @SuppressWarnings("Needed for JSON serialisation")
    public HubResponseTranslatorRequest() {
    }

    public HubResponseTranslatorRequest(
            String samlResponse,
            String requestId,
            String eidasRequestId,
            String levelOfAssurance,
            URI destinationUrl,
            String connectorEncryptionCertificate) {
        this.samlResponse = samlResponse;
        this.requestId = requestId;
        this.eidasRequestId = eidasRequestId;
        this.levelOfAssurance = levelOfAssurance;
        this.destinationUrl = destinationUrl;
        this.connectorEncryptionCertificate = connectorEncryptionCertificate;
    }

    public String getSamlResponse() {
        return samlResponse;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getEidasRequestId() {
        return eidasRequestId;
    }

    public String getConnectorEncryptionCertificate() {
        return connectorEncryptionCertificate;
    }

    public String getLevelOfAssurance() {
        return levelOfAssurance;
    }

    public URI getDestinationUrl() {
        return destinationUrl;
    }
}
