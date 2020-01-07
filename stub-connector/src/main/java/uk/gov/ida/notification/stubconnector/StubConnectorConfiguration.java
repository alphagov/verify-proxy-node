package uk.gov.ida.notification.stubconnector;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import uk.gov.ida.saml.metadata.TrustStoreBackedMetadataConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.Map;

public class StubConnectorConfiguration extends Configuration {

    @Valid
    @NotNull
    @JsonProperty
    private URI connectorNodeBaseUrl;

    @Valid
    @NotNull
    @JsonProperty
    private URI connectorNodeEntityId;

    /**
     * Period of validity of connector node metadata in months,
     */
    @JsonProperty
    private Integer connectorNodeMetadataExpiryMonths = 1;

    @Valid
    @NotNull
    @JsonProperty
    private ConnectorNodeCredentialConfiguration credentialConfiguration;

    @Valid
    @NotNull
    @JsonProperty
    private TrustStoreBackedMetadataConfiguration proxyNodeMetadataConfiguration;

    @NotNull
    @JsonProperty
    private Map<String, String> connectorNodeTemplateConfig;

    public URI getConnectorNodeBaseUrl() {
        return connectorNodeBaseUrl;
    }

    public URI getConnectorNodeEntityId() {
        return connectorNodeEntityId;
    }

    public String getProxyNodeEntityId() {
        return proxyNodeMetadataConfiguration.getExpectedEntityId();
    }

    public TrustStoreBackedMetadataConfiguration getProxyNodeMetadataConfiguration() {
        return proxyNodeMetadataConfiguration;
    }

    public ConnectorNodeCredentialConfiguration getCredentialConfiguration() {
        return credentialConfiguration;
    }

    public Map<String, String> getConnectorNodeTemplateConfig() {
        return connectorNodeTemplateConfig;
    }

    public Integer getConnectorNodeMetadataExpiryMonths() {
        return connectorNodeMetadataExpiryMonths;
    }
}
