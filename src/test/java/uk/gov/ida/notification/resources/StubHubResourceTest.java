package uk.gov.ida.notification.resources;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StubHubResourceTest {
    @Test
    public void shouldAppendVerifiedStringToRequestBody() {
        StubHubResource resource = new StubHubResource();
        assertEquals("hello - Verified By Hub", resource.handlePost("hello"));
    }
}