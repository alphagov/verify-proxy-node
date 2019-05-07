package uk.gov.ida.notification.exceptions.mappers;

import uk.gov.ida.common.ExceptionType;
import uk.gov.ida.exceptions.ApplicationException;
import uk.gov.ida.notification.exceptions.EidasSamlParserResponseException;

import javax.ws.rs.core.Response;
import java.net.URI;

public class EidasSamlParserResponseExceptionMapper extends ExceptionToErrorPageMapper<EidasSamlParserResponseException> {

    public EidasSamlParserResponseExceptionMapper(URI errorPageRedirectUrl) {
        super(errorPageRedirectUrl);
    }
}
