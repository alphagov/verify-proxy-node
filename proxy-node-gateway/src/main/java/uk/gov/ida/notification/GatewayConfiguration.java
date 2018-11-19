package uk.gov.ida.notification;

import com.fasterxml.jackson.annotation.JsonProperty;
import engineering.reliability.gds.metrics.config.PrometheusConfiguration;
import io.dropwizard.Configuration;
import uk.gov.ida.notification.pki.KeyPairConfiguration;
import uk.gov.ida.saml.metadata.TrustStoreBackedMetadataConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;

public class GatewayConfiguration extends Configuration implements PrometheusConfiguration {

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
    private URI translatorUrl;

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
    @NotNull
    private boolean prometheusEnabled = true;

    public URI getHubUrl() {
        return hubUrl;
    }

    public URI getConnectorNodeUrl() {
        return connectorNodeUrl;
    }

    public URI getTranslatorUrl() { return translatorUrl; }

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

    public boolean isPrometheusEnabled() {
        return prometheusEnabled;
    }
}
