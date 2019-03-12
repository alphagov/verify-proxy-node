package uk.gov.ida.notification.stubconnector.resources;

import io.dropwizard.jersey.sessions.Session;
import io.dropwizard.views.View;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.resolver.ResolverException;
import net.shibboleth.utilities.java.support.velocity.VelocityEngine;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.encoder.MessageEncodingException;
import org.opensaml.messaging.handler.MessageHandlerException;
import org.opensaml.saml.common.messaging.context.SAMLMessageInfoContext;
import org.opensaml.saml.saml2.binding.encoding.impl.HTTPPostEncoder;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.metadata.Endpoint;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.security.x509.X509Credential;
import se.litsec.eidas.opensaml.common.EidasLoaEnum;
import se.litsec.eidas.opensaml.ext.SPTypeEnumeration;
import se.litsec.eidas.opensaml.ext.attributes.AttributeConstants;
import uk.gov.ida.notification.saml.metadata.Metadata;
import uk.gov.ida.notification.stubconnector.EidasAuthnRequestContextFactory;
import uk.gov.ida.notification.stubconnector.StubConnectorConfiguration;
import uk.gov.ida.notification.stubconnector.views.StartPageView;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;

@Path("/")
public class SendAuthnRequestResource {
    private final StubConnectorConfiguration configuration;
    private final Metadata proxyNodeMetadata;
    private final EidasAuthnRequestContextFactory contextFactory;
    private final X509Credential signingCredential;

    public SendAuthnRequestResource(StubConnectorConfiguration configuration, Metadata proxyNodeMetadata, X509Credential signingCredential) {
        this.configuration = configuration;
        this.proxyNodeMetadata = proxyNodeMetadata;
        this.signingCredential = signingCredential;
        this.contextFactory = new EidasAuthnRequestContextFactory();
    }

    @GET
    @Path("/")
    public View startPage() {
        return new StartPageView();
    }

    @GET
    @Path("/Request")
    public Response authnRequest(
        @Session HttpSession session,
        @Context HttpServletResponse httpServletResponse
    ) throws ResolverException, ComponentInitializationException, MessageHandlerException, MessageEncodingException {
        MessageContext context = generateAuthnRequestContext(session);
        encode(httpServletResponse, context);
        return Response.ok().build();
    }

    @GET
    @Path("/BadRequest")
    public Response invalidAuthnRequest(
        @Session HttpSession session,
        @Context HttpServletResponse httpServletResponse
    ) throws ResolverException, ComponentInitializationException, MessageHandlerException, MessageEncodingException {
        MessageContext context = generateAuthnRequestContext(session);

        AuthnRequest authenRequest = (AuthnRequest) context.getMessage();
        authenRequest.setSignature(null);
        context.setMessage(authenRequest);

        encode(httpServletResponse, context);
        return Response.ok().build();
    }

    private MessageContext generateAuthnRequestContext(HttpSession session) throws ResolverException, ComponentInitializationException, MessageHandlerException {
        String proxyNodeEntityId = configuration.getProxyNodeMetadataConfiguration().getExpectedEntityId();
        String connectorEntityId = configuration.getConnectorNodeBaseUrl() + "/Metadata";
        Endpoint proxyNodeEndpoint = proxyNodeMetadata.getEndpoint(proxyNodeEntityId, IDPSSODescriptor.DEFAULT_ELEMENT_NAME);

        List<String> requestedAttributes = Arrays.asList(
            AttributeConstants.EIDAS_PERSON_IDENTIFIER_ATTRIBUTE_NAME,
            AttributeConstants.EIDAS_CURRENT_FAMILY_NAME_ATTRIBUTE_NAME,
            AttributeConstants.EIDAS_CURRENT_GIVEN_NAME_ATTRIBUTE_NAME,
            AttributeConstants.EIDAS_DATE_OF_BIRTH_ATTRIBUTE_NAME
        );

        MessageContext context = contextFactory.generate(
            proxyNodeEndpoint,
            connectorEntityId,
            SPTypeEnumeration.PUBLIC,
            requestedAttributes,
            EidasLoaEnum.LOA_SUBSTANTIAL,
            signingCredential);

        session.setAttribute("authn_id", context.getSubcontext(SAMLMessageInfoContext.class, true).getMessageId());

        return context;
    }

    private void encode(@Context HttpServletResponse httpServletResponse, MessageContext context) throws ComponentInitializationException, MessageEncodingException {
        HTTPPostEncoder encoder = new HTTPPostEncoder();
        encoder.setVelocityEngine(VelocityEngine.newVelocityEngine());
        encoder.setMessageContext(context);
        encoder.setHttpServletResponse(httpServletResponse);
        encoder.initialize();
        encoder.encode();
    }
}
