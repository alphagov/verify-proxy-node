package uk.gov.ida.notification.session.storage;

import uk.gov.ida.notification.exceptions.SessionAlreadyExistsException;
import uk.gov.ida.notification.exceptions.SessionMissingException;
import uk.gov.ida.notification.session.GatewaySessionData;

import java.util.HashMap;

public class InMemoryStorage implements SessionStore {

    private HashMap<String, GatewaySessionData> storage;

    @Override
    public void addSession(String sessionId, GatewaySessionData sessionData) {
        if (sessionExists(sessionId)) {
            GatewaySessionData existingSession = getSession(sessionId);
            String hubRequestId = existingSession.getHubRequestId();
            String eidasRequestId = existingSession.getEidasRequestId();

            throw new SessionAlreadyExistsException(sessionId, hubRequestId, eidasRequestId);
        }
        this.createOrUpdateSession(sessionId, sessionData);
    }

    @Override
    public void createOrUpdateSession(String sessionId, GatewaySessionData sessionData) {
        storage.put(sessionId, sessionData);
    }

    @Override
    public boolean sessionExists(String sessionId) {
        return storage.containsKey(sessionId);
    }

    @Override
    public GatewaySessionData getSession(String sessionId) {
        if (!sessionExists(sessionId)) throw new SessionMissingException(sessionId);
        return storage.get(sessionId);
    }

    @Override
    public void start() {
        storage = new HashMap<>();
    }

    @Override
    public void stop() {
        storage = null;
    }
}
