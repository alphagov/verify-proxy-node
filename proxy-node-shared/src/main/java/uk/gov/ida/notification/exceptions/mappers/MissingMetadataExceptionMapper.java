package uk.gov.ida.notification.exceptions.mappers;

import uk.gov.ida.notification.exceptions.metadata.MissingMetadataException;
import uk.gov.ida.notification.shared.logging.ProxyNodeLogger;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;

import static java.text.MessageFormat.format;

public class MissingMetadataExceptionMapper implements ExceptionMapper<MissingMetadataException> {

    private UriInfo uriInfo;

    @Context
    public void setUriInfo(UriInfo uriInfo) {
        this.uriInfo = uriInfo;
    }

    @Override
    public Response toResponse(MissingMetadataException exception) {
        ProxyNodeLogger.logException(exception, format("Metadata not found at [{0}]", uriInfo.getAbsolutePath()));
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Metadata temporarily unavailable").build();
    }
}
