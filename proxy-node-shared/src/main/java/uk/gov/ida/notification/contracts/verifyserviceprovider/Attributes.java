package uk.gov.ida.notification.contracts.verifyserviceprovider;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.format.ISODateTimeFormat;
import uk.gov.ida.notification.shared.logging.ProxyNodeLogger;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Attributes {

    @NotNull
    @JsonProperty
    private List<Attribute<String>> firstNames;

    @JsonProperty
    private List<Attribute<String>> middleNames;

    @NotNull
    @JsonProperty
    private List<Attribute<String>> surnames;

    @NotNull
    @JsonProperty
    private List<Attribute<DateTime>> datesOfBirth;

    @JsonProperty
    private Attribute<String> gender;

    @JsonProperty
    private List<Attribute<Address>> addresses;

    @SuppressWarnings("Needed for JSON serialisation")
    public Attributes() {
    }

    public Attributes(
            List<Attribute<String>> firstNames,
            List<Attribute<String>> middleNames,
            List<Attribute<String>> surnames,
            List<Attribute<DateTime>> datesOfBirth,
            Attribute<String> gender,
            List<Attribute<Address>> addresses) {
        this.firstNames = firstNames;
        this.middleNames = middleNames;
        this.surnames = surnames;
        this.datesOfBirth = datesOfBirth;
        this.gender = gender;
        this.addresses = addresses;
    }

    @JsonIgnore
    public AttributesList<String> getFirstNames() {
        return new AttributesList(firstNames, "firstName");
    }

    @JsonIgnore
    public AttributesList<String> getMiddleNames() {
        return new AttributesList(middleNames, "middleName");
    }

    @JsonIgnore
    public AttributesList<String> getSurnames() {
        return new AttributesList(surnames, "surname");
    }

    @JsonIgnore
    public AttributesList<DateTime> getDatesOfBirth() {
        return new AttributesList(datesOfBirth, "dateOfBirth");
    }

    @JsonIgnore
    public Attribute<String> getGender() {
        return gender;
    }

    @JsonIgnore
    public AttributesList<Address> getAddresses() {
        return new AttributesList(addresses, "address");
    }

    // Prints date in EIDAS format YYYY-MM-dd
    public static String getDateInEidasFormat(DateTime date) {
        return ISODateTimeFormat.date().withChronology(ISOChronology.getInstanceUTC()).print(date);
    }

    public class AttributesList<T> {

        private final List<Attribute<T>> attributes;
        private final String type;

        AttributesList(List<Attribute<T>> attributes, String type) {
            this.attributes = Optional.ofNullable(attributes).orElse(Collections.emptyList());
            this.type = type;
        }

        public String getType() {
            return type;
        }

        public List<Attribute<T>> getValidAttributes() {

            var valid = getAllAttributes().stream()
                    .filter(Attribute::isVerifiedAndCurrent)
                    .collect(Collectors.toList());
            if (valid.isEmpty()) {
                ProxyNodeLogger.info("No verified and current attributes: " + createAttributesMessage());
                valid = getAllAttributes().stream()
                        .filter(Attribute::isCurrent)
                        .collect(Collectors.toList());
            }
            return valid;
        }

        public List<Attribute<T>> getAllAttributes() {
            return Collections.unmodifiableList(attributes);
        }

        public String createAttributesMessage() {
            return "[ " + type + " " + attributes.stream()
                    .map(Attribute::toString)
                    .collect(Collectors.joining(", ")) + " ]";
        }

    }
}
