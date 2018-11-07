package uk.gov.ida.notification.stubconnector.views;

import io.dropwizard.views.View;

import java.util.List;

public class ResponseView extends View {
    private final List<String> attributes;

    public ResponseView(List<String> attributes) {
        super("response.mustache");
        this.attributes = attributes;
    }

    public List<String> getAttributes() {
        return attributes;
    }
}
