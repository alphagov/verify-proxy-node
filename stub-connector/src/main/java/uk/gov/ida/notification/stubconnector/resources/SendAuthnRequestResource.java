package uk.gov.ida.notification.stubconnector.resources;

import io.dropwizard.jersey.sessions.Session;
import io.dropwizard.views.View;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.resolver.ResolverException;
import net.shibboleth.utilities.java.support.velocity.VelocityEngine;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.encoder.MessageEncodingException;
import org.opensaml.messaging.handler.MessageHandlerException;
import org.opensaml.saml.common.binding.SAMLBindingSupport;
import org.opensaml.saml.common.messaging.context.SAMLMessageInfoContext;
import org.opensaml.saml.saml2.binding.encoding.impl.HTTPPostEncoder;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.metadata.Endpoint;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.xmlsec.SignatureSigningParameters;
import se.litsec.eidas.opensaml.common.EidasLoaEnum;
import se.litsec.eidas.opensaml.ext.SPTypeEnumeration;
import se.litsec.eidas.opensaml.ext.attributes.AttributeConstants;
import uk.gov.ida.common.shared.configuration.EncodedPrivateKeyConfiguration;
import uk.gov.ida.common.shared.configuration.X509CertificateConfiguration;
import uk.gov.ida.notification.configuration.CredentialConfiguration;
import uk.gov.ida.notification.configuration.KeyFileCredentialConfiguration;
import uk.gov.ida.notification.saml.SignatureSigningParametersHelper;
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

import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_PRIVATE_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_PUBLIC_CERT;


@Path("/")
public class SendAuthnRequestResource {
    private final StubConnectorConfiguration configuration;
    private final Metadata proxyNodeMetadata;
    private final EidasAuthnRequestContextFactory contextFactory;

    public SendAuthnRequestResource(StubConnectorConfiguration configuration, Metadata proxyNodeMetadata) {
        this.configuration = configuration;
        this.proxyNodeMetadata = proxyNodeMetadata;
        this.contextFactory = new EidasAuthnRequestContextFactory();
    }

    @GET
    @Path("/")
    public View startPage() {
        return new StartPageView();
    }

    @GET
    @Path("/RequestLow")
    public Response authnRequestLow(
            @Session HttpSession session,
            @Context HttpServletResponse httpServletResponse
    ) throws ResolverException, ComponentInitializationException, MessageHandlerException, MessageEncodingException {
        MessageContext context = generateAuthnRequestContext(session, EidasLoaEnum.LOA_LOW, configuration.getCredentialConfiguration());
        encode(httpServletResponse, context);
        return Response.ok().build();
    }

    @GET
    @Path("/RequestSubstantial")
    public Response authnRequestSubstantial(
            @Session HttpSession session,
            @Context HttpServletResponse httpServletResponse
    ) throws ResolverException, ComponentInitializationException, MessageHandlerException, MessageEncodingException {
        MessageContext context = generateAuthnRequestContext(session, EidasLoaEnum.LOA_SUBSTANTIAL, configuration.getCredentialConfiguration());
        encode(httpServletResponse, context);
        return Response.ok().build();
    }

    @GET
    @Path("/RequestHigh")
    public Response authnRequestHigh(
            @Session HttpSession session,
            @Context HttpServletResponse httpServletResponse
    ) throws ResolverException, ComponentInitializationException, MessageHandlerException, MessageEncodingException {
        MessageContext context = generateAuthnRequestContext(session, EidasLoaEnum.LOA_HIGH, configuration.getCredentialConfiguration());
        encode(httpServletResponse, context);
        return Response.ok().build();
    }

    @GET
    @Path("/MissingSignature")
    public Response invalidAuthnRequest(
        @Session HttpSession session,
        @Context HttpServletResponse httpServletResponse
    ) throws ResolverException, ComponentInitializationException, MessageHandlerException, MessageEncodingException {
        MessageContext context = generateAuthnRequestContext(session, EidasLoaEnum.LOA_SUBSTANTIAL, configuration.getCredentialConfiguration());

        AuthnRequest authnRequest = (AuthnRequest) context.getMessage();
        authnRequest.setSignature(null);
        context.setMessage(authnRequest);

        encode(httpServletResponse, context);
        return Response.ok().build();
    }

