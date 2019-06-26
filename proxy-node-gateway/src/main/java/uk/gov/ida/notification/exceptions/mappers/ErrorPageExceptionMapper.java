package uk.gov.ida.notification.exceptions.mappers;

import uk.gov.ida.notification.exceptions.ErrorPageException;

import java.net.URI;
import java.util.logging.Level;

public class ErrorPageExceptionMapper extends BaseExceptionToErrorPageMapper<ErrorPageException> {
    public ErrorPageExceptionMapper(URI errorPageRedirectUrl) {
        super(errorPageRedirectUrl);
    }

    @Override
    public Level getLogLevel(ErrorPageException exception) {
        return exception.getLogLevel();
    }
}
