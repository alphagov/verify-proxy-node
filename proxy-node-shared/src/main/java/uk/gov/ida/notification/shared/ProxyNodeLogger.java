package uk.gov.ida.notification.shared;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.MDC;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProxyNodeLogger {

    private static final Logger LOG = Logger.getLogger(ProxyNodeLogger.class.getName());

    private ProxyNodeLogger(){}

    public static void addContext(ProxyNodeMDCKey key, String value) {
        MDC.put(key.name(), value);
    }

    public static void info(String message) {
        log(Level.INFO, message);
    }

    public static void warning(String message) {
        log(Level.WARNING, message);
    }

    public static void error(String message) {
        log(Level.SEVERE, message);
    }

    public static void logException(Exception exception, Level level, String message) {
        addExceptionContext(exception);
        log(level, message);
    }

    private static void log(Level level, String message) {
        logWithCallingFrame(level, () -> message, null);
    }

    private static void log(Level level, String message, Exception exception) {
        logWithCallingFrame(level, () -> message, exception);
    }

    private static void log(Level level, Supplier<String> message) {
        logWithCallingFrame(level, message, null);
    }

    private static void addExceptionContext(Exception exception) {
        Throwable cause = exception.getCause();
        if (cause != null) {
            addContext(ProxyNodeMDCKey.EXCEPTION_CAUSE, cause.getMessage());
        }

        addContext(ProxyNodeMDCKey.EXCEPTION_MESSAGE, exception.getMessage());
        addContext(ProxyNodeMDCKey.EXCEPTION_STACKTRACE, ExceptionUtils.getStackTrace(exception));
    }

    private static void logWithCallingFrame(Level level, Supplier<String> message, Exception exception) {
        Optional<StackWalker.StackFrame> callingFrame = getCallingStackFrame();
        callingFrame.ifPresent(f -> {
            addContext(ProxyNodeMDCKey.LOG_LOCATION, String.format("%s.%s", f.getClassName(), f.getMethodName()));
            if (exception != null) {
                LOG.log(level, message.get(), exception);
            } else {
                LOG.log(level, message);
            }
            MDC.remove(ProxyNodeMDCKey.LOG_LOCATION.name());
        });
    }

    private static Optional<StackWalker.StackFrame> getCallingStackFrame() {
        return StackWalker.getInstance().walk(
                s -> s.dropWhile(f -> f.getClassName().startsWith(ProxyNodeLogger.class.getName())).findFirst());
    }
}
