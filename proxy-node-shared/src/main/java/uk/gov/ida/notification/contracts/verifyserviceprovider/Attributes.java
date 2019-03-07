package uk.gov.ida.notification.contracts.verifyserviceprovider;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.format.ISODateTimeFormat;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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

    public List<Attribute<String>> getFirstNames() {
        return firstNames != null ? firstNames : new ArrayList<>();
    }

    public List<Attribute<String>> getMiddleNames() {
        return middleNames != null ? middleNames : new ArrayList<>();
    }

    public List<Attribute<String>> getSurnames() {
        return surnames != null ? surnames : new ArrayList<>();
    }

    public List<Attribute<DateTime>> getDatesOfBirth() {
        return datesOfBirth != null ? datesOfBirth : new ArrayList<>();
    }

    public Attribute<String> getGender() {
        return gender;
    }

    public List<Attribute<Address>> getAddresses() {
        return addresses;
    }

    public static String combineAttributeValues(List<Attribute<String>> attributes) {
        return attributes.stream()
                .filter(Objects::nonNull)
                .map(Attribute::getValue)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(" "));
    }

    // Prints date in EIDAS format YYYY-MM-dd
    public static String getFormattedDate(DateTime date) {
        return ISODateTimeFormat.date().withChronology(ISOChronology.getInstanceUTC()).print(date);
    }
}
