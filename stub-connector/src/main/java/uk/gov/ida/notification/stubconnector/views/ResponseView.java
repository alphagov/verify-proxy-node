package uk.gov.ida.notification.stubconnector.views;

import io.dropwizard.views.View;

import java.util.List;
import java.util.Map.Entry;

public class ResponseView extends View {
    private final List<Entry<String, String>> attributes;
    private final String loa;
    private final String validity;
    private final String eidasRequestId;
    private final String issuerId;
    private String samlMessage;

    public ResponseView(List<Entry<String, String>> attributes, String loa, String validity, String eidasRequestId, String issuerId, String samlMessage) {
        super("response.mustache");
        this.attributes = attributes;
        this.loa = loa;
        this.validity = validity;
        this.eidasRequestId = eidasRequestId;
        this.issuerId = issuerId;
        this.samlMessage = samlMessage;
    }

    public List<Entry<String, String>> getAttributes() {
        return attributes;
    }

    public String getLoa() {
        return loa;
    }

    public String getValidity() {
        return validity;
    }

    public String getEidasRequestId() {
        return eidasRequestId;
    }

    public String getSamlMessage() {
        return samlMessage;
    }

    public String getIssuerId() {
        return issuerId;
    }
}
