package uk.gov.ida.notification.exceptions.mappers;

import java.net.URI;
import java.util.logging.Level;

import uk.gov.ida.notification.exceptions.ErrorPageException;

public class ErrorPageExceptionMapper extends BaseExceptionToErrorPageMapper<ErrorPageException> {
    public ErrorPageExceptionMapper(URI errorPageRedirectUrl) {
        super(errorPageRedirectUrl);
    }

    @Override
    public Level getLogLevel(ErrorPageException exception) {
        return exception.getLogLevel();
    }
}
