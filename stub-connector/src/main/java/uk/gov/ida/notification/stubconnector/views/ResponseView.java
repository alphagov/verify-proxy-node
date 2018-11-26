package uk.gov.ida.notification.stubconnector.views;

import io.dropwizard.views.View;

import java.util.List;

public class ResponseView extends View {
    private final List<String> attributes;
    private final String validity;

    public ResponseView(List<String> attributes, String validity) {
        super("response.mustache");
        this.attributes = attributes;
        this.validity = validity;
    }

    public List<String> getAttributes() {
        return attributes;
    }

    public String getValidity() {
        return validity;
    }
}
