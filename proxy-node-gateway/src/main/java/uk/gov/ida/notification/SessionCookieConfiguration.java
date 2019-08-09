package uk.gov.ida.notification;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import uk.gov.ida.notification.session.JWKSetConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class SessionCookieConfiguration extends Configuration {

    @Valid
    @NotNull
    @JsonProperty
    private JWKSetConfiguration jwkSetConfiguration;

    @Valid
    @JsonProperty
    private int expiryMinutes;

    @Valid
    @NotNull
    @JsonProperty
    private String domain;

    public JWKSetConfiguration getJwkSetConfiguration() {
        return jwkSetConfiguration;
    }

    public int getExpiryMinutes() {
        return expiryMinutes;
    }

    public String getDomain() {
        return domain;
    }
}
