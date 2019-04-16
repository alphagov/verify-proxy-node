package uk.gov.ida.notification.shared;

import org.slf4j.MDC;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProxyNodeLogger {

    private static final Logger LOG = Logger.getLogger(ProxyNodeLogger.class.getName());
    private static final int FRAMES_IN_THIS_CLASS = 2;

    public void addContext(ProxyNodeMDCKey key, String value) {
        MDC.put(key.name(), value);
    }

    public void log(Level level, String message) {
        Optional<StackWalker.StackFrame> callingFrame = getCallingStackFrame();
        logPrivate(level, () -> message, callingFrame);
    }

    public void log(Level level, Supplier<String> message) {
        Optional<StackWalker.StackFrame> callingFrame = getCallingStackFrame();
        logPrivate(level, message, callingFrame);
    }

    private void logPrivate(Level level, Supplier<String> message, Optional<StackWalker.StackFrame> callingFrame) {
        callingFrame.ifPresent(f -> {
            addContext(ProxyNodeMDCKey.LOG_LOCATION, String.format("%s.%s", f.getClassName(), f.getMethodName()));
            LOG.log(level, message);
            MDC.remove(ProxyNodeMDCKey.LOG_LOCATION.name());
        });
    }

    private Optional<StackWalker.StackFrame> getCallingStackFrame() {
        return StackWalker.getInstance().walk(s -> s.skip(FRAMES_IN_THIS_CLASS).findFirst());
    }

}
