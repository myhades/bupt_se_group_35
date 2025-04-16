package org.group35.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.group35.config.Settings;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import ch.qos.logback.classic.PatternLayout;

/**
 * LoggerHelper provides methods to configure logging based on application settings
 * and static convenience methods for logging at various levels.
 */
public class LoggerHelper {
    // SLF4J logger for LoggerHelper class
    private static final Logger logger = LoggerFactory.getLogger(LoggerHelper.class);

    /**
     * Configures the root logger level and optionally adds file logging based on the provided settings.
     *
     * @param settings the Settings instance containing configuration such as log level and file logging options.
     */
    public static void configureLogLevel(Settings settings) {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        // Use fully qualified name to avoid conflict with SLF4J Logger.
        ch.qos.logback.classic.Logger rootLogger =
                (ch.qos.logback.classic.Logger) loggerContext.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        // Set the root logger level based on settings, defaulting to DEBUG if the value is invalid.
        rootLogger.setLevel(Level.toLevel(settings.getLogLevel(), Level.DEBUG));
        logger.info("Log level configured to: " + settings.getLogLevel());

        // Detach any previously attached file appender named "FILE" to prevent duplicates.
        rootLogger.detachAppender("FILE");

        // If file logging is enabled, configure and attach a FileAppender.
        if (settings.isFileLoggingEnabled()) {
            FileAppender fileAppender = new FileAppender();
            fileAppender.setContext(loggerContext);
            fileAppender.setName("FILE");
            // Set file location based on settings.
            fileAppender.setFile(settings.getLogFilePath());

            // Configure pattern layout for file logging.
            PatternLayout layout = new PatternLayout();
            layout.setContext(loggerContext);
            layout.setPattern("%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n");
            layout.start();

            // Create an encoder wrapping the layout.
            LayoutWrappingEncoder encoder = new LayoutWrappingEncoder();
            encoder.setContext(loggerContext);
            encoder.setLayout(layout);
            encoder.start();

            fileAppender.setEncoder(encoder);
            fileAppender.start();

            // Attach the file appender to the root logger.
            rootLogger.addAppender(fileAppender);
            logger.info("File logging enabled. Log file: " + settings.getLogFilePath());
        } else {
            logger.info("File logging is disabled.");
        }
    }

    /**
     * Convenience method for logging an INFO level message.
     *
     * @param message the message to log.
     */
    public static void info(String message) {
        logger.info(message);
    }

    /**
     * Convenience method for logging a DEBUG level message.
     *
     * @param message the message to log.
     */
    public static void debug(String message) {
        logger.debug(message);
    }

    /**
     * Convenience method for logging a WARN level message.
     *
     * @param message the message to log.
     */
    public static void warn(String message) {
        logger.warn(message);
    }

    /**
     * Convenience method for logging an ERROR level message.
     *
     * @param message the message to log.
     */
    public static void error(String message) {
        logger.error(message);
    }

    /**
     * Convenience method for logging a TRACE level message.
     *
     * @param message the message to log.
     */
    public static void trace(String message) {
        logger.trace(message);
    }

    /**
     * Logs sample messages at various levels.
     */
    public static void logTest() {
        trace("This is a TRACE level message.");
        debug("This is a DEBUG level message.");
        info("This is an INFO level message.");
        warn("This is a WARN level message.");
        error("This is an ERROR level message.");
    }

    /**
     * Main method for testing the logging configuration and the convenience methods.
     *
     * @param args command-line arguments (not used).
     */
    public static void main(String[] args) {
        // For testing, create a default Settings instance.
        Settings settings = new Settings();
        // Optionally set desired settings.
        settings.setLogLevel("INFO");
        settings.setFileLoggingEnabled(true);
        settings.setLogFilePath("./log/app.log");

        // Configure logging with these settings.
        configureLogLevel(settings);

        // Log sample messages.
        logTest();
    }
}
