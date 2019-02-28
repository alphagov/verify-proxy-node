package uk.gov.ida.notification.exceptions.mappers;

import uk.gov.ida.saml.core.validation.SamlTransformationErrorException;
import javax.ws.rs.core.Response;
import static java.text.MessageFormat.format;

public class SamlTransformationErrorExceptionMapper extends BaseExceptionMapper<SamlTransformationErrorException> {

    @Override
    protected void handleException(SamlTransformationErrorException exception) { }

    @Override
    protected Response getResponse(SamlTransformationErrorException exception) {
        final String message = format("Error during AuthnRequest Signature Validation: {0};", exception.getMessage());
        return Response.status(Response.Status.BAD_REQUEST).entity(message).build();
    }
}
