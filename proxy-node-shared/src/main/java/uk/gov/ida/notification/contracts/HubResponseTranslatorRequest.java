package uk.gov.ida.notification.contracts;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.opensaml.security.x509.X509Credential;

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
    private X509Credential connectorEncryptionCredential;

    public HubResponseTranslatorRequest(String samlResponse, String requestId, String levelOfAssurance, String eidasRequestId, X509Credential connectorEncryptionCredential) {
        this(samlResponse, requestId, levelOfAssurance);

        this.eidasRequestId = eidasRequestId;
        this.connectorEncryptionCredential = connectorEncryptionCredential;
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

    public X509Credential getConnectorEncryptionCredential() {
        return connectorEncryptionCredential;
    }

    public String getLevelOfAssurance() {
        return levelOfAssurance;
    }
}
