package uk.gov.ida.notification.shared.metadata;

import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.model.Resource;

import java.net.URI;

public class MetadataPublishingBundle<T extends Configuration> implements ConfiguredBundle<T> {

    private final MetadataPublishingConfigurationExtractor<T> configExtractor;

    public MetadataPublishingBundle(MetadataPublishingConfigurationExtractor<T> configExtractor) {
        this.configExtractor = configExtractor;
    }

    @Override
    public void run(T configuration, Environment environment) {
        final MetadataPublishingConfiguration mpConfiguration =
                configExtractor.getMetadataPublishingConfiguration(configuration);

        final Resource metadataPublishResource = Resource.builder(MetadataPublishingResource.class)
                .path(mpConfiguration.getMetadataPublishPath().toString())
                .build();

        environment.jersey().register(getJerseyMetadataResourcePathBinder(mpConfiguration));
        environment.jersey().getResourceConfig().registerResources(metadataPublishResource);
    }

    @Override
    public void initialize(Bootstrap<?> bootstrap) {
    }

    public interface MetadataPublishingConfigurationExtractor<T> {
        MetadataPublishingConfiguration getMetadataPublishingConfiguration(T configuration);
    }

    private AbstractBinder getJerseyMetadataResourcePathBinder(MetadataPublishingConfiguration configuration) {
        return new AbstractBinder() {
            @Override
            protected void configure() {
                bind(configuration.getMetadataResourceFilePath()).to(URI.class).named("metadataResourceFilePath");
            }
        };
    }
}
