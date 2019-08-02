package uk.gov.ida.notification.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.sync.RedisCommands;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import org.opensaml.storage.ReplayCache;
import org.opensaml.storage.StorageService;
import org.opensaml.storage.impl.MemoryStorageService;
import se.litsec.opensaml.saml2.common.response.MessageReplayChecker;
import se.litsec.opensaml.saml2.common.response.MessageReplayCheckerImpl;
import uk.gov.ida.notification.session.storage.RedisStorageService;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class ReplayCheckerConfiguration {

    @Valid
    @NotNull
    @JsonProperty
    private String redisUrl = "";

    public MessageReplayChecker createMessageReplayChecker(String name) throws Exception {
        MessageReplayCheckerImpl checker = new MessageReplayCheckerImpl();
        checker.setReplayCache(createReplayCache(name + "-replay-cache", createStorageService(name)));
        checker.setReplayCacheName(name);
        checker.afterPropertiesSet();
        return checker;
    }

    private StorageService createStorageService(String name) throws ComponentInitializationException {
        if (redisUrl.isEmpty()) {
            return createMemoryCacheStorage(name + "-cache-storage");
        } else {
            RedisCommands<String, String> sync = RedisClient
                .create(redisUrl)
                .connect()
                .sync();
            return createRedisCacheStorage(name + "-cache-storage", sync);
        }
    }

    private ReplayCache createReplayCache(String name, StorageService storageService) throws ComponentInitializationException {
        ReplayCache cache = new ReplayCache();
        cache.setId(name);
        cache.setStorage(storageService);
        cache.initialize();
        return cache;
    }

    private StorageService createRedisCacheStorage(String name, RedisCommands<String, String> redis) throws ComponentInitializationException {
        RedisStorageService storage = new RedisStorageService(redis);
        storage.setId(name);
        storage.initialize();
        return storage;
    }

    private StorageService createMemoryCacheStorage(String name) throws ComponentInitializationException {
        MemoryStorageService storage = new MemoryStorageService();
        storage.setId(name);
        storage.initialize();
        return storage;
    }
}
