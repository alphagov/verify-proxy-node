package uk.gov.ida.notification.saml.metadata;

import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.xml.BasicParserPool;

import javax.ws.rs.client.Client;
import java.net.URI;
import java.util.Timer;

public class JerseyClientMetadataResolverInitializer {

    private final String resolverName;
    private BasicParserPool parserPool;
    private JerseyClientMetadataResolver metadataResolver;


    public JerseyClientMetadataResolverInitializer(String resolverName, Client client, URI metadataUrl) {
        this.resolverName = resolverName;
        parserPool = new BasicParserPool();
        metadataResolver = new JerseyClientMetadataResolver(new Timer(), client, metadataUrl);
    }

    public JerseyClientMetadataResolver initialize() throws ComponentInitializationException {
        parserPool.initialize();
        metadataResolver.setParserPool(parserPool);
        metadataResolver.setRequireValidMetadata(true);
        metadataResolver.setId(resolverName);
        metadataResolver.setFailFastInitialization(false);
        metadataResolver.initialize();
        return metadataResolver;
    }
}
