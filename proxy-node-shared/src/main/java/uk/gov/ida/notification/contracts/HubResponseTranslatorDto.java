package uk.gov.ida.notification.contracts;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.opensaml.security.x509.X509Credential;

import javax.validation.constraints.NotNull;

public class HubResponseTranslatorDto {

    @NotNull
    @JsonProperty
    private String samlResponse;

    @NotNull
    @JsonProperty
    private String requestId;

    @JsonProperty
    private String eidasRequestId;

    @JsonProperty
    private X509Credential connectorEncryptionCredential;

    @NotNull
    @JsonProperty
    private String levelOfAssurance;

    public HubResponseTranslatorDto(String samlResponse, String requestId, String levelOfAssurance, String eidasRequestId, X509Credential connectorEncryptionCredential) {
        this(samlResponse, requestId, levelOfAssurance);

        this.eidasRequestId = eidasRequestId;
        this.connectorEncryptionCredential = connectorEncryptionCredential;
    }

    public HubResponseTranslatorDto(String samlResponse, String requestId, String levelOfAssurance) {
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

    public X509Credential getConnectorEncryptionCredential() {
        return connectorEncryptionCredential;
    }

    public String getLevelOfAssurance() {
        return levelOfAssurance;
    }
}
