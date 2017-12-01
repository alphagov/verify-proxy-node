package uk.gov.ida.notification;

import org.glassfish.jersey.internal.util.Base64;
import org.opensaml.saml.saml2.core.AuthnRequest;
import uk.gov.ida.notification.saml.SamlParser;
import uk.gov.ida.notification.saml.translation.EidasAuthnRequest;

import java.util.logging.Logger;

public class EidasAuthnRequestMapper {

    private SamlParser parser;
    private static final Logger LOG = Logger.getLogger(EidasAuthnRequestMapper.class.getName());


    public EidasAuthnRequestMapper(SamlParser parser) {
        this.parser = parser;
    }

    public EidasAuthnRequest map(String inputRequest) {
        String requestAsString = Base64.decodeAsString(inputRequest);
        AuthnRequest authnRequest = parser.parseSamlString(requestAsString);
        EidasAuthnRequest eidasAuthnRequest = new EidasAuthnRequest(authnRequest);
        logAuthnRequestInformation(eidasAuthnRequest);
        return eidasAuthnRequest;
    }

    private void logAuthnRequestInformation(EidasAuthnRequest eidasAuthnRequest) {
        LOG.info("[eIDAS AuthnRequest] Request ID: " + eidasAuthnRequest.getRequestId());
        LOG.info("[eIDAS AuthnRequest] Issuer: " + eidasAuthnRequest.getIssuer());
        LOG.info("[eIDAS AuthnRequest] Destination: " + eidasAuthnRequest.getDestination());
        LOG.info("[eIDAS AuthnRequest] SPType: " + eidasAuthnRequest.getSpType());
        LOG.info("[eIDAS AuthnRequest] Requested level of assurance: " + eidasAuthnRequest.getRequestedLoa());
        eidasAuthnRequest.getRequestedAttributes()
                .stream()
                .forEach((attr) -> LOG.info("[eIDAS AuthnRequest] Requested attribute: " + attr.getName()));
    }
}
