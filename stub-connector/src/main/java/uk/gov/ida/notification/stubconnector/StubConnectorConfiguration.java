package uk.gov.ida.notification.stubconnector;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import uk.gov.ida.notification.configuration.CredentialConfiguration;
import uk.gov.ida.notification.shared.metadata.MetadataPublishingConfiguration;
import uk.gov.ida.saml.metadata.TrustStoreBackedMetadataConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;

public class StubConnectorConfiguration extends Configuration {

    @Valid
    @NotNull
    @JsonProperty
    private URI connectorNodeBaseUrl;

    @Valid
    @NotNull
    @JsonProperty
    private CredentialConfiguration credentialConfiguration;

    @Valid
    @NotNull
    @JsonProperty
    private MetadataPublishingConfiguration metadataPublishingConfiguration;

    @Valid
    @NotNull
    @JsonProperty
    private TrustStoreBackedMetadataConfiguration proxyNodeMetadataConfiguration;

    public URI getConnectorNodeBaseUrl() {
        return connectorNodeBaseUrl;
    }

    public String getProxyNodeEntityId() {
        return proxyNodeMetadataConfiguration.getExpectedEntityId();
    }

    public TrustStoreBackedMetadataConfiguration getProxyNodeMetadataConfiguration() {
        return proxyNodeMetadataConfiguration;
    }

    public MetadataPublishingConfiguration getMetadataPublishingConfiguration() {
        return metadataPublishingConfiguration;
    }

    public CredentialConfiguration getCredentialConfiguration() {
        return credentialConfiguration;
    }
}
