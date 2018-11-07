package uk.gov.ida.notification.exceptions.hubresponse;

import org.apache.http.HttpResponse;
import javax.ws.rs.WebApplicationException;

public class TranslatorResponseException extends WebApplicationException {
    private final HttpResponse translatorResponse;

    public TranslatorResponseException(Throwable cause, HttpResponse translatorResponse) {
        super(cause);
        this.translatorResponse = translatorResponse;
    }

    public HttpResponse getTranslatorResponse() {
        return translatorResponse;
    }
}
