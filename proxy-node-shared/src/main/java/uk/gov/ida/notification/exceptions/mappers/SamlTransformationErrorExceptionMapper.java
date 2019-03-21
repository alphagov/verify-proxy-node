package uk.gov.ida.notification.exceptions.mappers;

import uk.gov.ida.saml.core.validation.SamlTransformationErrorException;

import javax.ws.rs.core.Response;

import static java.text.MessageFormat.format;

public class SamlTransformationErrorExceptionMapper extends BaseExceptionMapper<SamlTransformationErrorException> {

    @Override
    protected Response.Status getResponseStatus() {
        return Response.Status.BAD_REQUEST;
    }

    @Override
    protected String getResponseMessage(SamlTransformationErrorException exception) {
        return format("Error during AuthnRequest Signature Validation: {0};", exception.getMessage());
    }
}
