package uk.gov.ida.notification.resources;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class EntityMetadataTest {

    @Test
    public void entityMetaValuesCanBeRead() {
        EntityMetadata entityMetadata = new EntityMetadata();
        entityMetadata.setData("issuer", EntityMetadata.Key.eidasDestination, "destination");
        String value = entityMetadata.getValue("issuer", EntityMetadata.Key.eidasDestination);
        assertThat(value).isEqualTo("destination");
    }

    @Test
    public void entityMetaValuesCanBeUpdated() {
        EntityMetadata entityMetadata = new EntityMetadata();
        entityMetadata.setData("issuer", EntityMetadata.Key.eidasDestination, "destination1");
        entityMetadata.setData("issuer", EntityMetadata.Key.eidasDestination, "destination2");
        String value = entityMetadata.getValue("issuer", EntityMetadata.Key.eidasDestination);
        assertThat(value).isEqualTo("destination2");
    }

    @Test(expected = IllegalStateException.class)
    public void when() {
        EntityMetadata entityMetadata = new EntityMetadata();
        entityMetadata.getValue("issuer", EntityMetadata.Key.eidasDestination);
    }
}