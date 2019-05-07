package uk.gov.ida.notification.exceptions.mappers;

import javax.ws.rs.core.Response;
import java.net.URI;

public class GenericExceptionMapper extends ExceptionToErrorPageMapper<Exception> {

    public GenericExceptionMapper(URI errorPageRedirectUrl) {
        super(errorPageRedirectUrl);
    }
}
