package uk.gov.ida.notification.exceptions;

import uk.gov.ida.notification.dto.EidasSamlParserResponse;

import javax.validation.ConstraintViolation;
import javax.ws.rs.WebApplicationException;
import java.util.Set;
import java.util.logging.Logger;

public class EidasSamlParserResponseException extends WebApplicationException {

    private final Logger log = Logger.getLogger(getClass().getName());

    public EidasSamlParserResponseException(Set<ConstraintViolation<EidasSamlParserResponse>> violations) {
        super("Invalid EidasSamlParserResponse");
        for (ConstraintViolation<EidasSamlParserResponse> violation : violations) {
            log.warning(
                String.format(
                    "Invalid EidasSamlParserResponse: Property '%s' %s",
                    violation.getPropertyPath().toString(),
                    violation.getMessage()
                )
            );
        }
    }
}
