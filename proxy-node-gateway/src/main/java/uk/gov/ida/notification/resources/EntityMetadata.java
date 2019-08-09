package uk.gov.ida.notification.resources;

import java.util.HashMap;
import java.util.Map;

public class EntityMetadata {

    enum Key {encryptionCertificate, eidasDestination}

    private final Map<String, Map<Key, String>> data;

    public EntityMetadata() {
        this.data = new HashMap<>();
    }

    public synchronized void setData(String issuer, Key key, String value) {
        Map<Key, String> entry = data.getOrDefault(issuer, new HashMap());
        entry.put(key, value);
        data.put(issuer, entry);
    }

    public synchronized String getValue(String issuer, Key key) {
        Map<Key, String> entry = data.getOrDefault(issuer, new HashMap());
        String value = entry.get(key);
        if (value == null) {
            throw new IllegalStateException(String.format("no value for issuer %s and key %s", issuer, key));
        }
        return value;
    }
}
