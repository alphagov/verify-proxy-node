package uk.gov.ida.notification.stubconnector;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import uk.gov.ida.configuration.ServiceNameConfiguration;
import uk.gov.ida.notification.pki.KeyPairConfiguration;
import uk.gov.ida.saml.metadata.TrustStoreBackedMetadataConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;

public class StubConnectorConfiguration extends Configuration implements ServiceNameConfiguration {
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
    private String proxyNodeEntityId;

    @JsonProperty
    @Valid
    @NotNull
    private TrustStoreBackedMetadataConfiguration proxyNodeMetadataConfiguration;

    public KeyPairConfiguration getEncryptionKeyPair() {
        return encryptionKeyPair;
    }

    public URI getConnectorNodeBaseUrl() {
        return connectorNodeBaseUrl;
    }

    public String getProxyNodeEntityId() {
        return proxyNodeEntityId;
    }

    public TrustStoreBackedMetadataConfiguration getProxyNodeMetadataConfiguration() {
        return proxyNodeMetadataConfiguration;
    }

    public KeyPairConfiguration getSigningKeyPair() {
        return signingKeyPair;
    }

    @Override
    public String getServiceName() {
        return "stub-connector";
    }
}
