package uk.gov.ida.notification.contracts;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EidasSamlParserRequest {

    @JsonProperty
    private String authnRequest;

    @SuppressWarnings("Needed for serialisation")
    public EidasSamlParserRequest() {
    }

    public EidasSamlParserRequest(String authnRequest) {
        this.authnRequest = authnRequest;
    }

    public String getAuthnRequest() {
        return authnRequest;
    }
}
