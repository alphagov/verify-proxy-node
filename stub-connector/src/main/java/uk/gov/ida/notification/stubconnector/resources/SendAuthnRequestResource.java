package uk.gov.ida.notification.stubconnector.resources;

import io.dropwizard.jersey.sessions.Session;
import io.dropwizard.views.View;
import net.shibboleth.utilities.java.support.resolver.ResolverException;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.xmlsec.signature.Signature;
import se.litsec.eidas.opensaml.common.EidasLoaEnum;
import se.litsec.eidas.opensaml.ext.SPTypeEnumeration;
import se.litsec.eidas.opensaml.ext.attributes.AttributeConstants;
import uk.gov.ida.notification.SamlFormViewBuilder;
import uk.gov.ida.notification.saml.metadata.Metadata;
import uk.gov.ida.notification.stubconnector.EidasAuthnRequestGenerator;
import uk.gov.ida.notification.stubconnector.StubConnectorConfiguration;
import uk.gov.ida.notification.stubconnector.views.StartPageView;
import uk.gov.ida.notification.views.SamlFormView;

import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Path("/")
public class SendAuthnRequestResource {
    private final StubConnectorConfiguration configuration;
    private final Metadata proxyNodeMetadata;
    private final EidasAuthnRequestGenerator authnRequestGenerator;
    private final SamlFormViewBuilder samlFormViewBuilder;

    public SendAuthnRequestResource(StubConnectorConfiguration configuration, Metadata proxyNodeMetadata, EidasAuthnRequestGenerator authnRequestGenerator, SamlFormViewBuilder samlFormViewBuilder) {
        this.configuration = configuration;
        this.proxyNodeMetadata = proxyNodeMetadata;
        this.authnRequestGenerator = authnRequestGenerator;
        this.samlFormViewBuilder = samlFormViewBuilder;
    }

    private AuthnRequest generateAuthnRequest(String ssoUrl) {
        String connectorEntityId = configuration.getConnectorNodeBaseUrl() + "/Metadata";

        List<String> requestedAttributes = Arrays.asList(
                AttributeConstants.EIDAS_PERSON_IDENTIFIER_ATTRIBUTE_NAME,
                AttributeConstants.EIDAS_CURRENT_FAMILY_NAME_ATTRIBUTE_NAME,
                AttributeConstants.EIDAS_CURRENT_GIVEN_NAME_ATTRIBUTE_NAME,
                AttributeConstants.EIDAS_DATE_OF_BIRTH_ATTRIBUTE_NAME
        );

         return authnRequestGenerator.generate(
                 "_" + UUID.randomUUID().toString(),
                 ssoUrl,
                 connectorEntityId,
                 SPTypeEnumeration.PUBLIC,
                 requestedAttributes,
                 EidasLoaEnum.LOA_SUBSTANTIAL
        );
    }

    @GET
    @Path("/")
    public View startPage() {
        return new StartPageView();
    }

    @GET
    @Path("/BadRequest")
    public SamlFormView setupBadAuthnRequest(@Session HttpSession session) throws ResolverException {
        String proxyNodeEntityId = configuration.getProxyNodeMetadataConfiguration().getExpectedEntityId();
        String ssoUrl = proxyNodeMetadata.getSsoUrl(proxyNodeEntityId);

        AuthnRequest authnRequest = generateAuthnRequest(ssoUrl);
        session.setAttribute("authn_id", authnRequest.getID());

        authnRequest.setSignature(null);

        return samlFormViewBuilder.buildRequest(ssoUrl, authnRequest, "Submit to Proxy Node", "relay");
    }

    @GET
    @Path("/Request")
    public SamlFormView setupAuthnRequest(@Session HttpSession session) throws ResolverException {
        String proxyNodeEntityId = configuration.getProxyNodeMetadataConfiguration().getExpectedEntityId();
        String ssoUrl = proxyNodeMetadata.getSsoUrl(proxyNodeEntityId);

        AuthnRequest authnRequest = generateAuthnRequest(ssoUrl);
        session.setAttribute("authn_id", authnRequest.getID());

        return samlFormViewBuilder.buildRequest(ssoUrl, authnRequest, "Submit to Proxy Node", "relay");
    }
}
