package uk.gov.ida.notification.resources;

import io.dropwizard.views.View;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.security.credential.UsageType;
import org.opensaml.security.x509.X509Credential;
import uk.gov.ida.notification.EidasResponseGenerator;
import uk.gov.ida.notification.SamlFormViewBuilder;
import uk.gov.ida.notification.exceptions.hubresponse.HubResponseException;
import uk.gov.ida.notification.saml.ResponseAssertionDecrypter;
import uk.gov.ida.notification.saml.ResponseAssertionEncrypter;
import uk.gov.ida.notification.saml.SamlFormMessageType;
import uk.gov.ida.notification.saml.metadata.Metadata;
import uk.gov.ida.notification.saml.translation.HubResponseContainer;
import uk.gov.ida.saml.security.validators.signature.SamlResponseSignatureValidator;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import java.util.logging.Logger;

@Path("/SAML2/SSO/Response")
public class HubResponseResource {
    private static final Logger LOG = Logger.getLogger(HubResponseResource.class.getName());

    private final EidasResponseGenerator eidasResponseGenerator;
    private final SamlFormViewBuilder samlFormViewBuilder;
    private final ResponseAssertionDecrypter assertionDecrypter;
    private final String connectorNodeUrl;
    private final String connectorEntityId;
    private final Metadata connectorMetadata;
    private final SamlResponseSignatureValidator hubResponseSignatureValidator;

    public HubResponseResource(
        EidasResponseGenerator eidasResponseGenerator,
        SamlFormViewBuilder samlFormViewBuilder,
        ResponseAssertionDecrypter assertionDecrypter,
        String connectorNodeUrl,
        String connectorEntityId,
        Metadata connectorMetadata,
        SamlResponseSignatureValidator hubResponseSignatureValidator) {
        this.assertionDecrypter = assertionDecrypter;
        this.connectorNodeUrl = connectorNodeUrl;
        this.eidasResponseGenerator = eidasResponseGenerator;
        this.samlFormViewBuilder = samlFormViewBuilder;
        this.connectorEntityId = connectorEntityId;
        this.connectorMetadata = connectorMetadata;
        this.hubResponseSignatureValidator = hubResponseSignatureValidator;
    }

    @POST
    @Path("/POST")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public View hubResponse(
            @FormParam(SamlFormMessageType.SAML_RESPONSE) Response encryptedHubResponse,
            @FormParam("RelayState") String relayState) {
        try {
            hubResponseSignatureValidator.validate(encryptedHubResponse, IDPSSODescriptor.DEFAULT_ELEMENT_NAME);

            Response decryptedHubResponse = assertionDecrypter.decrypt(encryptedHubResponse);

            HubResponseContainer hubResponseContainer = HubResponseContainer.fromResponse(decryptedHubResponse);
            logHubResponse(hubResponseContainer);

            ResponseAssertionEncrypter assertionEncrypter = createAssertionEncrypter();

            Response securedEidasResponse = eidasResponseGenerator.generate(hubResponseContainer, assertionEncrypter);
            logEidasResponse(securedEidasResponse);

            return samlFormViewBuilder.buildResponse(connectorNodeUrl, securedEidasResponse, "Post eIDAS Response SAML to Connector Node", relayState);
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
