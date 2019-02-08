package uk.gov.ida.notification.eidassaml.apprule;

import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.saml2.core.AuthnRequest;
import uk.gov.ida.notification.eidassaml.ResponseDto;
import uk.gov.ida.notification.eidassaml.apprule.EidasSamlParserAppRuleTestBase;
import uk.gov.ida.notification.helpers.EidasAuthnRequestBuilder;

import javax.ws.rs.core.Response;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class EspAuthnRequestAppRuleTest extends EidasSamlParserAppRuleTestBase {

    private EidasAuthnRequestBuilder request;

    @Before
    public void setup() throws Throwable {
        request = new EidasAuthnRequestBuilder().withIssuer(CONNECTOR_NODE_ENTITY_ID);
    }

    @Test
    public void shouldReturnRequestIdAndIssuer() throws XPathExpressionException, MarshallingException, TransformerException{
        AuthnRequest postedRequest = request
                .withRequestId("request_id")
                .withIssuer("issuer")
                .build();
        Response response = postEidasAuthnRequest(postedRequest);
        ResponseDto responseDto = response.readEntity(ResponseDto.class);

        assertEquals(responseDto.requestId, "request_id");
        assertEquals(responseDto.issuer, "issuer");
    }
}
