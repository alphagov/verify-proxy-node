package uk.gov.ida.notification.exceptions;

public class RedisSerializationException extends ErrorPageException {
    public RedisSerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}