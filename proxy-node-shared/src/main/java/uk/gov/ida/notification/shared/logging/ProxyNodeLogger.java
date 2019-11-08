package uk.gov.ida.notification.shared.logging;

import org.slf4j.MDC;

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

    public static void logException(Throwable exception, String message) {
        logException(exception, Level.SEVERE, message);
    }

    public static void logException(Throwable exception, Level level, String message) {
        logInternal(level, message, exception);
    }

    private static void log(Level level, String message) {
        logInternal(level, message, null);
    }

    private static void logInternal(Level level, String message, Throwable exception) {
        if (exception != null) {
            LOG.log(level, message, exception);
        } else {
            LOG.log(level, message);
        }
    }

}
