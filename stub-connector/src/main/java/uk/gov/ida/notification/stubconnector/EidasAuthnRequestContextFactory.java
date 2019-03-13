package uk.gov.ida.notification.stubconnector;

import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.security.SecureRandomIdentifierGenerationStrategy;
import org.joda.time.DateTime;
import org.opensaml.core.xml.Namespace;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.handler.MessageHandlerException;
import org.opensaml.saml.common.binding.impl.SAMLOutboundDestinationHandler;
import org.opensaml.saml.common.binding.security.impl.SAMLOutboundProtocolMessageSigningHandler;
import org.opensaml.saml.common.messaging.context.SAMLEndpointContext;
import org.opensaml.saml.common.messaging.context.SAMLPeerEntityContext;
import org.opensaml.saml.common.messaging.context.SAMLSelfEntityContext;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnContextComparisonTypeEnumeration;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Extensions;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.NameIDPolicy;
import org.opensaml.saml.saml2.core.NameIDType;
import org.opensaml.saml.saml2.core.RequestedAuthnContext;
import org.opensaml.saml.saml2.metadata.Endpoint;
import org.opensaml.xmlsec.SignatureSigningParameters;
import org.opensaml.xmlsec.context.SecurityParametersContext;
import se.litsec.eidas.opensaml.common.EidasConstants;
import se.litsec.eidas.opensaml.common.EidasLoaEnum;
import se.litsec.eidas.opensaml.ext.RequestedAttribute;
import se.litsec.eidas.opensaml.ext.RequestedAttributes;
import se.litsec.eidas.opensaml.ext.SPType;
import se.litsec.eidas.opensaml.ext.SPTypeEnumeration;
import uk.gov.ida.notification.saml.SamlBuilder;

import java.util.List;

public class EidasAuthnRequestContextFactory {
    private final
    SecureRandomIdentifierGenerationStrategy identifierGenerationStrategy = new SecureRandomIdentifierGenerationStrategy();

    public MessageContext generate(
        Endpoint destinationEndpoint,
        String connectorEntityId,
        SPTypeEnumeration spType,
        List<String> requestedAttributes,
        EidasLoaEnum loa,
        SignatureSigningParameters signingParameters) throws ComponentInitializationException, MessageHandlerException {

        AuthnRequest request = SamlBuilder.build(AuthnRequest.DEFAULT_ELEMENT_NAME);
        request.getNamespaceManager().registerNamespaceDeclaration(new Namespace(EidasConstants.EIDAS_NS, EidasConstants.EIDAS_PREFIX));

        // Add the request attributes.
        //
        request.setForceAuthn(true);
        request.setID(identifierGenerationStrategy.generateIdentifier(true));
        request.setIssueInstant(new DateTime());

        // Add the issuer element (the entity that issues this request).
        //
        Issuer issuer = SamlBuilder.build(Issuer.DEFAULT_ELEMENT_NAME);
        issuer.setFormat(NameIDType.ENTITY);
        issuer.setValue(connectorEntityId);
        request.setIssuer(issuer);
        request.setDestination(destinationEndpoint.getLocation());

        Extensions extensions = SamlBuilder.build(Extensions.DEFAULT_ELEMENT_NAME);

        // Add the type of SP as an extension.
        //
        SPType spTypeElement = SamlBuilder.build(SPType.DEFAULT_ELEMENT_NAME);
        spTypeElement.setType(spType);
        extensions.getUnknownXMLObjects().add(spTypeElement);

        // Add the eIDAS requested attributes as an extension.
        //
        if (requestedAttributes != null && !requestedAttributes.isEmpty()) {
            RequestedAttributes requestedAttributesElement = SamlBuilder.build(RequestedAttributes.DEFAULT_ELEMENT_NAME);

            // Also see the RequestedAttributeTemplates class ...

            for (String attr : requestedAttributes) {
                RequestedAttribute reqAttr = SamlBuilder.build(RequestedAttribute.DEFAULT_ELEMENT_NAME);
                reqAttr.setName(attr);
                reqAttr.setNameFormat(Attribute.URI_REFERENCE);
                reqAttr.setIsRequired(true);
                requestedAttributesElement.getRequestedAttributes().add(reqAttr);
            }
            extensions.getUnknownXMLObjects().add(requestedAttributesElement);
        }
        request.setExtensions(extensions);

        // Set the requested NameID policy to "persistent".
        //
        NameIDPolicy nameIDPolicy = SamlBuilder.build(NameIDPolicy.DEFAULT_ELEMENT_NAME);
        nameIDPolicy.setFormat(NameID.PERSISTENT);
        nameIDPolicy.setAllowCreate(true);
        request.setNameIDPolicy(nameIDPolicy);

        // Create the requested authentication context and assign the "level of assurance" that we require
        // the authentication to be performed under.
        //
        RequestedAuthnContext requestedAuthnContext = SamlBuilder.build(RequestedAuthnContext.DEFAULT_ELEMENT_NAME);
        requestedAuthnContext.setComparison(AuthnContextComparisonTypeEnumeration.MINIMUM); // Should be exact!
        AuthnContextClassRef authnContextClassRef = SamlBuilder.build(AuthnContextClassRef.DEFAULT_ELEMENT_NAME);
        authnContextClassRef.setAuthnContextClassRef(loa.getUri());
        requestedAuthnContext.getAuthnContextClassRefs().add(authnContextClassRef);
        request.setRequestedAuthnContext(requestedAuthnContext);

        MessageContext context = new MessageContext() {{
            setMessage(request);

            getSubcontext(SAMLPeerEntityContext.class, true)
                .getSubcontext(SAMLEndpointContext.class, true)
                .setEndpoint(destinationEndpoint);

            getSubcontext(SAMLSelfEntityContext.class, true)
                .setEntityId(connectorEntityId);

            getSubcontext(SecurityParametersContext.class, true)
                .setSignatureSigningParameters(signingParameters);
        }};

        SAMLOutboundProtocolMessageSigningHandler signingHandler = new SAMLOutboundProtocolMessageSigningHandler();
        signingHandler.initialize();
        signingHandler.invoke(context);

        SAMLOutboundDestinationHandler destinationHandler = new SAMLOutboundDestinationHandler();
        destinationHandler.initialize();
        signingHandler.invoke(context);

        return context;
    }
}
