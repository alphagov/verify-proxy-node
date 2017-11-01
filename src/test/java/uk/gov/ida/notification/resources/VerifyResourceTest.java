package uk.gov.ida.notification.resources;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class VerifyResourceTest {
    @Test
    public void handlePostReturnsSuccess() {
        VerifyResource resource = new VerifyResource();
        assertEquals("Success!", resource.handlePost());
    }
}