    @GET
    @Path("/InvalidSignature")
    public Response invalidSignature(
        @Session HttpSession session,
        @Context HttpServletResponse httpServletResponse
    ) throws Throwable {
        KeyFileCredentialConfiguration invalidCredentialConfiguration = new KeyFileCredentialConfiguration(
                new X509CertificateConfiguration(TEST_PUBLIC_CERT),
                new EncodedPrivateKeyConfiguration(TEST_PRIVATE_KEY)
        );
        MessageContext context = generateAuthnRequestContext(session, EidasLoaEnum.LOA_SUBSTANTIAL, invalidCredentialConfiguration);
        encode(httpServletResponse, context);
        return Response.ok().build();
    }

    @GET
    @Path("/test1") // Same as above but does not call pn gateway
    public String test1(
            @Session HttpSession session,
            @Context HttpServletResponse httpServletResponse
    ) throws Throwable {
        KeyFileCredentialConfiguration invalidCredentialConfiguration = new KeyFileCredentialConfiguration(
                new X509CertificateConfiguration(TEST_PUBLIC_CERT),
                new EncodedPrivateKeyConfiguration(TEST_PRIVATE_KEY)
        );
        MessageContext context = generateAuthnRequestContext(session, EidasLoaEnum.LOA_SUBSTANTIAL, invalidCredentialConfiguration);

        return "test1";
    }

    @GET
    @Path("/test2") // Same as above but does not call generateAuthnRequestContext
    public String test2(
            @Session HttpSession session,
            @Context HttpServletResponse httpServletResponse
    ) throws Throwable {
        KeyFileCredentialConfiguration invalidCredentialConfiguration = new KeyFileCredentialConfiguration(
                new X509CertificateConfiguration(TEST_PUBLIC_CERT),
                new EncodedPrivateKeyConfiguration(TEST_PRIVATE_KEY)
        );

        return "test2";
    }

    private MessageContext generateAuthnRequestContext(
        HttpSession session,
        EidasLoaEnum loaType,
        CredentialConfiguration credentialConfiguration
    ) throws ResolverException, ComponentInitializationException, MessageHandlerException {
        String proxyNodeEntityId = configuration.getProxyNodeMetadataConfiguration().getExpectedEntityId();
        String connectorEntityId = configuration.getConnectorNodeEntityId().toString();
        Endpoint proxyNodeEndpoint = proxyNodeMetadata.getEndpoint(proxyNodeEntityId, IDPSSODescriptor.DEFAULT_ELEMENT_NAME);

        List<String> requestedAttributes = Arrays.asList(
            AttributeConstants.EIDAS_PERSON_IDENTIFIER_ATTRIBUTE_NAME,
            AttributeConstants.EIDAS_CURRENT_FAMILY_NAME_ATTRIBUTE_NAME,
            AttributeConstants.EIDAS_CURRENT_GIVEN_NAME_ATTRIBUTE_NAME,
            AttributeConstants.EIDAS_DATE_OF_BIRTH_ATTRIBUTE_NAME
        );

        SignatureSigningParameters signingParameters = SignatureSigningParametersHelper.build(
            credentialConfiguration.getCredential(),
            credentialConfiguration.getAlgorithm());

        MessageContext context = contextFactory.generate(
            proxyNodeEndpoint,
            connectorEntityId,
            SPTypeEnumeration.PUBLIC,
            requestedAttributes,
            loaType,
            signingParameters);

        session.setAttribute("authn_id", context.getSubcontext(SAMLMessageInfoContext.class, true).getMessageId());
        SAMLBindingSupport.setRelayState(context, session.getId());
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
