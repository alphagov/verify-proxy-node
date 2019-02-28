package uk.gov.ida.notification.logging;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import uk.gov.ida.notification.contracts.EidasSamlParserResponse;
import uk.gov.ida.notification.contracts.verifyserviceprovider.AuthnRequestResponse;

import javax.servlet.http.HttpSession;

public class CombinedAuthnRequestAttributesLogger {
    private static final Logger log = LoggerFactory.getLogger(CombinedAuthnRequestAttributesLogger.class);

    public static void logAuthnRequestAttributes(
        String sessionId,
        EidasSamlParserResponse eidasSamlParserResponse,
        AuthnRequestResponse vspResponse
    ) {
        try {
            MDC.put("sessionId", sessionId);
            MDC.put("eidasRequestId", eidasSamlParserResponse.getRequestId());
            MDC.put("eidasIssuer", eidasSamlParserResponse.getIssuer());
            MDC.put("eidasDestination", eidasSamlParserResponse.getDestination());
            MDC.put(
                "eidasConnectorPublicKeySuffix",
                StringUtils.right(
                    eidasSamlParserResponse.getConnectorEncryptionPublicCertificate(),
                    10
                )
            );
            MDC.put("hubRequestId", vspResponse.getRequestId());
            MDC.put("hubUrl", vspResponse.getSsoLocation().toString());
            log.info("Authn requests received from ESP and VSP");
        } catch (Exception e) {
            throw e;
        } finally {
            MDC.clear();
        }

    }
}
