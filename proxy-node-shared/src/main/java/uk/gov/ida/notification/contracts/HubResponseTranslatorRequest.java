package uk.gov.ida.notification.contracts;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;

public class HubResponseTranslatorRequest {

    @NotNull
    @JsonProperty
    private String samlResponse;

    @NotNull
    @JsonProperty
    private String requestId;

    @NotNull
    @JsonProperty
    private String levelOfAssurance;

    @NotNull
    @JsonProperty
    private String eidasRequestId;

    @NotNull
    @JsonProperty
    private String connectorEncryptionCertificate;

    public HubResponseTranslatorRequest(String samlResponse, String requestId, String levelOfAssurance, String eidasRequestId, String connectorEncryptionCertificate) {
        this(samlResponse, requestId, levelOfAssurance);

        this.eidasRequestId = eidasRequestId;
        this.connectorEncryptionCertificate = connectorEncryptionCertificate;
    }

    public HubResponseTranslatorRequest(String samlResponse, String requestId, String levelOfAssurance) {
        this.samlResponse = samlResponse;
        this.requestId = requestId;
        this.levelOfAssurance = levelOfAssurance;
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
}
