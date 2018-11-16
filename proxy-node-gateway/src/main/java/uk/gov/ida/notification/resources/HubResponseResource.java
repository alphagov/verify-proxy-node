package uk.gov.ida.notification.resources;

import io.dropwizard.client.HttpClientBuilder;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.View;
import org.apache.http.NameValuePair;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.glassfish.jersey.internal.util.Base64;
import org.opensaml.saml.saml2.core.Response;
import uk.gov.ida.notification.SamlFormViewBuilder;
import uk.gov.ida.notification.exceptions.hubresponse.HubResponseException;
import uk.gov.ida.notification.exceptions.hubresponse.InvalidHubResponseException;
import uk.gov.ida.notification.exceptions.hubresponse.TranslatorResponseException;
import uk.gov.ida.notification.exceptions.saml.SamlParsingException;
import uk.gov.ida.notification.saml.SamlFormMessageType;
import uk.gov.ida.notification.saml.SamlObjectMarshaller;
import uk.gov.ida.notification.saml.SamlParser;
import uk.gov.ida.notification.saml.HubResponseContainer;
import uk.gov.ida.notification.saml.validation.HubResponseValidator;
import uk.gov.ida.notification.saml.validation.components.RequestIdWatcher;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Path("/SAML2/SSO/Response")
public class HubResponseResource {
    private static final Logger LOG = Logger.getLogger(HubResponseResource.class.getName());

    private final SamlFormViewBuilder samlFormViewBuilder;
    private final String connectorNodeUrl;
    private HubResponseValidator hubResponseValidator;
    private Environment environment;
    private String translatorUrl;
    private final RequestIdWatcher requestIdWatcher;

    public HubResponseResource(
            SamlFormViewBuilder samlFormViewBuilder,
            String connectorNodeUrl,
            HubResponseValidator hubResponseValidator,
            Environment environment,
            String translatorUrl,
            RequestIdWatcher requestIdWatcher) {
        this.samlFormViewBuilder = samlFormViewBuilder;
        this.connectorNodeUrl = connectorNodeUrl;
        this.hubResponseValidator = hubResponseValidator;
        this.environment = environment;
        this.translatorUrl = translatorUrl;
        this.requestIdWatcher = requestIdWatcher;
    }

    @POST
    @Path("/POST")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public View hubResponse(
            @FormParam(SamlFormMessageType.SAML_RESPONSE) Response encryptedHubResponse,
            @FormParam("RelayState") String relayState) {

        CloseableHttpResponse translatorResponse = null;

        try (CloseableHttpClient client = new HttpClientBuilder(environment).build("translator")) {

            hubResponseValidator.validate(encryptedHubResponse);
            if (!requestIdWatcher.haveSeenRequestFor(encryptedHubResponse)) {
                throw new InvalidHubResponseException(
                    String.format("Received a Response from Hub for an AuthnRequest we have not seen (ID: %s)", encryptedHubResponse.getInResponseTo()));
            }

            HubResponseContainer hubResponseContainer = HubResponseContainer.from(
                    hubResponseValidator.getValidatedResponse(),
                    hubResponseValidator.getValidatedAssertions()
            );
            logHubResponse(hubResponseContainer);

            String samlMessage = new SamlObjectMarshaller().transformToString(encryptedHubResponse);

            HttpPost httpPost = new HttpPost(UriBuilder.fromUri(translatorUrl).build());

            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair(SamlFormMessageType.SAML_RESPONSE, Base64.encodeAsString(samlMessage)));
            httpPost.setEntity(new UrlEncodedFormEntity(params));

            translatorResponse = client.execute(httpPost);

            ResponseHandler<String> handler = new BasicResponseHandler();
            String response = handler.handleResponse(translatorResponse);

            Response samlObject = new SamlParser().parseSamlString(response);
            logEidasResponse(samlObject);

            return samlFormViewBuilder.buildResponse(connectorNodeUrl, samlObject, "Post eIDAS Response SAML to Connector Node", relayState);

        } catch (UnsupportedEncodingException e) {
            throw new HubResponseException(e, encryptedHubResponse);
        } catch (SamlParsingException | IOException e) {
            throw new TranslatorResponseException(e, translatorResponse);
        } catch (Throwable e) {
            throw new HubResponseException(e, encryptedHubResponse);
        }
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
