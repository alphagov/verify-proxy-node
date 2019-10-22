package uk.gov.ida.notification.views;

import io.dropwizard.views.View;

import java.util.UUID;

public class SamlFormView extends View {
    private final String postUrl;
    private final String samlMessageType;
    private final String encodedSamlMessage;
    private final String relayState;

    public SamlFormView(String postUrl, String samlMessageType, String encodedSamlMessage) {
        this(postUrl, samlMessageType, encodedSamlMessage, UUID.randomUUID().toString());
    }

    public SamlFormView(String postUrl, String samlMessageType, String encodedSamlMessage, String relayState) {
        super("saml-form.mustache");
        this.postUrl = postUrl;
        this.samlMessageType = samlMessageType;
        this.encodedSamlMessage = encodedSamlMessage;
        this.relayState = relayState;
    }

    public String getPostUrl() {
        return postUrl;
    }

    public String getSamlMessageType() {
        return samlMessageType;
    }

    public String getEncodedSamlMessage() {
        return encodedSamlMessage;
    }

    public String getRelayState() {
        return relayState;
    }
}
