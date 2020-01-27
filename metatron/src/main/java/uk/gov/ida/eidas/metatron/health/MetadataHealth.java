package uk.gov.ida.eidas.metatron.health;

import org.joda.time.DateTime;
import org.opensaml.saml.metadata.resolver.impl.AbstractReloadingMetadataResolver;

public class MetadataHealth {

    private String entityId;
    private DateTime expirationTime;
    private DateTime lastRefresh;
    private DateTime lastSuccessfulRefresh;
    private boolean wasLastRefreshSuccessful;
    private DateTime nextRefresh;

    public MetadataHealth(String entityId, AbstractReloadingMetadataResolver resolver) {
        this.entityId = entityId;
        this.expirationTime = resolver.getExpirationTime();
        this.lastRefresh = resolver.getLastRefresh();
        this.lastSuccessfulRefresh = resolver.getLastSuccessfulRefresh();
        this.wasLastRefreshSuccessful = resolver.wasLastRefreshSuccess();
        this.nextRefresh = resolver.getNextRefresh();
    }

    public String getEntityId() {
        return entityId;
    }

    public DateTime getExpirationTime() {
        return expirationTime;
    }

    public DateTime getLastRefresh() {
        return lastRefresh;
    }

    public DateTime getLastSuccessfulRefresh() {
        return lastSuccessfulRefresh;
    }

    public boolean isWasLastRefreshSuccessful() {
        return wasLastRefreshSuccessful;
    }

    public DateTime getNextRefresh() {
        return nextRefresh;
    }
}
