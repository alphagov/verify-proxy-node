package uk.gov.ida.notification.shared;

import org.slf4j.MDC;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProxyNodeLogger {

    private static final Logger LOG = Logger.getLogger(ProxyNodeLogger.class.getName());

    public void addContext(ProxyNodeMDCKey key, String value) {
        MDC.put(key.name(), value);
    }

    public void log(Level level, String message) {
        log(level, () -> message);
    }

    public void log(Level level, Supplier<String> message) {
        Optional<StackWalker.StackFrame> callingFrame = getCallingStackFrame();
        callingFrame.ifPresent(f -> {
            addContext(ProxyNodeMDCKey.LOG_LOCATION, String.format("%s.%s", f.getClassName(), f.getMethodName()));
            LOG.log(level, message);
            MDC.remove(ProxyNodeMDCKey.LOG_LOCATION.name());
        });
    }

    private Optional<StackWalker.StackFrame> getCallingStackFrame() {
        return StackWalker.getInstance().walk(s -> s.skip(1).findFirst());
    }
}