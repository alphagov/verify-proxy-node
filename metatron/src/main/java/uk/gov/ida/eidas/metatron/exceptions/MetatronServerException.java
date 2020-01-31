package uk.gov.ida.eidas.metatron.exceptions;

public class MetatronServerException extends RuntimeException {
    public MetatronServerException(String message) {
        super(message);
    }

    public MetatronServerException(String message, Throwable cause) {
        super(message, cause);
    }
}
