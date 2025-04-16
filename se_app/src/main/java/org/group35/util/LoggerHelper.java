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
 * LoggerHelper demonstrates the use of a professional logging framework.
 * It configures the log level and optionally adds a file appender based on application settings.
 */
public class LoggerHelper {
    // Obtain a logger instance for this class using SLF4J.
    private static final Logger logger = LoggerFactory.getLogger(LoggerHelper.class);

    /**
     * Configures the root logger level and optionally adds file logging based on the provided settings.
     *
     * @param settings the Settings instance containing configuration such as log level and file logging options.
     */
    public static void configureLogLevel(Settings settings) {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        // Use fully qualified name for ch.qos.logback.classic.Logger to avoid the conflict.
        ch.qos.logback.classic.Logger rootLogger =
                (ch.qos.logback.classic.Logger) loggerContext.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        // Set root logger level based on settings (defaulting to DEBUG if the value is invalid).
        rootLogger.setLevel(Level.toLevel(settings.getLogLevel(), Level.DEBUG));
        logger.info("Log level configured to: " + settings.getLogLevel());

        // Detach any previously added file appender with the name "FILE".
        rootLogger.detachAppender("FILE");

        // If file logging is enabled, configure and attach a file appender.
        if (settings.isFileLoggingEnabled()) {
            FileAppender fileAppender = new FileAppender();
            fileAppender.setContext(loggerContext);
            fileAppender.setName("FILE");
            // Set the file path from settings.
            fileAppender.setFile(settings.getLogFilePath());

            // Configure a PatternLayout for formatting log messages.
            PatternLayout layout = new PatternLayout();
            layout.setContext(loggerContext);
            layout.setPattern("%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n");
            layout.start();

            // Use a LayoutWrappingEncoder to wrap the layout.
            LayoutWrappingEncoder encoder = new LayoutWrappingEncoder();
            encoder.setContext(loggerContext);
            encoder.setLayout(layout);
            encoder.start();

            fileAppender.setEncoder(encoder);
            fileAppender.start();

            // Add the file appender to the root logger.
            rootLogger.addAppender(fileAppender);
            logger.info("File logging enabled. Log file: " + settings.getLogFilePath());
        } else {
            logger.info("File logging is disabled.");
        }
    }

    /**
     * Logs sample messages at various log levels.
     */
    public static void logTest() {
        logger.trace("This is a TRACE level message.");
        logger.debug("This is a DEBUG level message.");
        logger.info("This is an INFO level message.");
        logger.warn("This is a WARN level message.");
        logger.error("This is an ERROR level message.");
    }

    /**
     * Main method to test the logging configuration and functionality.
     *
     * @param args command-line arguments (not used).
     */
    public static void main(String[] args) {
        // For demonstration, create a new Settings instance.
        Settings settings = new Settings();
        // Optionally change settings:
        settings.setLogLevel("INFO");
        settings.setFileLoggingEnabled(true);
        settings.setLogFilePath("logs/app.log");

        // Configure logger based on settings.
        configureLogLevel(settings);
        // Log sample messages.
        logTest();
    }
}
