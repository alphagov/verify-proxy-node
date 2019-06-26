package uk.gov.ida.notification.saml.validation.components;

import io.lettuce.core.api.sync.RedisCommands;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import org.opensaml.storage.ReplayCache;
import org.opensaml.storage.StorageService;
import org.opensaml.storage.impl.MemoryStorageService;
import se.litsec.opensaml.saml2.common.response.MessageReplayChecker;
import se.litsec.opensaml.saml2.common.response.MessageReplayCheckerImpl;
import uk.gov.ida.notification.session.storage.RedisStorageService;

public class MessageReplayCheckerFactory {
    private MessageReplayCheckerFactory() {
    }

    public static MessageReplayChecker createMessageReplayChecker(String name, ReplayCache replayCache) throws Exception {
        MessageReplayCheckerImpl checker = new MessageReplayCheckerImpl();
        checker.setReplayCache(replayCache);
        checker.setReplayCacheName(name);
        checker.afterPropertiesSet();
        return checker;
    }

    public static ReplayCache createReplayCache(String name, StorageService storageService) throws ComponentInitializationException {
        ReplayCache cache = new ReplayCache();
        cache.setId(name);
        cache.setStorage(storageService);
        cache.initialize();
        return cache;
    }

    public static StorageService createRedisCacheStorage(String name, RedisCommands<String, String> redis) throws ComponentInitializationException {
        RedisStorageService storage = new RedisStorageService(redis);
        storage.setId(name);
        storage.initialize();
        return storage;
    }

    public static StorageService createMemoryCacheStorage(String name) throws ComponentInitializationException {
        MemoryStorageService storage = new MemoryStorageService();
        storage.setId(name);
        storage.initialize();
        return storage;
    }
}
