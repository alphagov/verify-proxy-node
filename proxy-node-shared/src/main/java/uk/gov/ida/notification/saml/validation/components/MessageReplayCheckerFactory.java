package uk.gov.ida.notification.saml.validation.components;

import org.opensaml.storage.ReplayCache;
import org.opensaml.storage.StorageService;
import org.opensaml.storage.impl.MemoryStorageService;

import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import se.litsec.opensaml.saml2.common.response.MessageReplayChecker;
import se.litsec.opensaml.saml2.common.response.MessageReplayCheckerImpl;

public class MessageReplayCheckerFactory {
    private MessageReplayCheckerFactory() {
    }

    public static MessageReplayChecker createMessageReplayChecker(String name) throws Exception {
        MessageReplayCheckerImpl checker = new MessageReplayCheckerImpl();
        checker.setReplayCache(createReplayCache(name + "-cache"));
        checker.setReplayCacheName(name);
        checker.afterPropertiesSet();
        return checker;
    }

    private static ReplayCache createReplayCache(String name) throws ComponentInitializationException {
        ReplayCache cache = new ReplayCache();
        cache.setId(name);
        cache.setStorage(createCacheStorage(name + "-storage"));
        cache.initialize();
        return cache;
    }

    private static StorageService createCacheStorage(String name) throws ComponentInitializationException {
        MemoryStorageService storage = new MemoryStorageService();
        storage.setId(name);
        storage.initialize();
        return storage;
    }
}
