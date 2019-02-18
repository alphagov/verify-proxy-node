package uk.gov.ida.notification.translator.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import uk.gov.ida.notification.configuration.VerifyServiceProviderConfiguration;
import uk.gov.ida.notification.pki.KeyPairConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;

public class TranslatorConfiguration extends Configuration {

    @Valid
    @NotNull
    @JsonProperty
    private URI proxyNodeMetadataForConnectorNodeUrl;

    @Valid
    @NotNull
    @JsonProperty
    private VerifyServiceProviderConfiguration vspConfiguration;

    // TODO: This should be retrieved from the HSM instead and removed from config in EID-1289
    @Valid
    @NotNull
    @JsonProperty
    private KeyPairConfiguration connectorFacingSigningKeyPair;

    @Valid
    @NotNull
    @JsonProperty
    private String connectorNodeIssuerId;

    @Valid
    @NotNull
    @JsonProperty
    private String keyRetrieverServiceName = "";

    @Valid
    @NotNull
    @JsonProperty
    private String softHSMLibPath = "";

    @Valid
    @NotNull
    @JsonProperty
    private String softHSMSigningKeyPin = "";

    @Valid
    @NotNull
    @JsonProperty
    private String softHSMSigningKeyLabel = "";


    public KeyPairConfiguration getConnectorFacingSigningKeyPair() {
        return connectorFacingSigningKeyPair;
    }

    public URI getProxyNodeMetadataForConnectorNodeUrl() {
        return proxyNodeMetadataForConnectorNodeUrl;
    }

    public VerifyServiceProviderConfiguration getVspConfiguration() {
        return vspConfiguration;
    }

    public String getConnectorNodeIssuerId() {
        return connectorNodeIssuerId;
    }

    public String getKeyRetrieverServiceName() { return keyRetrieverServiceName; }

    public String getSoftHSMLibPath() { return softHSMLibPath; }

    public String getSoftHSMSigningKeyPin() { return softHSMSigningKeyPin; }

    public String getSoftHSMSigningKeyLabel() { return softHSMSigningKeyLabel; }
}
