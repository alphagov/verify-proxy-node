package uk.gov.ida.notification.stubconnector;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.smoketurner.dropwizard.zipkin.ZipkinFactory;
import io.dropwizard.Configuration;
import uk.gov.ida.notification.configuration.CredentialConfiguration;
import uk.gov.ida.saml.metadata.TrustStoreBackedMetadataConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;

public class StubConnectorConfiguration extends Configuration {
    @JsonProperty
    @Valid
    @NotNull
    private URI connectorNodeBaseUrl;

    @Valid
    @NotNull
    @JsonProperty
    private CredentialConfiguration credentialConfiguration;

    @JsonProperty
    @Valid
    @NotNull
    private TrustStoreBackedMetadataConfiguration proxyNodeMetadataConfiguration;

    @JsonProperty
    @Valid
    private ZipkinFactory zipkin;

    public URI getConnectorNodeBaseUrl() {
        return connectorNodeBaseUrl;
    }

    public String getProxyNodeEntityId() {
        return proxyNodeMetadataConfiguration.getExpectedEntityId();
    }

    public TrustStoreBackedMetadataConfiguration getProxyNodeMetadataConfiguration() {
        return proxyNodeMetadataConfiguration;
    }

    public CredentialConfiguration getCredentialConfiguration() {
        return credentialConfiguration;
    }

    public ZipkinFactory getZipkin() {
        return zipkin;
    }
}
