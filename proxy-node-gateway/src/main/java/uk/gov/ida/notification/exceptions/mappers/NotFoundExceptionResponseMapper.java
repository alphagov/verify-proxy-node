package uk.gov.ida.notification.exceptions.mappers;

import uk.gov.ida.notification.shared.logging.ProxyNodeLogger;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.NotFoundException;
import java.util.logging.Level;

import static java.text.MessageFormat.format;

public class NotFoundExceptionResponseMapper implements ExceptionMapper<NotFoundException> {
    private UriInfo uriInfo;

    public NotFoundExceptionResponseMapper() {

    }

    @Context
    public void setUriInfo(UriInfo uriInfo) {
        this.uriInfo = uriInfo;
    }

    @Override
    public Response toResponse(NotFoundException exception) {
        ProxyNodeLogger.logException(exception, Level.FINEST,
                                     format("404 Not Found for URI [{0}]", uriInfo.getAbsolutePath()));

        return Response.status(Response.Status.NOT_FOUND).build();
    }


}
