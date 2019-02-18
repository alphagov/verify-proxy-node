package uk.gov.ida.notification.exceptions;

import javax.ws.rs.WebApplicationException;

public class EidasSamlParserResponseException extends WebApplicationException{

    public EidasSamlParserResponseException(Throwable cause) {
        super(cause);
    }
}
