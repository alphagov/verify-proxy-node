package uk.gov.ida.notification.exceptions.mappers;

import uk.gov.ida.common.ExceptionType;
import uk.gov.ida.exceptions.ApplicationException;
import uk.gov.ida.notification.exceptions.TranslatorResponseException;

import javax.ws.rs.core.Response;

public class TranslatorResponseExceptionMapper extends ExceptionToErrorPageMapper<TranslatorResponseException> {

    @Override
    protected Response.Status getResponseStatus(TranslatorResponseException exception) {
        ApplicationException cause = (ApplicationException) exception.getCause();
        return cause.getExceptionType() == ExceptionType.CLIENT_ERROR ?
                Response.Status.BAD_REQUEST : Response.Status.INTERNAL_SERVER_ERROR;
    }

    @Override
    protected String getErrorPageMessage(TranslatorResponseException exception) {
        return "Something went wrong when contacting the Translator";
    }

    @Override
    protected String getSessionId(TranslatorResponseException exception) {
        return exception.getSessionId();
    }
}
