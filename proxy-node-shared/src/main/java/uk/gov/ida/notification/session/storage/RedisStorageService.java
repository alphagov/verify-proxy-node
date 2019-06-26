package uk.gov.ida.notification.session.storage;

import io.lettuce.core.KeyScanCursor;
import io.lettuce.core.KeyValue;
import io.lettuce.core.ScanArgs;
import io.lettuce.core.ScanCursor;
import io.lettuce.core.TransactionResult;
import io.lettuce.core.api.sync.RedisCommands;
import net.shibboleth.utilities.java.support.collection.Pair;
import org.opensaml.storage.AbstractStorageService;
import org.opensaml.storage.StorageRecord;
import org.opensaml.storage.VersionMismatchException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class RedisStorageService extends AbstractStorageService {

    private static final Long INITIAL_VERSION = 1L;

    private final RedisCommands<String, String> redis;

    public RedisStorageService(RedisCommands<String, String> redis) {
        this.setContextSize(1024);
        this.redis = redis;
    }

    private static String key(String context, String key) {
        return context + ":" + key;
    }

    @Override
    public boolean create(String context, String key, String value, Long expiration) {
        Boolean result = redis.hsetnx(key(context, key), "value", value);
        if (!result) return false;

        redis.hset(key(context, key), "version", INITIAL_VERSION.toString());
        redis.hset(key(context, key), "expiration", expiration.toString());
        redis.pexpireat(key(context, key), expiration);
        return true;
    }

    @Override
    public StorageRecord read(String context, String key) throws IOException {
        List<KeyValue<String, String>> result = redis.hmget(key(context, key), "value", "version", "expiration");
        String value = result.get(0).getValueOrElse(null);
        if (value == null) return null;

        Long version = Long.valueOf(result.get(1).getValue());
        Long expiration = Long.valueOf(result.get(2).getValue());
        return new RedisStorageRecord(value, expiration, version);
    }

    @Override
    public Pair<Long, StorageRecord> read(String context, String key, long version) throws IOException {
        Long storedVersion = getCurrentVersion(context, key);
        if (storedVersion == null) {
            return new Pair<>(null, null);
        } else if (version == storedVersion) {
            return new Pair<>(storedVersion, null);
        } else {
            StorageRecord value = read(context, key);
            return new Pair<>(storedVersion, value);
        }
    }

    private Long getCurrentVersion(String context, String key) {
        String storedVersionString = redis.hget(key(context, key), "version");
        if (storedVersionString == null) return null;

        return Long.valueOf(storedVersionString);
    }

    @Override
    public boolean update(String context, String key, String value, Long expiration) throws IOException {
        redis.watch(key(context, key));
        Long storedVersion = getCurrentVersion(context, key);
        if (storedVersion == null) {
            redis.unwatch();
            return false;
        }

        redis.multi();
        redis.hmset(key(context, key), Map.of("value", value, "expiration", Long.toString(expiration), "version", Long.toString(storedVersion + 1)));
        TransactionResult result = redis.exec();
        if (result.wasDiscarded()) {
            throw new IOException("Failed to update the object due to a Redis transaction conflict.");
        }
        return true;
    }

    @Override
    public Long updateWithVersion(long version, String context, String key, String value, Long expiration) throws IOException, VersionMismatchException {
        redis.watch(key(context, key));
        Long storedVersion = getCurrentVersion(context, key);
        if (storedVersion == null) {
            redis.unwatch();
            return null;
        } else if (storedVersion != version) {
            redis.unwatch();
            throw new VersionMismatchException(String.format("Expected version to be %l but it was %l", version, storedVersion));
        } else {
            redis.multi();
            redis.hmset(key(context, key), Map.of("value", value, "expiration", Long.toString(expiration), "version", Long.toString(storedVersion + 1)));
            TransactionResult result = redis.exec();
            if (result.wasDiscarded()) {
                throw new IOException("Failed to update the object due to a Redis transaction conflict.");
            }
            return storedVersion + 1;
        }
    }

    @Override
    public boolean updateExpiration(String context, String key, Long expiration) throws IOException {
        redis.watch(key(context, key));
        Boolean exists = redis.exists(key(context, key)) > 0;
        if (!exists) return false;

        redis.multi();
        redis.hset(key(context, key), "expiration", Long.toString(expiration));
        TransactionResult result = redis.exec();
        if (result.wasDiscarded()) {
            throw new IOException("Failed to update the object due to a Redis transaction conflict.");
        }
        return true;
    }

    @Override
    public boolean delete(String context, String key) {
        return redis.del(key(context, key)) > 0;
    }

    @Override
    public boolean deleteWithVersion(long version, String context, String key) throws IOException, VersionMismatchException {
        redis.watch(key(context, key));
        Long storedVersion = getCurrentVersion(context, key);
        if (storedVersion == null) {
            redis.unwatch();
            return false;
        } else if (storedVersion != version) {
            redis.unwatch();
            throw new VersionMismatchException(String.format("Expected version to be %l but it was %l", version, storedVersion));
        } else {
            redis.multi();
            redis.del(key(context, key));
            TransactionResult result = redis.exec();
            if (result.wasDiscarded()) {
                throw new IOException("Failed to update the object due to a Redis transaction conflict.");
            }
        }
        return true;
    }

    @Override
    public void reap(String context) {
        // Don't need to do anything here as Redis will handle key expiry.
    }

    @Override
    public void updateContextExpiration(String context, Long expiration) {
        ScanCursor cursor = ScanCursor.INITIAL;
        do {
            KeyScanCursor<String> keyCursor = redis.scan(cursor, ScanArgs.Builder.matches(context + ":"));
            keyCursor.getKeys().forEach(key -> {
                redis.pexpireat(key, expiration);
            });
            cursor = keyCursor;
        } while (cursor != ScanCursor.FINISHED);
    }

    @Override
    public void deleteContext(String context) {
        ScanCursor cursor = ScanCursor.INITIAL;
        do {
            KeyScanCursor<String> keyCursor = redis.scan(cursor, ScanArgs.Builder.matches(context + ":"));
            keyCursor.getKeys().forEach(key -> {
                redis.del(key);
            });
            cursor = keyCursor;
        } while (cursor != ScanCursor.FINISHED);
    }
}
