package uk.gov.ida.notification.exceptions.mappers.errorpage;

import uk.gov.ida.notification.exceptions.mappers.errorpage.ExceptionToErrorPageMapper;

import java.net.URI;

public class GenericExceptionMapper extends ExceptionToErrorPageMapper<Exception> {

    public GenericExceptionMapper(URI errorPageRedirectUrl) {
        super(errorPageRedirectUrl);
    }
}
