package uk.gov.ida.notification.contracts.verifyserviceprovider;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

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
    private List<Attribute<DateTime>> dateOfBirths;

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
            List<Attribute<DateTime>> dateOfBirths,
            Attribute<String> gender,
            List<Attribute<Address>> addresses) {
        this.firstNames = firstNames;
        this.middleNames = middleNames;
        this.surnames = surnames;
        this.dateOfBirths = dateOfBirths;
        this.gender = gender;
        this.addresses = addresses;
    }

    public List<Attribute<String>> getFirstNames() {
        return firstNames;
    }

    public List<Attribute<String>> getMiddleNames() {
        return middleNames != null ? middleNames : new ArrayList<>();
    }

    public List<Attribute<String>> getSurnames() {
        return surnames;
    }

    public List<Attribute<DateTime>> getDateOfBirths() {
        return dateOfBirths;
    }

    public Attribute<String> getGender() {
        return gender;
    }

    public List<Attribute<Address>> getAddresses() {
        return addresses;
    }
}