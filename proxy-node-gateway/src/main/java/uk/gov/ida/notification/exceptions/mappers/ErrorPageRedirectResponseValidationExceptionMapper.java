package uk.gov.ida.notification.exceptions.mappers;

import io.dropwizard.jersey.validation.JerseyViolationException;

import java.net.URI;
import java.util.logging.Level;

public class ErrorPageRedirectResponseValidationExceptionMapper extends BaseExceptionToErrorPageMapper<JerseyViolationException> {

    public ErrorPageRedirectResponseValidationExceptionMapper(URI errorPageRedirectUrl) {
        super(errorPageRedirectUrl);
    }

    @Override
    public Level getLogLevel(JerseyViolationException exception) { return Level.WARNING; }

}
