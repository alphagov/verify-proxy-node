package uk.gov.ida.notification.translator.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import uk.gov.ida.notification.configuration.CredentialConfiguration;
import uk.gov.ida.notification.configuration.VerifyServiceProviderConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;

public class TranslatorConfiguration extends Configuration {

    @Valid
    @NotNull
    @JsonProperty
    private URI proxyNodeMetadataForConnectorNodeUrl;

    @Valid
    @NotNull
    @JsonProperty
    private VerifyServiceProviderConfiguration vspConfiguration;

    @Valid
    @NotNull
    @JsonProperty
    private CredentialConfiguration credentialConfiguration;

    @Valid
    @NotNull
    @JsonProperty
    private String connectorNodeIssuerId;

    public URI getProxyNodeMetadataForConnectorNodeUrl() {
        return proxyNodeMetadataForConnectorNodeUrl;
    }

    public VerifyServiceProviderConfiguration getVspConfiguration() {
        return vspConfiguration;
    }

    public String getConnectorNodeIssuerId() {
        return connectorNodeIssuerId;
    }

    public CredentialConfiguration getCredentialConfiguration() {
        return credentialConfiguration;
    }
}
