package uk.gov.ida.eidas.metatron.health;

import io.prometheus.client.Gauge;
import uk.gov.ida.eidas.metatron.domain.MetadataResolverService;

public class CountryMetadataHealthMetrics implements Runnable {

    private static final io.prometheus.client.Gauge METADATA_FETCH_GAUGE = Gauge
            .build("country_metadata_health_metrics",
                    "Country Metadata Health Metrics")
            .labelNames("entity")
            .register();

    private final MetadataResolverService resolverService;

    public CountryMetadataHealthMetrics(MetadataResolverService resolverService) {
        this.resolverService = resolverService;
    }

    @Override
    public void run() {
        resolverService.getResolvers().stream().forEach(entityId -> {
            try {
                resolverService.getCountryMetadataResponse(entityId);
                METADATA_FETCH_GAUGE.labels(entityId.toString()).set(1);
            } catch (Exception e) {
                METADATA_FETCH_GAUGE.labels(entityId.toString()).set(0);
            }
        });
    }
}
