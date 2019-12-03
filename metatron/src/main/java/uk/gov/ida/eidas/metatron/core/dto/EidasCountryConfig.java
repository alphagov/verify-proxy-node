package uk.gov.ida.eidas.metatron.core.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

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
    private String connectorMetadata;
    @JsonProperty
    @Valid
    @NotNull
    private boolean enabled;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    @JsonIgnore
    public String getEntityId() {
        return connectorMetadata;
    }

    public String getConnectorMetadata() {
        return connectorMetadata;
    }

    public void setConnectorMetadata(String connectorMetadata) {
        this.connectorMetadata = connectorMetadata;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
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
                .append("<td>metadata url:</td><td>").append(href(getConnectorMetadata())).append("</td>")
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
