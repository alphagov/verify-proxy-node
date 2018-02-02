package uk.gov.ida.notification.resources;

import io.dropwizard.views.View;
import net.shibboleth.utilities.java.support.resolver.ResolverException;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.security.SecurityException;
import org.opensaml.security.credential.BasicCredential;
import org.opensaml.security.x509.X509Credential;
import uk.gov.ida.notification.EidasResponseGenerator;
import uk.gov.ida.notification.SamlFormViewBuilder;
import uk.gov.ida.notification.saml.ResponseAssertionDecrypter;
import uk.gov.ida.notification.saml.ResponseAssertionEncrypter;
import uk.gov.ida.notification.saml.SamlFormMessageType;
import uk.gov.ida.notification.saml.metadata.Metadata;
import uk.gov.ida.notification.saml.translation.HubResponseContainer;
import uk.gov.ida.notification.saml.validation.SamlSignatureValidator;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import java.security.PublicKey;
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
    private final Metadata hubMetadata;

    public HubResponseResource(EidasResponseGenerator eidasResponseGenerator, SamlFormViewBuilder samlFormViewBuilder, ResponseAssertionDecrypter assertionDecrypter, String connectorNodeUrl, String connectorEntityId, Metadata connectorMetadata, Metadata hubMetadata) {
        this.assertionDecrypter = assertionDecrypter;
        this.connectorNodeUrl = connectorNodeUrl;
        this.eidasResponseGenerator = eidasResponseGenerator;
        this.samlFormViewBuilder = samlFormViewBuilder;
        this.connectorEntityId = connectorEntityId;
        this.connectorMetadata = connectorMetadata;
        this.hubMetadata = hubMetadata;
    }

    @POST
    @Path("/POST")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public View hubResponse(
            @FormParam(SamlFormMessageType.SAML_RESPONSE) Response encryptedHubResponse,
            @FormParam("RelayState") String relayState) throws SecurityException, ResolverException, MarshallingException {
        SamlSignatureValidator samlSignatureValidator = new SamlSignatureValidator();
        PublicKey hubPublicKey = hubMetadata.getSigningPublicKey(encryptedHubResponse.getIssuer().getValue());

        samlSignatureValidator.validateResponse(new BasicCredential(hubPublicKey), encryptedHubResponse);
        Response decryptedHubResponse = assertionDecrypter.decrypt(encryptedHubResponse);


        HubResponseContainer hubResponseContainer = HubResponseContainer.fromResponse(decryptedHubResponse);
        logHubResponse(hubResponseContainer);

        ResponseAssertionEncrypter assertionEncrypter = createAssertionEncrypter();

        Response securedEidasResponse = eidasResponseGenerator.generate(hubResponseContainer, assertionEncrypter);
        logEidasResponse(securedEidasResponse);

        return samlFormViewBuilder.buildResponse(connectorNodeUrl, securedEidasResponse, "Post eIDAS Response SAML to Connector Node", relayState);
    }

    private ResponseAssertionEncrypter createAssertionEncrypter() throws ResolverException, SecurityException {
        X509Credential encryptionCredential = connectorMetadata.getEncryptionCredential(connectorEntityId);
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
