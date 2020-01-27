package uk.gov.ida.eidas.metatron.domain;

import helpers.ResourceHelpers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import uk.gov.ida.saml.metadata.KeyStoreLoader;
import uk.gov.ida.saml.metadata.factories.MetadataResolverFactory;

import javax.ws.rs.client.Client;
import java.net.URI;
import java.security.KeyStore;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.ida.eidas.metatron.domain.MetadataResolverService.MAX_REFRESH_DELAY_MS;
import static uk.gov.ida.eidas.metatron.domain.MetadataResolverService.MIN_REFRESH_DELAY_MS;

@RunWith(MockitoJUnitRunner.class)
public class MetadataResolverServiceTest {

    public static final String AN_ENTITY_ID = "http://localhost";

    @Mock
    private EidasConfig countriesConfig;

    @Mock
    private EidasCountryConfig countryConfig1;

    @Mock
    private EidasCountryConfig countryConfig2;

    @Mock
    private MetadataResolverFactory metadataResolverFactory;

    @Mock
    private MetadataResolver metadataResolver1;

    @Mock
    private MetadataResolver metadataResolver2;

    @Test
    public void shouldProvideCorrectMetadataResolverForEntityId() {

        KeyStore keyStore = new KeyStoreLoader().load(ResourceHelpers.resourceFilePath("test-truststore.ts"), "puppet");
        when(countriesConfig.getCountries()).thenReturn(List.of(countryConfig1, countryConfig2));
        setCountryConfig(countryConfig1, keyStore, AN_ENTITY_ID, metadataResolver1);

        setCountryConfig(countryConfig2, keyStore, "http://localhost/another-entity", metadataResolver2);
        when(countryConfig2.getTlsTruststore()).thenReturn(Optional.of(keyStore));

        MetadataResolverService service = new MetadataResolverService(countriesConfig, metadataResolverFactory);

        MetadataResolver result = service.getMetadataResolver(AN_ENTITY_ID);
        assertThat(result).isEqualTo(metadataResolver1);
    }

    private void setCountryConfig(EidasCountryConfig config, KeyStore keyStore, String entitId, MetadataResolver metadataResolver) {
        when(config.getEntityId()).thenReturn(entitId);
        when(config.getConnectorMetadata()).thenReturn(URI.create(entitId));
        when(config.getMetadataTruststore()).thenReturn(keyStore);
        when(metadataResolverFactory.create(any(Client.class), eq(URI.create(entitId)), any(List.class), eq(MIN_REFRESH_DELAY_MS), eq(MAX_REFRESH_DELAY_MS))).thenReturn(metadataResolver);
    }
}