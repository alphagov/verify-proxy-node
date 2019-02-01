package uk.gov.ida.notification;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import uk.gov.ida.configuration.ServiceNameConfiguration;
import uk.gov.ida.notification.configuration.ReplayCheckerConfiguration;
import uk.gov.ida.notification.pki.KeyPairConfiguration;
import uk.gov.ida.saml.metadata.TrustStoreBackedMetadataConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;

public class GatewayConfiguration extends Configuration implements ServiceNameConfiguration {

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
    private TranslatorServiceConfiguration translatorService;

    @JsonProperty
    @Valid
    @NotNull
    private String proxyNodeEntityId;

    @JsonProperty
    @Valid
    @NotNull
    private URI proxyNodeAuthnRequestUrl;

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
    private KeyPairConfiguration connectorFacingSigningKeyPair;

    @JsonProperty
    @Valid
    @NotNull
    private KeyPairConfiguration hubFacingSigningKeyPair;

    @JsonProperty
    @Valid
    @NotNull
    private KeyPairConfiguration hubFacingEncryptionKeyPair;

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

    public URI getHubUrl() {
        return hubUrl;
    }

    public URI getConnectorNodeUrl() {
        return connectorNodeUrl;
    }

    public TranslatorServiceConfiguration getTranslatorServiceConfiguration() { return translatorService; }

    public String getProxyNodeEntityId() {
        return proxyNodeEntityId;
    }

    public KeyPairConfiguration getConnectorFacingSigningKeyPair() {
        return connectorFacingSigningKeyPair;
    }

    public KeyPairConfiguration getHubFacingEncryptionKeyPair() {
        return hubFacingEncryptionKeyPair;
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

    public KeyPairConfiguration getHubFacingSigningKeyPair() {
        return hubFacingSigningKeyPair;
    }

    public URI getProxyNodeResponseUrl() {
        return proxyNodeResponseUrl;
    }

    public URI getProxyNodeAuthnRequestUrl() {
        return proxyNodeAuthnRequestUrl;
    }

    public ReplayCheckerConfiguration getReplayChecker() {
        return replayChecker;
    }

    @Override
    public String getServiceName() {
        return "gateway";
    }
}
