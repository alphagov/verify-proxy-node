package uk.gov.ida.notification.eidassaml.logging;

import org.opensaml.saml.saml2.core.AuthnRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class EidasAuthnRequestAttributesLogger {
    private static final Logger log = LoggerFactory.getLogger(EidasAuthnRequestAttributesLogger.class);

    public static void logAuthnRequestAttributes(AuthnRequest authnRequest) {
        try {
            MDC.put("eidasRequestId", authnRequest.getID());
            MDC.put("eidasDestination", authnRequest.getDestination());
            MDC.put("eidasIssueInstant", authnRequest.getIssueInstant().toString());
            MDC.put("eidasIssuer", authnRequest.getIssuer().getValue());
            log.info("Authn request validated by ESP");
        } catch (Exception e) {
            MDC.clear();
            throw e;
        }
        MDC.clear();
    }
}
