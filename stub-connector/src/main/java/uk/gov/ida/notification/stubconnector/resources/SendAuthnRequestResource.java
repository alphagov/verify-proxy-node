package uk.gov.ida.notification.stubconnector.resources;

import io.dropwizard.jersey.sessions.Session;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.resolver.ResolverException;
import net.shibboleth.utilities.java.support.velocity.VelocityEngine;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.encoder.MessageEncodingException;
import org.opensaml.messaging.handler.MessageHandlerException;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.binding.security.impl.SAMLOutboundProtocolMessageSigningHandler;
import org.opensaml.saml.common.messaging.context.SAMLEndpointContext;
import org.opensaml.saml.common.messaging.context.SAMLPeerEntityContext;
import org.opensaml.saml.saml2.binding.encoding.impl.HTTPPostEncoder;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.metadata.Endpoint;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.security.x509.X509Credential;
import org.opensaml.xmlsec.SignatureSigningParameters;
import org.opensaml.xmlsec.context.SecurityParametersContext;
import org.opensaml.xmlsec.signature.support.SignatureConstants;
import se.litsec.eidas.opensaml.common.EidasLoaEnum;
import se.litsec.eidas.opensaml.ext.SPTypeEnumeration;
import uk.gov.ida.notification.saml.metadata.Metadata;
import uk.gov.ida.notification.stubconnector.EidasAuthnRequestGenerator;
import uk.gov.ida.notification.stubconnector.RequestUtils;
import uk.gov.ida.notification.stubconnector.StubConnectorConfiguration;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/Request")
public class SendAuthnRequestResource {
    private final StubConnectorConfiguration configuration;
    private final Metadata proxyNodeMetadata;
    private final EidasAuthnRequestGenerator authnRequestGenerator;
    private final X509Credential signingCredential;

    public SendAuthnRequestResource(StubConnectorConfiguration configuration, Metadata proxyNodeMetadata, EidasAuthnRequestGenerator authnRequestGenerator, X509Credential signingCredential) {
        this.configuration = configuration;
        this.proxyNodeMetadata = proxyNodeMetadata;
        this.authnRequestGenerator = authnRequestGenerator;
        this.signingCredential = signingCredential;
    }

    @GET
    public Response setupAuthnRequest(
        @Session HttpSession session,
        @Context HttpServletResponse httpServletResponse
    ) throws ResolverException, ComponentInitializationException, MessageHandlerException, MessageEncodingException {
        String proxyNodeEntityId = configuration.getProxyNodeMetadataConfiguration().getExpectedEntityId();
        String connectorEntityId = configuration.getConnectorNodeBaseUrl() + "/Metadata";
        Endpoint proxyNodeEndpoint = proxyNodeMetadata.getEndpoint(proxyNodeEntityId, IDPSSODescriptor.DEFAULT_ELEMENT_NAME);
        List<String> requestedAttributes = RequestUtils.getMinimumEidasRequestedAttributes();
        String authnRequestId = RequestUtils.generateId();

        session.setAttribute("authn_id", authnRequestId);

        AuthnRequest authnRequest = authnRequestGenerator.generate(
                authnRequestId,
                proxyNodeEndpoint.getLocation(),
                connectorEntityId,
                SPTypeEnumeration.PUBLIC,
                requestedAttributes,
                EidasLoaEnum.LOA_SUBSTANTIAL
        );

        SignatureSigningParameters signatureSigningParameters = new SignatureSigningParameters();
        signatureSigningParameters.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256);
        signatureSigningParameters.setSignatureCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
        signatureSigningParameters.setSigningCredential(signingCredential);

        MessageContext<SAMLObject> context = new MessageContext<>() {{
            setMessage(authnRequest);

            getSubcontext(SAMLPeerEntityContext.class, true)
                .getSubcontext(SAMLEndpointContext.class, true)
                    .setEndpoint(proxyNodeEndpoint);

            getSubcontext(SecurityParametersContext.class, true)
                .setSignatureSigningParameters(signatureSigningParameters);
        }};

        SAMLOutboundProtocolMessageSigningHandler signingHandler = new SAMLOutboundProtocolMessageSigningHandler();
        signingHandler.initialize();
        signingHandler.invoke(context);

        HTTPPostEncoder encoder = new HTTPPostEncoder();
        encoder.setMessageContext(context);
        encoder.setHttpServletResponse(httpServletResponse);
        encoder.setVelocityEngine(VelocityEngine.newVelocityEngine());
        encoder.initialize();
        encoder.encode();

        return Response.ok().build();
    }
}
