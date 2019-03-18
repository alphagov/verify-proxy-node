package uk.gov.ida.notification.exceptions.mappers;

import uk.gov.ida.common.ExceptionType;
import uk.gov.ida.exceptions.ApplicationException;
import uk.gov.ida.notification.exceptions.EidasSamlParserResponseException;

import javax.ws.rs.core.Response;

public class EidasSamlParserResponseExceptionMapper extends ExceptionToErrorPageMapper<EidasSamlParserResponseException> {

    @Override
    protected Response.Status getResponseStatus(EidasSamlParserResponseException exception) {
        ApplicationException cause = (ApplicationException) exception.getCause();
        return cause.getExceptionType() == ExceptionType.CLIENT_ERROR ?
                Response.Status.BAD_REQUEST : Response.Status.INTERNAL_SERVER_ERROR;
    }

    @Override
    protected String getErrorPageMessage(EidasSamlParserResponseException exception) {
        return "Something went wrong when contacting the ESP";
    }

    @Override
    protected String getSessionId(EidasSamlParserResponseException exception) {
        return exception.getSessionId();
    }
}
