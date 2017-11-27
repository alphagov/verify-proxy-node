package uk.gov.ida.notification;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

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
    private String proxyNodeEntityId;

    public URI getHubUrl() {
        return hubUrl;
    }

    public URI getConnectorNodeUrl() {
        return connectorNodeUrl;
    }

    public String getProxyNodeEntityId() {
        return proxyNodeEntityId;
    }
}
