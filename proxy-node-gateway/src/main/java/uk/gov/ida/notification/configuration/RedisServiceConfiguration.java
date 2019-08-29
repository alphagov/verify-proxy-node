package uk.gov.ida.notification.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

import javax.validation.Valid;
import java.net.URI;
import java.time.Duration;

import static java.time.temporal.ChronoUnit.MINUTES;

public class RedisServiceConfiguration extends Configuration {

    @JsonProperty
    @Valid
    private URI urlWrite = null;

    @JsonProperty
    @Valid
    private URI urlRead = null;

    @Valid
    @JsonProperty
    private Duration recordTTL = Duration.of(150, MINUTES);

    @Valid
    @JsonProperty
    private Boolean local = false;


    public URI getUrlWrite() {
        return urlWrite;
    }

    public URI getUrlRead() {
        return urlRead;
    }

    public Long getRecordTTL() {
        return recordTTL.getSeconds();
    }

    public Boolean isLocal() {
        return local;
    }
}
