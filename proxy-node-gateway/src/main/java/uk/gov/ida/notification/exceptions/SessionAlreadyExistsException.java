package uk.gov.ida.notification.exceptions;

public class SessionAlreadyExistsException extends RuntimeException {

    private String sessionId;

    public SessionAlreadyExistsException(String sessionId) {
        super("Session already exists for session_id: " + sessionId);
        this.sessionId = sessionId;
    }

    public String getSessionId() {
        return sessionId;
    }
}