package uk.gov.ida.eidas.metatron.core.dto;

import com.google.common.collect.ImmutableMap;
import com.nimbusds.jose.jwk.JWK;
import io.dropwizard.client.JerseyClientConfiguration;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.xmlsec.signature.support.impl.ExplicitKeySignatureTrustEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.ida.saml.metadata.DynamicTrustStoreConfiguration;
import uk.gov.ida.saml.metadata.JerseyClientMetadataResolver;
import uk.gov.ida.saml.metadata.MetadataResolverRepository;
import uk.gov.ida.saml.metadata.TrustStoreBackedMetadataConfiguration;
import uk.gov.ida.saml.metadata.factories.DropwizardMetadataResolverFactory;

import javax.ws.rs.client.Client;
import java.net.URI;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MetatronMetadataResolverRepository implements MetadataResolverRepository {

    private final Logger log = LoggerFactory.getLogger(MetatronMetadataResolverRepository.class);
    private final DropwizardMetadataResolverFactory dropwizardMetadataResolverFactory = new DropwizardMetadataResolverFactory();
    private Client client;


    private ImmutableMap<String, MetadataResolver> metadataResolvers = ImmutableMap.of();
    private EidasConfig config;

    public MetatronMetadataResolverRepository (EidasConfig config, Client client) {
        this.config = config;
        this.client = client;
        refresh();
    }

    @Override
    public Optional<MetadataResolver> getMetadataResolver(String entityId) {
        return Optional.ofNullable(metadataResolvers.get(entityId));
    }

    @Override
    public List<String> getResolverEntityIds() {
        return metadataResolvers.keySet().asList();
    }

    @Override
    public Optional<ExplicitKeySignatureTrustEngine> getSignatureTrustEngine(String entityId) {
        return Optional.empty();
    }

    @Override
    public Map<String, MetadataResolver> getMetadataResolvers() {
        return metadataResolvers;
    }

    @Override
    public List<String> getTrustAnchorsEntityIds() {
        return null;
    }

    @Override
    public void refresh() {

        ImmutableMap.Builder<String, MetadataResolver> newMetadataResolvers = new ImmutableMap.Builder<>();
        config.getCountries()
                .stream()
                .forEach(eidasCountryConfig -> {
                    var entityId = eidasCountryConfig.getEntityId();
                    try {
                        MetadataResolver metadataResolver = metadataResolvers.containsKey(entityId) ?
                                metadataResolvers.get(entityId) : createMetadataResolver(entityId);
                        newMetadataResolvers.put(entityId, metadataResolver);
                    } catch (Exception e) {
                        log.error("Error creating MetadataResolver for " + entityId, e);
                    }
                });
        this.metadataResolvers = newMetadataResolvers.build();
    }

    @Override
    public List<X509Certificate> sortCertsByDate(JWK trustAnchor) {
        return null;
    }

    private MetadataResolver createMetadataResolver(String entityId) {
        TrustStoreBackedMetadataConfiguration mdConfig = new TrustStoreBackedMetadataConfiguration(
                URI.create(entityId),
                60000L,
                600000L,
                null,
                new JerseyClientConfiguration(),
                "myLittleJerseyClient",
                null,
                new DynamicTrustStoreConfiguration(config.getKeyStore())
        );

        MetadataResolver metadataResolverWithClient = dropwizardMetadataResolverFactory.createMetadataResolverWithClient(mdConfig, true, client);
        JerseyClientMetadataResolver metadataResolver = (JerseyClientMetadataResolver) metadataResolverWithClient;
        return metadataResolver;
    }
}
