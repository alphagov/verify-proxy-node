package uk.gov.ida.notification.stubconnector.resources;

import net.shibboleth.utilities.java.support.resolver.ResolverException;
import org.opensaml.saml.saml2.core.AuthnRequest;
import se.litsec.eidas.opensaml.common.EidasLoaEnum;
import se.litsec.eidas.opensaml.ext.SPTypeEnumeration;
import se.litsec.eidas.opensaml.ext.attributes.AttributeConstants;
import uk.gov.ida.notification.SamlFormViewBuilder;
import uk.gov.ida.notification.saml.metadata.Metadata;
import uk.gov.ida.notification.stubconnector.EidasAuthnRequestGenerator;
import uk.gov.ida.notification.stubconnector.StubConnectorConfiguration;
import uk.gov.ida.notification.views.SamlFormView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import java.util.Arrays;
import java.util.UUID;
import java.util.List;


@Path("/Request")
public class SendAuthnRequestResource {
    private final StubConnectorConfiguration configuration;
    private final Metadata proxyNodeMetadata;
    private final EidasAuthnRequestGenerator authnRequestGenerator;
    private final SamlFormViewBuilder samlFormViewBuilder;

    @Context
    HttpServletRequest request;

    public SendAuthnRequestResource(StubConnectorConfiguration configuration, Metadata proxyNodeMetadata, EidasAuthnRequestGenerator authnRequestGenerator, SamlFormViewBuilder samlFormViewBuilder) {
        this.configuration = configuration;
        this.proxyNodeMetadata = proxyNodeMetadata;
        this.authnRequestGenerator = authnRequestGenerator;
        this.samlFormViewBuilder = samlFormViewBuilder;
    }

    @GET
    public SamlFormView setupAuthnRequest(ContainerRequestContext requestContext) throws ResolverException {
        assert requestContext != null && request != null;

        HttpSession session = request.getSession(true);

        String proxyNodeEntityId = configuration.getProxyNodeMetadataConfiguration().getExpectedEntityId();
        String ssoUrl = proxyNodeMetadata.getSsoUrl(proxyNodeEntityId);
        String connectorEntityId = configuration.getConnectorNodeBaseUrl() + "/Metadata";

        List<String> requestedAttributes = Arrays.asList(
                AttributeConstants.EIDAS_PERSON_IDENTIFIER_ATTRIBUTE_NAME,
                AttributeConstants.EIDAS_CURRENT_FAMILY_NAME_ATTRIBUTE_NAME,
                AttributeConstants.EIDAS_CURRENT_GIVEN_NAME_ATTRIBUTE_NAME,
                AttributeConstants.EIDAS_DATE_OF_BIRTH_ATTRIBUTE_NAME
        );

        String authnRequestId = UUID.randomUUID().toString();

        session.setAttribute("authn_id", authnRequestId);

        AuthnRequest authnRequest = authnRequestGenerator.generate(
                authnRequestId,
                ssoUrl,
                connectorEntityId,
                SPTypeEnumeration.PUBLIC,
                requestedAttributes,
                EidasLoaEnum.LOA_SUBSTANTIAL
        );

        return samlFormViewBuilder.buildRequest(ssoUrl, authnRequest, "Submit to Proxy Node", "relay");
    }
}
