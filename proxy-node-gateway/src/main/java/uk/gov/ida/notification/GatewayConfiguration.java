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

    @Valid
    @NotNull
    @JsonProperty
    private TranslatorServiceConfiguration translatorService;

    @Valid
    @NotNull
    @JsonProperty
    private EidasSamlParserServiceConfiguration eidasSamlParserService;

    @Valid
    @NotNull
    @JsonProperty
    private VerifyServiceProviderConfiguration verifyServiceProviderService;

    @Valid
    @NotNull
    @JsonProperty
    private RedisServiceConfiguration redisService;

    @Valid
    @NotNull
    @JsonProperty
    private URI errorPageRedirectUrl;

    @Valid
    @NotNull
    @JsonProperty
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
