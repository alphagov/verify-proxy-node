package uk.gov.ida.notification.stubconnector;

import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.handler.AbstractMessageHandler;
import org.opensaml.messaging.handler.MessageHandlerException;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.StatusCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Remove unencrypted Assertion elements from a SAML Response.
 */
public class StripAssertionsHandler extends AbstractMessageHandler {
    /** Logger. */
    @Nonnull
    private Logger log = LoggerFactory.getLogger(StripAssertionsHandler.class);

    @Override
    protected void doInvoke(@Nonnull MessageContext messageContext) throws MessageHandlerException {
        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);

        if (messageContext.getMessage() instanceof SAMLObject) {
            SAMLObject message = (SAMLObject) messageContext.getMessage();
            if (message instanceof Response) {
                List<Assertion> assertions = ((Response) message).getAssertions();

                if (!assertions.isEmpty()) {
                    log.debug("Removed {} Assertion elements from Response", assertions.size());
                    assertions.clear();
                }
            } else {
                throw new MessageHandlerException("SAML message is not a Response");
            }
        }
    }
}
