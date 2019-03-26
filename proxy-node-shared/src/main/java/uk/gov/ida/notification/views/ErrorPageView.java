package uk.gov.ida.notification.views;

import io.dropwizard.views.View;

public class ErrorPageView extends View {

    public ErrorPageView() {
        super("error-page.mustache");
    }
}
