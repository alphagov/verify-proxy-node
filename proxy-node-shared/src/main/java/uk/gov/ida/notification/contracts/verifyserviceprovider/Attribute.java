package uk.gov.ida.notification.contracts.verifyserviceprovider;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;

import org.joda.time.DateTime;

public class Attribute<T> {

    @NotNull
    @JsonProperty
    private T value;

    @NotNull
    @JsonProperty
    private boolean verified;

    @JsonProperty
    private DateTime from;

    @JsonProperty
    private DateTime to;

    @SuppressWarnings("Needed for JSON serialisation")
    public Attribute() {
    }

    public Attribute(
            T value,
            boolean verified,
            DateTime from,
            DateTime to) {
        this.value = value;
        this.verified = verified;
        this.from = from;
        this.to = to;
    }

    public T getValue() {
        return value;
    }

    public boolean isVerified() {
        return verified;
    }

    public DateTime getFrom() {
        return from;
    }

    public DateTime getTo() {
        return to;
    }
}