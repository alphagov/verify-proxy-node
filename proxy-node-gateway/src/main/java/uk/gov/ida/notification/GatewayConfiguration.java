package uk.gov.ida.notification;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import uk.gov.ida.notification.configuration.EidasSamlParserServiceConfiguration;
import uk.gov.ida.notification.configuration.RedisServiceConfiguration;
import uk.gov.ida.notification.configuration.TranslatorServiceConfiguration;
import uk.gov.ida.notification.configuration.VerifyServiceProviderConfiguration;
import uk.gov.ida.notification.shared.metadata.MetadataPublishingConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;

public class GatewayConfiguration extends Configuration {

    @JsonProperty
    @Valid
    @NotNull
    private TranslatorServiceConfiguration translatorService;

    @JsonProperty
    @Valid
    @NotNull
    private EidasSamlParserServiceConfiguration eidasSamlParserService;

    @JsonProperty
    @Valid
    @NotNull
    private VerifyServiceProviderConfiguration verifyServiceProviderService;

    @JsonProperty
    @Valid
    @NotNull
    private RedisServiceConfiguration redisService;

    @JsonProperty
    @Valid
    @NotNull
    private URI errorPageRedirectUrl;

    @JsonProperty
    @Valid
    @NotNull
    private MetadataPublishingConfiguration metadataPublishingConfiguration;


    public TranslatorServiceConfiguration getTranslatorServiceConfiguration() { return translatorService; }

    public EidasSamlParserServiceConfiguration getEidasSamlParserServiceConfiguration() { return eidasSamlParserService; }

    public VerifyServiceProviderConfiguration getVerifyServiceProviderConfiguration() { return verifyServiceProviderService; }

    public RedisServiceConfiguration getRedisService() {
        return redisService;
    }

    public URI getErrorPageRedirectUrl() {
        return errorPageRedirectUrl;
    }

    public MetadataPublishingConfiguration getMetadataPublishingConfiguration() {
        return metadataPublishingConfiguration;
    }
}
