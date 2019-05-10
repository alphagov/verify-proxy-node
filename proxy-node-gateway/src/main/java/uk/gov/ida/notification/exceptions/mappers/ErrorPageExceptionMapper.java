package uk.gov.ida.notification.exceptions.mappers.errorpage;

import java.net.URI;
import java.util.logging.Level;

import uk.gov.ida.notification.exceptions.EidasErrorPageException;

public class ErrorPageExceptionMapper extends ExceptionToErrorPageMapper<EidasErrorPageException> {
    public ErrorPageExceptionMapper(URI errorPageRedirectUrl) {
        super(errorPageRedirectUrl);
    }

    @Override
    public Level getLogLevel(EidasErrorPageException exception) {
        return exception.getLogLevel();
    }
}
