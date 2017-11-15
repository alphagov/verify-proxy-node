package uk.gov.ida.notification.views;

import io.dropwizard.views.View;

import java.util.List;
import java.util.stream.Collectors;

public class EidasAuthnRequestView extends View{

    private final String issuer;
    private final String destination;
    private final String spType;
    private final String authnContextComparison;
    private final List<RequestedAttributesDisplay> requestedAttributes;
    private final List<AuthnContextClassRefDisplay> authnContextClassRefs;

    public static class RequestedAttributesDisplay {
        private final String requestedAttribute;
        public RequestedAttributesDisplay(String requestedAttribute) {
            this.requestedAttribute = requestedAttribute;
        }
        public String getRequestedAttribute() {
            return requestedAttribute;
        }
    }

    public static class AuthnContextClassRefDisplay {
        private final String levelOfAssurance;
        public AuthnContextClassRefDisplay(String levelOfAssurance) {
            this.levelOfAssurance = levelOfAssurance;
        }
        public String getLevelOfAssurance() {
            return levelOfAssurance;
        }
    }

    public EidasAuthnRequestView(
            String issuer,
            String destination,
            String spType,
            List<String> requestedAttributes,
            String authnContextComparison,
            List<String> authnContextClassRefs) {
        super("eidas-authn-request.mustache");
        this.issuer = issuer;
        this.destination = destination;
        this.spType = spType;
        this.authnContextComparison = authnContextComparison;
        this.requestedAttributes = requestedAttributes.stream().map(RequestedAttributesDisplay::new).collect(Collectors.toList());
        this.authnContextClassRefs = authnContextClassRefs.stream().map(AuthnContextClassRefDisplay::new).collect(Collectors.toList());
    }

    public String getIssuer() {
        return issuer;
    }

    public String getDestination() {
        return destination;
    }

    public String getSpType() {
        return spType;
    }

    public String getAuthnContextComparison() {
        return authnContextComparison;
    }

    public List<RequestedAttributesDisplay> getRequestedAttributes() {
        return requestedAttributes;
    }

    public List<AuthnContextClassRefDisplay> getAuthnContextClassRefs() {
        return authnContextClassRefs;
    }
}
