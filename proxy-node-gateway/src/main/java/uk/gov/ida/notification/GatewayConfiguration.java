package uk.gov.ida.notification;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import uk.gov.ida.notification.configuration.EidasSamlParserServiceConfiguration;
import uk.gov.ida.notification.configuration.TranslatorServiceConfiguration;
import uk.gov.ida.notification.configuration.VerifyServiceProviderConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

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

    public TranslatorServiceConfiguration getTranslatorServiceConfiguration() { return translatorService; }

    public EidasSamlParserServiceConfiguration getEidasSamlParserServiceConfiguration() { return eidasSamlParserService; }

    public VerifyServiceProviderConfiguration getVerifyServiceProviderConfiguration() { return verifyServiceProviderService; }
}
