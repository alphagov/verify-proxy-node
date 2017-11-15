package uk.gov.ida.notification.views;

import io.dropwizard.views.View;

public class SamlFormView extends View {
    private final String postUrl;
    private final String samlMessageType;
    private final String encodedSamlMessage;
    private final String submitText;

    public SamlFormView(String postUrl, String samlMessageType, String encodedSamlMessage, String submitText) {
        super("saml-form.mustache");
        this.postUrl = postUrl;
        this.samlMessageType = samlMessageType;
        this.encodedSamlMessage = encodedSamlMessage;
        this.submitText = submitText;
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

    public String getSubmitText() {
        return submitText;
    }
}
