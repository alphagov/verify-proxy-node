package uk.gov.ida.notification;

import io.dropwizard.Configuration;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.ida.configuration.ServiceNameConfiguration;
import uk.gov.ida.notification.configuration.ReplayCheckerConfiguration;
import uk.gov.ida.notification.pki.KeyPairConfiguration;
import uk.gov.ida.saml.metadata.TrustStoreBackedMetadataConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.net.URI;

public class TranslatorConfiguration extends Configuration implements ServiceNameConfiguration {

    @JsonProperty
    @Valid
    @NotNull
    private URI hubUrl;

    @JsonProperty
    @Valid
    @NotNull
    private URI connectorNodeUrl;

    @JsonProperty
    @Valid
    @NotNull
    private String proxyNodeEntityId;

    @JsonProperty
    @Valid
    @NotNull
    private URI proxyNodeResponseUrl;

    @JsonProperty
    @Valid
    @NotNull
    private URI proxyNodeMetadataForConnectorNodeUrl;

    @JsonProperty
    @Valid
    @NotNull
    private KeyPairConfiguration hubFacingEncryptionKeyPair;

    @JsonProperty
    @Valid
    @NotNull
    private KeyPairConfiguration connectorFacingSigningKeyPair;

    @JsonProperty
    @Valid
    @NotNull
    private String connectorNodeIssuerId;

    @JsonProperty
    @Valid
    @NotNull
    private TrustStoreBackedMetadataConfiguration connectorMetadataConfiguration;

    @JsonProperty
    @Valid
    @NotNull
    private TrustStoreBackedMetadataConfiguration hubMetadataConfiguration;

    @JsonProperty
    @Valid
    private ReplayCheckerConfiguration replayChecker = new ReplayCheckerConfiguration();

    @JsonProperty
    @Valid
    @NotNull
    private String keyRetrieverServiceName = "";

    @JsonProperty
    @Valid
    @NotNull
    private String softHSMLibPath = "";

    @JsonProperty
    @Valid
    @NotNull
    private String softHSMSigningKeyPin = "";

    @JsonProperty
    @Valid
    @NotNull
    private String softHSMSigningKeyLabel = "";


    public URI getHubUrl() {
        return hubUrl;
    }

    public URI getConnectorNodeUrl() {
        return connectorNodeUrl;
    }

    public String getProxyNodeEntityId() {
        return proxyNodeEntityId;
    }

    public KeyPairConfiguration getHubFacingEncryptionKeyPair() {
        return hubFacingEncryptionKeyPair;
    }

    public KeyPairConfiguration getConnectorFacingSigningKeyPair() {
        return connectorFacingSigningKeyPair;
    }

    public URI getProxyNodeMetadataForConnectorNodeUrl() {
        return proxyNodeMetadataForConnectorNodeUrl;
    }

    public String getConnectorNodeIssuerId() {
        return connectorNodeIssuerId;
    }

    public TrustStoreBackedMetadataConfiguration getConnectorMetadataConfiguration() {
        return connectorMetadataConfiguration;
    }

    public TrustStoreBackedMetadataConfiguration getHubMetadataConfiguration() {
        return hubMetadataConfiguration;
    }

    public URI getProxyNodeResponseUrl() {
        return proxyNodeResponseUrl;
    }

    public ReplayCheckerConfiguration getReplayChecker() {
        return replayChecker;
    }

    public String getKeyRetrieverServiceName() { return keyRetrieverServiceName; }

    public String getSoftHSMLibPath() { return softHSMLibPath; }

    public String getSoftHSMSigningKeyPin() { return softHSMSigningKeyPin; }

    public String getSoftHSMSigningKeyLabel() { return softHSMSigningKeyLabel; }

    @Override
    public String getServiceName() {
        return "translator";
    }
}
