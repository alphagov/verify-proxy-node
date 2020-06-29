package uk.gov.ida.notification.stubconnector.views;

import io.dropwizard.views.View;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

public class StartPageView extends View {
    private final URI proxyNodeMetadataUrl;
    private final URI proxyNodeMetadataSigningCertsUrl;

    private URI connectorMetadataFromMetatron;

    public StartPageView(URI proxyNodeMetadataUrl, URI stubConnectorEntityId, URI metatronUrl) {
        super("startpage.mustache");

        this.proxyNodeMetadataUrl = URI.create(
                proxyNodeMetadataUrl.toString().replace("host.docker.internal", "localhost"));

        this.proxyNodeMetadataSigningCertsUrl = URI.create(
                this.proxyNodeMetadataUrl.toString().replaceAll("/$", "") + "SigningCertificates");

        if (metatronUrl != null) {
            this.connectorMetadataFromMetatron =
                    UriBuilder.fromUri(metatronUrl)
                            .path("metadata")
                            .path("{entityID}")
                            .build(stubConnectorEntityId);
        }
    }

    public URI getProxyNodeMetadataUrl() {
        return proxyNodeMetadataUrl;
    }

    public URI getProxyNodeMetadataSigningCertsUrl() {
        return proxyNodeMetadataSigningCertsUrl;
    }

    public URI getConnectorMetadataFromMetatron() {
        return connectorMetadataFromMetatron;
    }

    public boolean isMetatronUrlAvailable() {
        return this.connectorMetadataFromMetatron != null;
    }
}
