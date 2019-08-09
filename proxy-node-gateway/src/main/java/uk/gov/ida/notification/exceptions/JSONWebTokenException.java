package uk.gov.ida.notification.exceptions;

public class JSONWebTokenException extends RuntimeException {

    public JSONWebTokenException(String message, Throwable e) {
        super(message, e);
    }

    public JSONWebTokenException(String message) {
        super(message);
    }
}
