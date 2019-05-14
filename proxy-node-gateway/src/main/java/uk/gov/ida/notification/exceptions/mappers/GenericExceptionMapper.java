package uk.gov.ida.notification.exceptions.mappers;

import java.net.URI;
import java.util.logging.Level;

public class GenericExceptionMapper extends BaseExceptionToErrorPageMapper<Exception> {

    public GenericExceptionMapper(URI errorPageRedirectUrl) {
        super(errorPageRedirectUrl);
    }

    @Override
    public Level getLogLevel(Exception exception) {
        return Level.SEVERE;
    }
}
