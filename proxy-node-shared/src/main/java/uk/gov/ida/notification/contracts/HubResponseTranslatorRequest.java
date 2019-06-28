package uk.gov.ida.notification.contracts;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.ida.notification.validations.ValidBase64SamlResponse;
import uk.gov.ida.notification.validations.ValidDestinationUri;
import uk.gov.ida.notification.validations.ValidLOA;
import uk.gov.ida.notification.validations.ValidPEM;
import uk.gov.ida.notification.validations.ValidSamlId;

import javax.validation.constraints.NotNull;
import java.net.URI;

public class HubResponseTranslatorRequest {

    @NotNull
    @ValidBase64SamlResponse
    @JsonProperty
    private String samlResponse;

    @NotNull
    @ValidSamlId
    @JsonProperty
    private String requestId;

    @NotNull
    @ValidSamlId
    @JsonProperty
    private String eidasRequestId;

    @NotNull
    @ValidLOA
    @JsonProperty
    private String levelOfAssurance;

    @NotNull
    @ValidDestinationUri
    @JsonProperty
    private URI destinationUrl;

    @NotNull
    @ValidPEM
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
