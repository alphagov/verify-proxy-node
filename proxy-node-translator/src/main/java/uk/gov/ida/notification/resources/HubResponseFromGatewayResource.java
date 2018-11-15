package uk.gov.ida.notification.resources;

import com.codahale.metrics.annotation.Timed;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.security.credential.UsageType;
import org.opensaml.security.x509.X509Credential;
import uk.gov.ida.notification.EidasResponseGenerator;
import uk.gov.ida.notification.exceptions.hubresponse.HubResponseException;
import uk.gov.ida.notification.saml.ResponseAssertionEncrypter;
import uk.gov.ida.notification.saml.SamlFormMessageType;
import uk.gov.ida.notification.saml.SamlObjectMarshaller;
import uk.gov.ida.notification.saml.metadata.Metadata;
import uk.gov.ida.notification.saml.HubResponseContainer;
import uk.gov.ida.notification.saml.validation.HubResponseValidator;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.logging.Logger;

@Timed
@Path("/SAML2/SSO/Response")
public class HubResponseFromGatewayResource {
    private static final Logger LOG = Logger.getLogger(HubResponseFromGatewayResource.class.getName());

    private final EidasResponseGenerator eidasResponseGenerator;
    private final String connectorNodeUrl;
    private final String connectorEntityId;
    private final Metadata connectorMetadata;
    private HubResponseValidator hubResponseValidator;

    public HubResponseFromGatewayResource(
        EidasResponseGenerator eidasResponseGenerator,
        String connectorNodeUrl,
        String connectorEntityId,
        Metadata connectorMetadata,
        HubResponseValidator hubResponseValidator) {
        this.connectorNodeUrl = connectorNodeUrl;
        this.eidasResponseGenerator = eidasResponseGenerator;
        this.connectorEntityId = connectorEntityId;
        this.connectorMetadata = connectorMetadata;
        this.hubResponseValidator = hubResponseValidator;
    }

    @POST
    @Path("/POST")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_XML)
    public javax.ws.rs.core.Response hubResponse(@FormParam(SamlFormMessageType.SAML_RESPONSE) Response encryptedHubResponse) {

        try {
            hubResponseValidator.validate(encryptedHubResponse);

            HubResponseContainer hubResponseContainer = HubResponseContainer.from(
                    hubResponseValidator.getValidatedResponse(),
                    hubResponseValidator.getValidatedAssertions()
            );
            logHubResponse(hubResponseContainer);

            ResponseAssertionEncrypter assertionEncrypter = createAssertionEncrypter();

            Response securedEidasResponse = eidasResponseGenerator.generate(hubResponseContainer, assertionEncrypter);
            logEidasResponse(securedEidasResponse);

            SamlObjectMarshaller marshaller = new SamlObjectMarshaller();
            String samlMessage = marshaller.transformToString(securedEidasResponse);

            javax.ws.rs.core.Response.ResponseBuilder builder = javax.ws.rs.core.Response.ok();
            return builder.entity(samlMessage).build();

        } catch (Throwable e) {
            throw new HubResponseException(e, encryptedHubResponse);
        }
    }

    private ResponseAssertionEncrypter createAssertionEncrypter() {
        X509Credential encryptionCredential = (X509Credential) connectorMetadata.getCredential(UsageType.ENCRYPTION, connectorEntityId, SPSSODescriptor.DEFAULT_ELEMENT_NAME);
        return new ResponseAssertionEncrypter(encryptionCredential);
    }

    private void logHubResponse(HubResponseContainer hubResponseContainer) {
        LOG.info("[Hub Response] ID: " + hubResponseContainer.getHubResponse().getResponseId());
        LOG.info("[Hub Response] In response to: " + hubResponseContainer.getHubResponse().getInResponseTo());
        LOG.info("[Hub Response] Provided level of assurance: " + hubResponseContainer.getAuthnAssertion().getProvidedLoa());
    }

    private void logEidasResponse(Response eidasResponse) {
        LOG.info("[eIDAS Response] ID: " + eidasResponse.getID());
        LOG.info("[eIDAS Response] In response to: " + eidasResponse.getInResponseTo());
    }

}
