package uk.gov.ida.notification.saml.metadata;

import org.opensaml.saml.metadata.resolver.impl.AbstractReloadingMetadataResolver;

import javax.ws.rs.client.Client;
import java.net.URI;
import java.util.Timer;

public class JerseyClientMetadataResolver extends AbstractReloadingMetadataResolver {
    private final Client client;
    private final URI metadataUri;

    public JerseyClientMetadataResolver(Timer timer, Client client, URI metadataUri) {
        super(timer);
        this.client = client;
        this.metadataUri = metadataUri;
    }

    @Override
    protected String getMetadataIdentifier() {
        return "metadata";
    }

    @Override
    protected byte[] fetchMetadata() {
        return client.target(metadataUri).request().get(String.class).getBytes();
    }
}
