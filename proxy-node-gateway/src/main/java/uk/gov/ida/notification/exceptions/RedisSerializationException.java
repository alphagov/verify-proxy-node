package uk.gov.ida.notification.exceptions;

public class RedisSerializationException extends RuntimeException {
    public RedisSerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}