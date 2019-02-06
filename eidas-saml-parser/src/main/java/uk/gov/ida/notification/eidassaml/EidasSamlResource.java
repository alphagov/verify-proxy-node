package uk.gov.ida.notification.eidassaml;

import net.shibboleth.utilities.java.support.xml.XMLParserException;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.saml.saml2.core.AuthnRequest;
import se.litsec.opensaml.utils.ObjectUtils;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.util.Base64;

@Path("/eidasAuthnRequest")
@Produces(MediaType.APPLICATION_JSON)
public class EidasSamlResource {
    @POST
    public ResponseDto post(RequestDto request) throws UnmarshallingException, XMLParserException {
        AuthnRequest authnRequest = ObjectUtils.unmarshall(
            new ByteArrayInputStream(Base64.getDecoder().decode(request.authnRequest.getBytes())),
            AuthnRequest.class);

        return new ResponseDto(
            authnRequest.getID(),
            authnRequest.getIssuer().getValue());
    }
}
