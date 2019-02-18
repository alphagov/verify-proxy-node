package uk.gov.ida.notification.contracts.verifyserviceprovider;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

public class Attributes {

    @NotNull
    @JsonProperty
    private Attribute<String> firstName;

    @JsonProperty
    private List<Attribute<String>> middleNames;

    @NotNull
    @JsonProperty
    private List<Attribute<String>> surnames;

    @NotNull
    @JsonProperty
    private Attribute<DateTime> dateOfBirth;

    @JsonProperty
    private Attribute<String> gender;

    @JsonProperty
    private List<Attribute<Address>> addresses;

    @SuppressWarnings("Needed for JSON serialisation")
    public Attributes() {
    }

    public Attributes(
            Attribute<String> firstName,
            List<Attribute<String>> middleNames,
            List<Attribute<String>> surnames,
            Attribute<DateTime> dateOfBirth,
            Attribute<String> gender,
            List<Attribute<Address>> addresses) {
        this.firstName = firstName;
        this.middleNames = middleNames;
        this.surnames = surnames;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.addresses = addresses;
    }

    public Attribute<String> getFirstName() {
        return firstName;
    }

    public List<Attribute<String>> getMiddleNames() {
        return middleNames != null ? middleNames : new ArrayList<>();
    }

    public List<Attribute<String>> getSurnames() {
        return surnames;
    }

    public Attribute<DateTime> getDateOfBirth() {
        return dateOfBirth;
    }

    public Attribute<String> getGender() {
        return gender;
    }

    public List<Attribute<Address>> getAddresses() {
        return addresses;
    }
}