package uk.gov.ida.stubs;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;

public class StubIdpConfiguration extends Configuration {

    @JsonProperty
    @Valid
    @NotNull
    private URI proxyNodeIdpResponseUri;

    public URI getProxyNodeIdpResponseUri() {
        return proxyNodeIdpResponseUri;
    }
}
