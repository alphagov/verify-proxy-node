package uk.gov.ida.notification.contracts;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotBlank;
import uk.gov.ida.notification.validations.ValidBase64Xml;

public class EidasSamlParserRequest {

    @NotBlank
    @JsonProperty
    @ValidBase64Xml
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
