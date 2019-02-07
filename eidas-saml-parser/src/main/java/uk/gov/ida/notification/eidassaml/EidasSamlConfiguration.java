package uk.gov.ida.notification.eidassaml;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import uk.gov.ida.notification.configuration.ReplayCheckerConfiguration;
import uk.gov.ida.saml.metadata.TrustStoreBackedMetadataConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;

public class EidasSamlConfiguration extends Configuration {

    @JsonProperty
    @Valid
    @NotNull
    private URI proxyNodeAuthnRequestUrl;

    @JsonProperty
    @Valid
    private ReplayCheckerConfiguration replayChecker = new ReplayCheckerConfiguration();

    @JsonProperty
    @Valid
    @NotNull
    private TrustStoreBackedMetadataConfiguration connectorMetadataConfiguration;

    public ReplayCheckerConfiguration getReplayChecker() {
        return replayChecker;
    }

    public TrustStoreBackedMetadataConfiguration getConnectorMetadataConfiguration() {
        return connectorMetadataConfiguration;
    }

    public URI getProxyNodeAuthnRequestUrl() {
        return proxyNodeAuthnRequestUrl;
    }
}
