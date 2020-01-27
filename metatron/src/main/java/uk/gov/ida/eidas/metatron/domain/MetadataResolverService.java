package uk.gov.ida.eidas.metatron.domain;

import org.glassfish.jersey.client.JerseyClientBuilder;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.metadata.resolver.filter.MetadataFilter;
import uk.gov.ida.saml.metadata.ExpiredCertificateMetadataFilter;
import uk.gov.ida.saml.metadata.PKIXSignatureValidationFilterProvider;
import uk.gov.ida.saml.metadata.factories.MetadataResolverFactory;

import javax.ws.rs.client.Client;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MetadataResolverService {

    protected static long MIN_REFRESH_DELAY_MS = 60_000;
    protected static long MAX_REFRESH_DELAY_MS = 600_000;

    private Map<String, MetadataResolver> metadataResolverMap;
    private ExpiredCertificateMetadataFilter expiredCertificateMetadataFilter;
    private MetadataResolverFactory metadataResolverFactory;

    public MetadataResolverService(EidasConfig countriesConfig, MetadataResolverFactory metadataResolverFactory) {
        this.metadataResolverFactory = metadataResolverFactory;
        this.metadataResolverMap = new HashMap<>();
        this.expiredCertificateMetadataFilter = new ExpiredCertificateMetadataFilter();

        metadataResolverMap = countriesConfig.getCountries().stream().collect(
                Collectors.toMap(EidasCountryConfig::getEntityId, this::createMetadataResolver));
    }

    public MetadataResolver getMetadataResolver(String entityId) {
        return metadataResolverMap.get(entityId);
    }

    private Client getClient(EidasCountryConfig country) {
        return country.getTlsTruststore().isPresent()
                ? JerseyClientBuilder.newBuilder().trustStore(country.getTlsTruststore().get()).build()
                : JerseyClientBuilder.newClient();
    }

    private List<MetadataFilter> getFilters(EidasCountryConfig country) {
        List<MetadataFilter> filters = new ArrayList<>();
        filters.add(new PKIXSignatureValidationFilterProvider(country.getMetadataTruststore()).get());
        filters.add(expiredCertificateMetadataFilter);
        return filters;
    }

    private MetadataResolver createMetadataResolver(EidasCountryConfig country) {
        MetadataResolver resolver = this.metadataResolverFactory.create(
                getClient(country),
                country.getConnectorMetadata(),
                getFilters(country),
                MIN_REFRESH_DELAY_MS,
                MAX_REFRESH_DELAY_MS);
        return resolver;
    }
}

