package uk.gov.ida.notification.exceptions.proxy;

import org.slf4j.MDC;
import uk.gov.ida.common.ExceptionType;
import uk.gov.ida.exceptions.ApplicationException;
import uk.gov.ida.notification.exceptions.mappers.BaseJsonErrorResponseRuntimeException;
import uk.gov.ida.notification.shared.logging.ProxyNodeMDCKey;

import javax.ws.rs.core.Response;

public class MetatronResponseException extends BaseJsonErrorResponseRuntimeException {

    public static final String ERROR_MESSAGE_FORMAT = "Error making request to Metatron with entityId '%s'";

    public MetatronResponseException(Throwable cause, String entityId) {
        super(String.format(ERROR_MESSAGE_FORMAT, entityId), cause);
        MDC.put(ProxyNodeMDCKey.EIDAS_ENTITY_ID.name(), entityId);
    }

    @Override
    public Response.Status getResponseStatus() {
        ApplicationException cause = (ApplicationException) this.getCause();
        return cause.getExceptionType() == ExceptionType.CLIENT_ERROR ?
                Response.Status.BAD_REQUEST : Response.Status.INTERNAL_SERVER_ERROR;
    }
}
