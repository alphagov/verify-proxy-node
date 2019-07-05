package uk.gov.ida.notification.exceptions.mappers;

import uk.gov.ida.notification.exceptions.metadata.MissingMetadataException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class MissingMetadataExceptionMapper implements ExceptionMapper<MissingMetadataException> {

    @Override
    public Response toResponse(MissingMetadataException exception) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Metadata temporarily unavailable").build();
    }
}
