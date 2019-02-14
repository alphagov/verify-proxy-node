package uk.gov.ida.notification.contracts.verifyserviceprovider;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Address {

    @JsonProperty
    private List<String> lines;

    @JsonProperty
    private String postCode;

    @JsonProperty
    private String internationalPostCode;

    @JsonProperty
    private String uprn;

    @SuppressWarnings("Needed for JSON serialisation")
    public Address() {
    }

    public Address(
            List<String> lines,
            String postCode,
            String internationalPostCode,
            String uprn) {
        this.lines = lines;
        this.postCode = postCode;
        this.internationalPostCode = internationalPostCode;
        this.uprn = uprn;
    }

    public List<String> getLines() {
        return lines;
    }

    public String getPostCode() {
        return postCode;
    }

    public String getInternationalPostCode() {
        return internationalPostCode;
    }

    public String getUprn() {
        return uprn;
    }
}
