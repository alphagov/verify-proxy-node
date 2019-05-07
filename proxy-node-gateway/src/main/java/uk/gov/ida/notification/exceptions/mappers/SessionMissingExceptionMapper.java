package uk.gov.ida.notification.exceptions.mappers;

import uk.gov.ida.notification.exceptions.SessionMissingException;

import javax.ws.rs.core.Response;
import java.net.URI;

public class SessionMissingExceptionMapper extends ExceptionToErrorPageMapper<SessionMissingException> {

    public SessionMissingExceptionMapper(URI errorPageRedirectUrl) {
        super(errorPageRedirectUrl);
    }
}
