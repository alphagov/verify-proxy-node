package uk.gov.ida.notification.stubconnector;

import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.handler.AbstractMessageHandler;
import org.opensaml.messaging.handler.MessageHandlerException;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.StatusCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Check for a fixed number of EncryptedAssertion elements in a SAML Response.
 */
public class EncryptedAssertionCountValidationHandler extends AbstractMessageHandler {
    /** Logger. */
    @Nonnull
    private Logger log = LoggerFactory.getLogger(EncryptedAssertionCountValidationHandler.class);

    @Nonnull
    private long encryptedAssertionCount;

    public EncryptedAssertionCountValidationHandler() {
        this.encryptedAssertionCount = 1;
    }

    @Override
    protected void doInvoke(@Nonnull MessageContext messageContext) throws MessageHandlerException {
        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);

        if (messageContext.getMessage() instanceof SAMLObject) {
            SAMLObject message = (SAMLObject) messageContext.getMessage();
            if (message instanceof Response) {
                String statusCode = ((Response) message).getStatus().getStatusCode().getValue();
                List<EncryptedAssertion> encryptedAssertions = ((Response) message).getEncryptedAssertions();
                if (StatusCode.SUCCESS.equals(statusCode)) {
                    if (encryptedAssertions.size() != encryptedAssertionCount)
                        log.warn("Number of EncryptedAssertions {} != encryptedAssertionCount {}",
                            encryptedAssertions.size(), encryptedAssertionCount);
                        throw new MessageHandlerException("Number of EncryptedAssertions does not match encryptedAssertionCount");
                } else {
                    if (!encryptedAssertions.isEmpty())
                        throw new MessageHandlerException("Non success Response contained EncryptedAssertions");
                }
            } else {
                throw new MessageHandlerException("SAML message is not a Response");
            }
        }
    }

    public long getEncryptedAssertionCount() {
        return encryptedAssertionCount;
    }

    public void setEncryptedAssertionCount(int encryptedAssertionCount) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        this.encryptedAssertionCount = Constraint.isGreaterThanOrEqual(1, encryptedAssertionCount, "encryptedAssertionCount must be >= 1");
    }
}
