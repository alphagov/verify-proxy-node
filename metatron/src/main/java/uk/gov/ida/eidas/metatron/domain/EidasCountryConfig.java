package uk.gov.ida.eidas.metatron.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.security.KeyStore;
import java.util.Objects;
import java.util.Optional;

public class EidasCountryConfig {

    @JsonProperty
    @Valid
    @NotNull
    private String name;
    @JsonProperty
    @Valid
    @NotNull
    private String countryCode;
    @JsonProperty
    @Valid
    @NotNull
    private URI connectorMetadata;
    @JsonProperty
    @Valid
    @NotNull
    private boolean enabled;
    @JsonProperty
    private KeyStore metadataTruststore;
    @JsonProperty
    private KeyStore tlsTruststore;

    public String getName() {
        return name;
    }

    public String getCountryCode() {
        return countryCode;
    }

    @JsonIgnore
    public String getEntityId() {
        return connectorMetadata.toString();
    }

    public URI getConnectorMetadata() {
        return connectorMetadata;
    }

    public KeyStore getMetadataTruststore() {
        return this.metadataTruststore;
    }

    public Optional<KeyStore> getTlsTruststore() {
        return Optional.ofNullable(this.tlsTruststore);
    }

    @Override
    public String toString() {
        StringBuffer stringBuffer = new StringBuffer("<table>");
        stringBuffer
                .append("<tr>")
                .append("<td>name:</td><td>").append(getName()).append("</td>")
                .append("</tr>")
                .append("<tr>")
                .append("<td>country code:</td><td>").append(getCountryCode()).append("</td>")
                .append("</tr>")
                .append("<tr>")
                .append("<td>metadata url:</td><td>").append(href(getConnectorMetadata().toString())).append("</td>")
                .append("</tr>")
                .append("<tr>")
                .append("<td>entity id:</td><td>").append(href(getEntityId())).append("</td>")
                .append("</tr>")
                .append("</table>");
        return stringBuffer.toString();
    }
    private String href(String url) {
        return "<a href='" + url + "'>" + url + "</a>";
    }
}
