package uk.gov.ida.notification.eidassaml.apprule;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opensaml.saml.saml2.core.AuthnRequest;
import uk.gov.ida.notification.contracts.EidasSamlParserResponse;
import uk.gov.ida.notification.eidassaml.apprule.base.EidasSamlParserAppRuleTestBase;
import uk.gov.ida.notification.helpers.EidasAuthnRequestBuilder;
import uk.gov.ida.notification.helpers.X509CredentialFactory;
import uk.gov.ida.notification.saml.SamlObjectSigner;

import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PRIVATE_SIGNING_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PUBLIC_SIGNING_CERT;

public class EspAuthnRequestAppRuleTest extends EidasSamlParserAppRuleTestBase {

    private EidasAuthnRequestBuilder request;

    @Before
    public void setup() throws Throwable {
        request = new EidasAuthnRequestBuilder()
                .withIssuer(CONNECTOR_NODE_ENTITY_ID)
                .withDestination("http://proxy-node/eidasAuthnRequest");
    }

    @Test
    public void shouldReturnRequestIdAndIssuer() throws Exception {
        AuthnRequest postedRequest = request
                .withRequestId("request_id")
                .build();
        SamlObjectSigner samlObjectSigner = new SamlObjectSigner(
                X509CredentialFactory.build(TEST_RP_PUBLIC_SIGNING_CERT, TEST_RP_PRIVATE_SIGNING_KEY)
        );
        samlObjectSigner.sign(postedRequest);

        Response response = postEidasAuthnRequest(postedRequest);

        if (response.getStatus() != 200) {
            fail("Unsuccessful Response");
        }
        EidasSamlParserResponse responseDto = response.readEntity(EidasSamlParserResponse.class);

        assertEquals(responseDto.getRequestId(), "request_id");
        assertEquals(responseDto.getIssuer(), CONNECTOR_NODE_ENTITY_ID);
    }
}
