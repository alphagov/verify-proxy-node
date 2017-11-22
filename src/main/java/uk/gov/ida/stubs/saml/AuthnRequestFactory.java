package uk.gov.ida.stubs.saml;

import net.shibboleth.utilities.java.support.security.SecureRandomIdentifierGenerationStrategy;
import org.joda.time.DateTime;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnContextComparisonTypeEnumeration;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Extensions;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.NameIDPolicy;
import org.opensaml.saml.saml2.core.NameIDType;
import org.opensaml.saml.saml2.core.RequestedAuthnContext;
import se.litsec.eidas.opensaml.common.EidasConstants;
import se.litsec.eidas.opensaml.ext.RequestedAttribute;
import se.litsec.eidas.opensaml.ext.RequestedAttributes;
import se.litsec.eidas.opensaml.ext.SPType;
import se.litsec.eidas.opensaml.ext.SPTypeEnumeration;
import se.litsec.eidas.opensaml.ext.attributes.AttributeConstants;

public class AuthnRequestFactory {
    private final SecureRandomIdentifierGenerationStrategy strategy = new SecureRandomIdentifierGenerationStrategy();

    public AuthnRequest createEidasAuthnRequest(String issuerEntityId, String destination, DateTime issueInstant) {
        AuthnRequest authnRequest = (AuthnRequest) XMLObjectSupport.buildXMLObject(AuthnRequest.DEFAULT_ELEMENT_NAME);
        authnRequest.setID(strategy.generateIdentifier());
        authnRequest.setDestination(destination);
        authnRequest.setIssueInstant(issueInstant);

        authnRequest.setIssuer(createIssuer(issuerEntityId));
        authnRequest.setExtensions(createEidasExtensions());
        authnRequest.setNameIDPolicy(createEidasNameIDPolicy());
        authnRequest.setRequestedAuthnContext(createEidasRequestedAuthnContext());

        return authnRequest;
    }

    private RequestedAuthnContext createEidasRequestedAuthnContext() {
        RequestedAuthnContext requestedAuthnContext = (RequestedAuthnContext) XMLObjectSupport.buildXMLObject(RequestedAuthnContext.DEFAULT_ELEMENT_NAME);
        requestedAuthnContext.setComparison(AuthnContextComparisonTypeEnumeration.MINIMUM);

        AuthnContextClassRef authnContextClassRef = (AuthnContextClassRef) XMLObjectSupport.buildXMLObject(AuthnContextClassRef.DEFAULT_ELEMENT_NAME);
        authnContextClassRef.setAuthnContextClassRef(EidasConstants.EIDAS_LOA_HIGH);
        requestedAuthnContext.getAuthnContextClassRefs().add(authnContextClassRef);
        return requestedAuthnContext;
    }

    private NameIDPolicy createEidasNameIDPolicy() {
        NameIDPolicy nameIDPolicy = (NameIDPolicy) XMLObjectSupport.buildXMLObject(NameIDPolicy.DEFAULT_ELEMENT_NAME);
        nameIDPolicy.setAllowCreate(true);
        return nameIDPolicy;
    }

    private Extensions createEidasExtensions() {
        SPType spType = (SPType) XMLObjectSupport.buildXMLObject(SPType.DEFAULT_ELEMENT_NAME);
        spType.setType(SPTypeEnumeration.PUBLIC);

        RequestedAttributes requestedAttributes = (RequestedAttributes) XMLObjectSupport.buildXMLObject(RequestedAttributes.DEFAULT_ELEMENT_NAME);
        requestedAttributes.getRequestedAttributes().add(createRequestedAttribute(AttributeConstants.EIDAS_PERSON_IDENTIFIER_ATTRIBUTE_NAME));
        requestedAttributes.getRequestedAttributes().add(createRequestedAttribute(AttributeConstants.EIDAS_CURRENT_FAMILY_NAME_ATTRIBUTE_NAME));
        requestedAttributes.getRequestedAttributes().add(createRequestedAttribute(AttributeConstants.EIDAS_CURRENT_GIVEN_NAME_ATTRIBUTE_NAME));
        requestedAttributes.getRequestedAttributes().add(createRequestedAttribute(AttributeConstants.EIDAS_DATE_OF_BIRTH_ATTRIBUTE_NAME));

        Extensions extensions = (Extensions) XMLObjectSupport.buildXMLObject(Extensions.DEFAULT_ELEMENT_NAME);
        extensions.getUnknownXMLObjects().add(spType);
        extensions.getUnknownXMLObjects().add(requestedAttributes);
        return extensions;
    }

    private Issuer createIssuer(String issuerEntityId) {
        Issuer issuer = (Issuer) XMLObjectSupport.buildXMLObject(Issuer.DEFAULT_ELEMENT_NAME);
        issuer.setFormat(NameIDType.ENTITY);
        issuer.setValue(issuerEntityId);
        return issuer;
    }

    private RequestedAttribute createRequestedAttribute(String requestedAttributeName) {
        RequestedAttribute attr = (RequestedAttribute) XMLObjectSupport.buildXMLObject(RequestedAttribute.DEFAULT_ELEMENT_NAME);
        attr.setName(requestedAttributeName);
        attr.setNameFormat(Attribute.URI_REFERENCE);
        attr.setIsRequired(true);
        return attr;
    }
}
