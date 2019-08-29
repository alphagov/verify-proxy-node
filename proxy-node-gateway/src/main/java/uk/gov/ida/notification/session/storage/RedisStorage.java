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
    private RedisClient redisClientWrite;
    private final RedisClient redisClientRead;
    private StatefulRedisConnection<String, GatewaySessionData> redisConnectionWrite;
    private RedisCommands<String, GatewaySessionData> redisCommandsWrite;
    private StatefulRedisConnection<String, GatewaySessionData> redisConnectionRead;
    private RedisCommands<String, GatewaySessionData> redisCommandsRead;

    public RedisStorage(RedisServiceConfiguration redisConfiguration) {
        this.redisConfiguration = redisConfiguration;
        redisClientWrite = RedisClient.create(RedisURI.create(redisConfiguration.getUrlWrite()));
        redisClientRead = RedisClient.create(RedisURI.create(redisConfiguration.getUrlRead()));
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
        redisCommandsWrite.setex(sessionId, redisConfiguration.getRecordTTL(), sessionData);
    }

    @Override
    public boolean sessionExists(String sessionId) {
        return redisCommandsRead.exists(sessionId) > 0;
    }

    @Override
    public GatewaySessionData getSession(String sessionId) {
        if (!sessionExists(sessionId)) throw new SessionMissingException(sessionId);
        return redisCommandsRead.get(sessionId);
    }

    @Override
    public void start() {
        redisConnectionWrite = redisClientWrite.connect(new SessionRedisCodec());
        redisCommandsWrite = redisConnectionWrite.sync();
        redisConnectionRead = redisClientRead.connect(new SessionRedisCodec());
        redisCommandsRead = redisConnectionRead.sync();
    }

    @Override
    public void stop() {
        redisConnectionWrite.close();
        redisClientWrite.shutdown();
        redisConnectionRead.close();
        redisClientRead.shutdown();
    }
}
