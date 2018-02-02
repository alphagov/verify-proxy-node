package uk.gov.ida.notification;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientConfiguration;
import uk.gov.ida.notification.pki.KeyPairConfiguration;
import uk.gov.ida.saml.metadata.TrustStoreBackedMetadataConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;

public class EidasProxyNodeConfiguration extends Configuration {

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
    private URI connectorNodeMetadataUrl;

    @JsonProperty
    @Valid
    @NotNull
    private String connectorNodeEntityId;

    @JsonProperty
    @Valid
    @NotNull
    private String proxyNodeEntityId;

    @JsonProperty
    @Valid
    @NotNull
    private KeyPairConfiguration signingKeyPair;

    @JsonProperty
    @Valid
    @NotNull
    private KeyPairConfiguration hubFacingEncryptionKeyPair;

    @JsonProperty
    @Valid
    @NotNull
    private JerseyClientConfiguration httpClient;

    @JsonProperty
    @Valid
    @NotNull
    private URI proxyNodeMetadataForConnectorNodeUrl;

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

    public URI getHubUrl() {
        return hubUrl;
    }

    public URI getConnectorNodeUrl() {
        return connectorNodeUrl;
    }

    public String getProxyNodeEntityId() {
        return proxyNodeEntityId;
    }

    public KeyPairConfiguration getSigningKeyPair() {
        return signingKeyPair;
    }

    public KeyPairConfiguration getHubFacingEncryptionKeyPair() {
        return hubFacingEncryptionKeyPair;
    }

    public JerseyClientConfiguration getHttpClientConfiguration() {
        return httpClient;
    }

    public URI getConnectorNodeMetadataUrl() {
        return connectorNodeMetadataUrl;
    }

    public String getConnectorNodeEntityId() {
        return connectorNodeEntityId;
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
}
