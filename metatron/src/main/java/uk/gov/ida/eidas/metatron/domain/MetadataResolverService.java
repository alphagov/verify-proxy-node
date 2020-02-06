package uk.gov.ida.eidas.metatron.domain;

import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import net.shibboleth.utilities.java.support.resolver.ResolverException;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.criterion.EntityRoleCriterion;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.metadata.resolver.filter.MetadataFilter;
import org.opensaml.saml.saml2.metadata.AssertionConsumerService;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.saml.security.impl.MetadataCredentialResolver;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.credential.UsageType;
import org.opensaml.security.criteria.UsageCriterion;
import org.opensaml.security.x509.X509Credential;
import uk.gov.ida.eidas.metatron.exceptions.MetatronClientException;
import uk.gov.ida.eidas.metatron.exceptions.MetatronServerException;
import uk.gov.ida.notification.contracts.CountryMetadataResponse;
import uk.gov.ida.saml.metadata.ExpiredCertificateMetadataFilter;
import uk.gov.ida.saml.metadata.PKIXSignatureValidationFilterProvider;
import uk.gov.ida.saml.metadata.factories.CredentialResolverFactory;
import uk.gov.ida.saml.metadata.factories.MetadataResolverFactory;

import javax.ws.rs.client.Client;
import java.net.URI;
import java.security.cert.CertificateEncodingException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MetadataResolverService {

    protected static long MIN_REFRESH_DELAY_MS = 60_000;
    protected static long MAX_REFRESH_DELAY_MS = 600_000;

    private final Map<URI, CountryMetadata> countryConfigMap;
    private final ExpiredCertificateMetadataFilter expiredCertificateMetadataFilter;
    private final MetadataResolverFactory metadataResolverFactory;
    private final CredentialResolverFactory credentialResolverFactory;

    public MetadataResolverService(
            EidasConfig countriesConfig,
            MetadataResolverFactory metadataResolverFactory,
            CredentialResolverFactory credentialResolverFactory) {
        this.metadataResolverFactory = metadataResolverFactory;
        this.credentialResolverFactory = credentialResolverFactory;
        this.expiredCertificateMetadataFilter = new ExpiredCertificateMetadataFilter();
        this.countryConfigMap = countriesConfig.getCountries().stream()
                .collect(Collectors.toMap(EidasCountryConfig::getEntityId, this::createMetadataResolver));
    }

    public CountryMetadataResponse getCountryMetadataResponse(URI entityId) {
        CountryMetadata countryMetadata = getEnabledCountryConfigurationData(entityId);
        URI location = getAssertionConsumerServiceLocation(countryMetadata);
        String signingX509 = getCertificateAsX509(countryMetadata, UsageType.SIGNING);
        String encryptionX509 = getCertificateAsX509(countryMetadata, UsageType.ENCRYPTION);
        return new CountryMetadataResponse(
                signingX509,
                encryptionX509,
                location,
                countryMetadata.getCountryConfig().getEntityId().toString(),
                countryMetadata.getCountryConfig().getCountryCode());
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

    private CountryMetadata createMetadataResolver(EidasCountryConfig country) {
        MetadataResolver resolver = this.metadataResolverFactory.create(
                getClient(country),
                country.getConnectorMetadata(),
                getFilters(country),
                MIN_REFRESH_DELAY_MS,
                MAX_REFRESH_DELAY_MS);
        return new CountryMetadata(country, resolver);
    }

    private CountryMetadata getEnabledCountryConfigurationData(URI entityId) {
        CountryMetadata countryMetadata = countryConfigMap.get(entityId);
        if (countryMetadata == null) {
            throw new MetatronClientException(String.format("No metadata configured for entityId %s", entityId));
        }
        if (!countryMetadata.getCountryConfig().isEnabled()) {
            throw new MetatronClientException(String.format("entityId %s is not enabled", entityId));
        }
        return countryMetadata;
    }

    private String getCertificateAsX509(CountryMetadata countryMetadata, UsageType usageType) {
        X509Credential credential = (X509Credential) getCredential(countryMetadata, usageType);
        try {
            return Base64.getEncoder().encodeToString(credential.getEntityCertificate().getEncoded());
        } catch (CertificateEncodingException e) {
            throw new MetatronServerException(String.format("Error encoding the %s certificate for entityId %s", usageType, countryMetadata.getCountryConfig().getEntityId()), e);
        }
    }

    private Credential getCredential(CountryMetadata countryMetadata, UsageType usageType) {
        CriteriaSet criteria = new CriteriaSet();
        URI entityId = countryMetadata.getCountryConfig().getEntityId();
        criteria.add(new EntityIdCriterion(entityId.toString()));
        criteria.add(new EntityRoleCriterion(SPSSODescriptor.DEFAULT_ELEMENT_NAME));
        criteria.add(new UsageCriterion(usageType));
        try {
            MetadataResolver metadataResolver = countryMetadata.getMetadataResolver();
            MetadataCredentialResolver metadataCredentialResolver = credentialResolverFactory.create(metadataResolver);
            Credential credential = metadataCredentialResolver.resolveSingle(criteria);
            if (credential == null) {
                throw new MetatronServerException(String.format("Missing %s certificate for entityId %s", usageType, entityId));
            }
            return credential;
        } catch (ResolverException | ComponentInitializationException e) {
            throw new MetatronServerException(String.format("Unable to resolve metadata credentials from for entityId %s", entityId), e);
        }
    }

    private URI getAssertionConsumerServiceLocation(CountryMetadata countryMetadata) {
        MetadataResolver metadataResolver = countryMetadata.getMetadataResolver();
        String entityId = countryMetadata.getCountryConfig().getEntityId().toString();
        CriteriaSet criteria = new CriteriaSet(new EntityIdCriterion(entityId));
        EntityDescriptor entityDescriptor;
        try {
            entityDescriptor = metadataResolver.resolveSingle(criteria);
        } catch (ResolverException e) {
            throw new MetatronServerException(String.format("Unable to resolve entityDescriptor with given criteria for entityId %s", entityId), e);
        }
        if (entityDescriptor == null) {
            throw new MetatronServerException(String.format("No entityDescriptor for entityId %s"));
        }
        SPSSODescriptor spssoDescriptor = entityDescriptor.getSPSSODescriptor(SAMLConstants.SAML20P_NS);
        return spssoDescriptor.getAssertionConsumerServices().stream()
                .map(AssertionConsumerService::getLocation)
                .map(URI::create)
                .findFirst()
                .orElseThrow(() -> new MetatronServerException(String.format("Missing Assertion Consumer Service Location for entityId %s", entityId)));

    }

    private class CountryMetadata {
        private final EidasCountryConfig countryConfig;
        private final MetadataResolver metadataResolver;

        public CountryMetadata(EidasCountryConfig countryConfig, MetadataResolver metadataResolver) {
            this.countryConfig = countryConfig;
            this.metadataResolver = metadataResolver;
        }

        public EidasCountryConfig getCountryConfig() {
            return countryConfig;
        }

        public MetadataResolver getMetadataResolver() {
            return metadataResolver;
        }
    }

}
