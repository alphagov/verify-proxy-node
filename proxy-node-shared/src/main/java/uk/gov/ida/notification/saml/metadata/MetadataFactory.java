package uk.gov.ida.notification.saml.metadata;

import io.dropwizard.Configuration;
import uk.gov.ida.saml.metadata.MetadataHealthCheck;
import uk.gov.ida.saml.metadata.MetadataResolverConfiguration;
import uk.gov.ida.saml.metadata.bundle.MetadataResolverBundle;

public class MetadataFactory<T extends Configuration> {
    private final Metadata metadata;
    private final MetadataResolverBundle<T> bundle;
    private final MetadataResolverBundle.MetadataConfigurationExtractor<T> extractor;

    public MetadataFactory(MetadataResolverBundle.MetadataConfigurationExtractor<T> extractor) {
        this.extractor = extractor;
        this.bundle = new MetadataResolverBundle<>(extractor);
        this.metadata = new Metadata(bundle.getMetadataCredentialResolver());
    }

    public MetadataHealthCheck buildHealthCheck(T configuration) {
        MetadataResolverConfiguration metadataConfiguration = extractor.getMetadataConfiguration(configuration);
        return new MetadataHealthCheck(
                bundle.getMetadataResolver(),
                metadataConfiguration.getUri().toString(),
                metadataConfiguration.getExpectedEntityId()
        );
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public MetadataResolverBundle<T> getBundle() {
        return bundle;
    }
}
