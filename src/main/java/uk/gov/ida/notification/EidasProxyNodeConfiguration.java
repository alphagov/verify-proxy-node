package uk.gov.ida.notification;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import uk.gov.ida.stubs.StubConnectorNodeConfiguration;
import uk.gov.ida.stubs.StubIdpConfiguration;

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
    private StubIdpConfiguration stubIdpConfiguration;

    @JsonProperty
    @Valid
    @NotNull
    private StubConnectorNodeConfiguration stubConnectorNodeConfiguration;

    public URI getHubUrl() {
        return hubUrl;
    }

    public URI getConnectorNodeUrl() {
        return connectorNodeUrl;
    }

    public StubIdpConfiguration getStubIdpConfiguration() {
        return stubIdpConfiguration;
    }

    public StubConnectorNodeConfiguration getStubConnectorNodeConfiguration() {
        return stubConnectorNodeConfiguration;
    }
}
