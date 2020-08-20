package uk.gov.ida.notification.eidassaml;

import com.fasterxml.jackson.annotation.JsonProperty;
import engineering.reliability.gds.metrics.config.PrometheusConfiguration;
import io.dropwizard.Configuration;
import uk.gov.ida.notification.configuration.ReplayCheckerConfiguration;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;

public class EidasSamlParserConfiguration extends Configuration implements PrometheusConfiguration {

    @Valid
    @NotNull
    @JsonProperty
    private URI proxyNodeAuthnRequestUrl;

    @Valid
    @NotNull
    @JsonProperty
    private ReplayCheckerConfiguration replayChecker;

    @Valid
    @NotNull
    @JsonProperty
    private URI metatronUri;

    public URI getMetatronUri() {
        return metatronUri;
    }

    public ReplayCheckerConfiguration getReplayChecker() {
        return replayChecker;
    }

    public URI getProxyNodeAuthnRequestUrl() {
        return proxyNodeAuthnRequestUrl;
    }

    @Override
    public boolean isPrometheusEnabled() {
        return true;
    }
}
