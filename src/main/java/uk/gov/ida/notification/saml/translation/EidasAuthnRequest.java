package uk.gov.ida.notification.saml.translation;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.saml2.core.AuthnRequest;
import se.litsec.eidas.opensaml.ext.RequestedAttribute;
import se.litsec.eidas.opensaml.ext.RequestedAttributes;
import se.litsec.eidas.opensaml.ext.SPType;
import se.litsec.eidas.opensaml.ext.SPTypeEnumeration;
import se.litsec.eidas.opensaml.ext.impl.RequestedAttributesImpl;
import se.litsec.eidas.opensaml.ext.impl.SPTypeImpl;
import uk.gov.ida.notification.exceptions.EidasAuthnRequestException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class EidasAuthnRequest {
    private final String requestId;
    private final String issuer;
    private final String destination;
    private final SPTypeEnumeration spType;
    private final String requestedLoa;
    private final List<RequestedAttribute> requestedAttributes;

    public EidasAuthnRequest(String requestId, String issuer, String destination, SPTypeEnumeration spType, String requestedLoa, List<RequestedAttribute> requestedAttributes) {
        this.requestId = requestId;
        this.issuer = issuer;
        this.destination = destination;
        this.spType = spType;
        this.requestedLoa = requestedLoa;
        this.requestedAttributes = requestedAttributes;
    }

    public static EidasAuthnRequest buildFromAuthnRequest(AuthnRequest request)  {
        String requestId = request.getID();
        String issuer = request.getIssuer().getValue();
        String destination = request.getDestination();
        String requestedLoa = request.getRequestedAuthnContext().getAuthnContextClassRefs().get(0).getAuthnContextClassRef();

        XMLObject spTypeObj = request.getExtensions().getOrderedChildren()
                .stream()
                .filter(obj -> obj  instanceof  SPTypeImpl)
                .findFirst()
                .orElseThrow(() -> new EidasAuthnRequestException("eIDAS AuthnRequest has no SPType"));

        SPTypeEnumeration spType = ((SPType) spTypeObj).getType();

        Optional<XMLObject> requestedAttributesObj = request.getExtensions().getOrderedChildren()
                .stream()
                .filter(obj -> obj.getClass() == RequestedAttributesImpl.class)
                .findFirst();

        List<RequestedAttribute> requestedAttributes = requestedAttributesObj
                .map(obj -> ((RequestedAttributes) obj).getRequestedAttributes())
                .orElse(Collections.emptyList());

        return new EidasAuthnRequest(requestId, issuer, destination, spType, requestedLoa, requestedAttributes);
    }

    public String getRequestId() {
        return requestId;
    }

    public String getIssuer() {
        return issuer;
    }

    public String getDestination() {
        return destination;
    }

    public SPTypeEnumeration getSpType() {
        return spType;
    }

    public String getRequestedLoa() {
        return requestedLoa;
    }

    public List<RequestedAttribute> getRequestedAttributes() {
        return requestedAttributes;
    }
}
