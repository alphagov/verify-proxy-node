package uk.gov.ida.stubs.saml;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opensaml.core.config.InitializationService;
import org.opensaml.saml.saml2.core.AuthnRequest;
import uk.gov.ida.notification.saml.AuthnRequestFactory;

import static org.junit.Assert.assertEquals;

public class AuthnRequestFactoryTest {
    @BeforeClass
    public static void setup() throws Exception {
        InitializationService.initialize();
    }

    @Test
    public void createEidasAuthnRequest() throws Exception {
        String issuerEntityId = "an-issuer";
        String destination = "a-destination";
        DateTime issueInstant = new DateTime(DateTimeZone.UTC);

        AuthnRequestFactory authnRequestFactory = new AuthnRequestFactory();
        AuthnRequest authnRequest = authnRequestFactory.createEidasAuthnRequest(issuerEntityId, destination, issueInstant);

        assertEquals(authnRequest.getIssuer().getValue(), issuerEntityId);
        assertEquals(authnRequest.getDestination(), destination);
        assertEquals(authnRequest.getIssueInstant(), issueInstant);
    }
}
