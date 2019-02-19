package uk.gov.ida.notification.eidassaml.apprule;

import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.saml2.core.AuthnRequest;
import uk.gov.ida.notification.contracts.EidasSamlParserResponse;
import uk.gov.ida.notification.eidassaml.apprule.base.EidasSamlParserAppRuleTestBase;
import uk.gov.ida.notification.helpers.EidasAuthnRequestBuilder;

import javax.ws.rs.core.Response;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class EspAuthnRequestAppRuleTest extends EidasSamlParserAppRuleTestBase {

    private EidasAuthnRequestBuilder request;

    public void setup() throws Throwable {
        request = new EidasAuthnRequestBuilder().withIssuer(CONNECTOR_NODE_ENTITY_ID);
    }

    public void shouldReturnRequestIdAndIssuer() throws XPathExpressionException, MarshallingException, TransformerException{
        AuthnRequest postedRequest = request
                .withRandomRequestId()
                .build();
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
