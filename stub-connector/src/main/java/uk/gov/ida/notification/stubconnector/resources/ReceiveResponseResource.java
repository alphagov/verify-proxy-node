package uk.gov.ida.notification.stubconnector.resources;

import com.codahale.metrics.annotation.Timed;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.Response;
import se.litsec.eidas.opensaml.ext.attributes.EidasAttributeValueType;
import uk.gov.ida.notification.saml.ResponseAssertionDecrypter;
import uk.gov.ida.notification.saml.SamlFormMessageType;
import uk.gov.ida.notification.stubconnector.views.ResponseView;
import uk.gov.ida.saml.security.AssertionDecrypter;
import uk.gov.ida.saml.security.validators.ValidatedResponse;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.stream.Collectors;

@Timed
@Path("/SAML2/Response")
public class ReceiveResponseResource {
    private final ResponseAssertionDecrypter decrypter;

    public ReceiveResponseResource(ResponseAssertionDecrypter decrypter) {
        this.decrypter = decrypter;
    }

    @POST
    @Path("/POST")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public ResponseView receiveResponse(
            @FormParam(SamlFormMessageType.SAML_RESPONSE) Response response,
            @FormParam("RelayState") String relayState) {

        // The eIDAS Response should only contain one Assertion with one AttributeStatement which contains
        // the user's requested attributes
        Assertion assertion = decrypter.decrypt(response).getAssertions().get(0);
        AttributeStatement attributeStatement = assertion.getAttributeStatements().get(0);
        List<String> attributes = attributeStatement.getAttributes().stream()
                .map(attr -> ((EidasAttributeValueType) attr.getAttributeValues().get(0)).toStringValue())
                .collect(Collectors.toList());

        return new ResponseView(attributes);
    }
}
