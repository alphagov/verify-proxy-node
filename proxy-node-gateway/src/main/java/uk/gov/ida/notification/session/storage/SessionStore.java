package uk.gov.ida.notification.session.storage;

import io.dropwizard.lifecycle.Managed;
import uk.gov.ida.notification.session.GatewaySessionData;

public interface SessionStore extends Managed {

    void addSession(String sessionId, GatewaySessionData sessionData);
    void createOrUpdateSession(String sessionId, GatewaySessionData sessionData);
    boolean sessionExists(String sessionId);
    GatewaySessionData getSession(String sessionId);
}
