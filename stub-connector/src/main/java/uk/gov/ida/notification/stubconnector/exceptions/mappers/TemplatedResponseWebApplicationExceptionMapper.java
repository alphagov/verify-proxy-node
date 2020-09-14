package uk.gov.ida.notification.stubconnector.exceptions.mappers;

import org.opensaml.saml.common.assertion.ValidationResult;
import uk.gov.ida.notification.stubconnector.views.ResponseView;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//public class TemplatedResponseValidationExceptionMapper implements ExceptionMapper<JerseyViolationException> {
public class TemplatedResponseWebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {

    @Override
    //public Response toResponse(JerseyViolationException exception) {
    public Response toResponse(WebApplicationException exception) {
        List<Map.Entry<String, String>> attributesByName = new ArrayList<>();
        /*
        exception
            .getConstraintViolations()
            .forEach(violation -> attributesByName.add(new AbstractMap.SimpleEntry<String,String>(violation.getPropertyPath().toString(), violation.getMessage())));
         */

        attributesByName.add(new AbstractMap.SimpleEntry<String,String>("response", exception.getMessage()));

        ResponseView rv = new ResponseView(attributesByName, null, ValidationResult.INVALID.toString(), null, null, null);
        Response r = Response.status(400).entity(rv).build();
        return r;
    }


}
