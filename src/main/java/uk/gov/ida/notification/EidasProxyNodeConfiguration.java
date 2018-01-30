package uk.gov.ida.notification;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientConfiguration;
import uk.gov.ida.notification.pki.KeyPairConfiguration;

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
    private String hubEntityId;

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
    private URI hubMetadataUrl;

    @JsonProperty
    @Valid
    @NotNull
    private URI proxyNodeMetadataForConnectorNodeUrl;

    @JsonProperty
    @Valid
    @NotNull
    private String connectorNodeIssuerId;

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

    public URI getHubMetadataUrl() {
        return hubMetadataUrl;
    }

    public String getHubEntityId() {
        return hubEntityId;
    }

    public URI getProxyNodeMetadataForConnectorNodeUrl() {
        return proxyNodeMetadataForConnectorNodeUrl;
    }

    public String getConnectorNodeIssuerId() {
        return connectorNodeIssuerId;
    }
}
