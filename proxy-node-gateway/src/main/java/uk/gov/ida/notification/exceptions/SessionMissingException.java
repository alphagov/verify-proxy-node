package uk.gov.ida.notification.exceptions;

public class SessionMissingException extends RuntimeException {
    private String sessionId;

    public SessionMissingException(String sessionId) {
        super("Session should exist for session_id: " + sessionId);
        this.sessionId = sessionId;
    }

    public String getSessionId() {
        return sessionId;
    }
}

