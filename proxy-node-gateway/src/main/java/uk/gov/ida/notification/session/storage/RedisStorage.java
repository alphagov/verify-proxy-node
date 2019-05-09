package uk.gov.ida.notification.session.storage;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import uk.gov.ida.notification.configuration.RedisServiceConfiguration;
import uk.gov.ida.notification.exceptions.SessionAlreadyExistsException;
import uk.gov.ida.notification.exceptions.SessionMissingException;
import uk.gov.ida.notification.session.GatewaySessionData;

public class RedisStorage implements SessionStore {

    private RedisServiceConfiguration redisConfiguration;

    private RedisClient redisClient;
    private StatefulRedisConnection<String, GatewaySessionData> redisConnection;
    private RedisCommands<String, GatewaySessionData> redisCommands;

    public RedisStorage(RedisServiceConfiguration redisConfiguration) {
        this.redisConfiguration = redisConfiguration;

        RedisURI redisURI = RedisURI.create(redisConfiguration.getUrl());
        redisClient = RedisClient.create(redisURI);
    }

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
        redisCommands.setex(sessionId, redisConfiguration.getRecordTTL(), sessionData);
    }

    @Override
    public boolean sessionExists(String sessionId) {
        return redisCommands.exists(sessionId) > 0;
    }

    @Override
    public GatewaySessionData getSession(String sessionId) {
        if (!sessionExists(sessionId)) throw new SessionMissingException(sessionId);
        return redisCommands.get(sessionId);
    }

    @Override
    public void start() {
        redisConnection = redisClient.connect(new SessionRedisCodec());
        redisCommands = redisConnection.sync();
    }

    @Override
    public void stop() {
        redisConnection.close();
        redisClient.shutdown();
    }
}
