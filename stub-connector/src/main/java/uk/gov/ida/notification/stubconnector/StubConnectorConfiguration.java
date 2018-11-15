package uk.gov.ida.notification.stubconnector;

import com.fasterxml.jackson.annotation.JsonProperty;
import engineering.reliability.gds.metrics.config.PrometheusConfiguration;
import io.dropwizard.Configuration;
import uk.gov.ida.notification.pki.KeyPairConfiguration;
import uk.gov.ida.saml.metadata.TrustStoreBackedMetadataConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;

public class StubConnectorConfiguration extends Configuration implements PrometheusConfiguration {
    @JsonProperty
    @Valid
    @NotNull
    private KeyPairConfiguration signingKeyPair;

    @JsonProperty
    @Valid
    @NotNull
    private KeyPairConfiguration encryptionKeyPair;

    @JsonProperty
    @Valid
    @NotNull
    private URI connectorNodeBaseUrl;

    @JsonProperty
    @Valid
    @NotNull
    private TrustStoreBackedMetadataConfiguration proxyNodeMetadataConfiguration;

    @JsonProperty
    @Valid
    @NotNull
    private boolean prometheusEnabled = true;

    public KeyPairConfiguration getEncryptionKeyPair() {
        return encryptionKeyPair;
    }

    public URI getConnectorNodeBaseUrl() {
        return connectorNodeBaseUrl;
    }

    public TrustStoreBackedMetadataConfiguration getProxyNodeMetadataConfiguration() {
        return proxyNodeMetadataConfiguration;
    }

    public KeyPairConfiguration getSigningKeyPair() {
        return signingKeyPair;
    }

    public boolean isPrometheusEnabled() {
        return prometheusEnabled;
    }
}
