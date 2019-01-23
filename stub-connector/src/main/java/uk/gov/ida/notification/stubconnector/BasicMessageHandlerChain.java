package uk.gov.ida.notification.stubconnector;

import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.handler.MessageHandler;
import org.opensaml.messaging.handler.MessageHandlerChain;
import org.opensaml.messaging.handler.MessageHandlerException;

import javax.annotation.Nonnull;
import java.util.List;

public class BasicMessageHandlerChain<MessageType> implements MessageHandlerChain<MessageType> {
    private final List<MessageHandler<MessageType>> handlers;

    public BasicMessageHandlerChain(List<MessageHandler<MessageType>> handlers) {
        this.handlers = handlers;
    }

    @Override
    public boolean isInitialized() {
        return handlers.stream().allMatch(MessageHandler<MessageType>::isInitialized);
    }

    @Override
    public void initialize() throws ComponentInitializationException {
        for (MessageHandler<MessageType> handler : handlers) handler.initialize();
    }

    @Override
    public void invoke(@Nonnull MessageContext<MessageType> messageContext) throws MessageHandlerException {
        for (MessageHandler<MessageType> handler : handlers) handler.invoke(messageContext);
    }

    @Override
    public List<MessageHandler<MessageType>> getHandlers() {
        return handlers;
    }
}
