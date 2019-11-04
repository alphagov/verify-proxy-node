package uk.gov.ida.notification.contracts.verifyserviceprovider;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.joda.time.DateTime;

import javax.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
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

    public boolean isVerifiedAndCurrent() {
        return this.verified && this.isCurrent() && this.value != null;
    }

    public boolean isCurrent() {
        return (this.from == null || this.from.isBeforeNow()) &&
                (this.to == null || this.to.isAfterNow());
    }

    @Override
    @JsonIgnore
    public String toString() {
        final StringBuilder sb = new StringBuilder("Attribute{");
        sb.append("verified=").append(verified);
        sb.append(", has from=").append(from != null);
        sb.append(", has to=").append(to != null);
        sb.append('}');
        return sb.toString();
    }
}
