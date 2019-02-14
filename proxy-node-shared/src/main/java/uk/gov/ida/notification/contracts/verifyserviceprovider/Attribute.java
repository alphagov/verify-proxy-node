package uk.gov.ida.notification.contracts.verifyserviceprovider;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class Attribute<T> {

    @NotNull
    @JsonProperty
    private T value;

    @NotNull
    @JsonProperty
    private boolean verified;

    @JsonProperty
    private LocalDateTime from;

    @JsonProperty
    private LocalDateTime to;

    @SuppressWarnings("Needed for JSON serialisation")
    public Attribute() {
    }

    public Attribute(
            T value,
            boolean verified,
            LocalDateTime from,
            LocalDateTime to) {
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

    public LocalDateTime getFrom() {
        return from;
    }

    public LocalDateTime getTo() {
        return to;
    }
}
