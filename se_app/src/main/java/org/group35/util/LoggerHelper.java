package org.group35.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.group35.config.Settings;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import ch.qos.logback.classic.PatternLayout;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * LoggerHelper provides methods to configure logging based on application settings
 * and static convenience methods for logging at various levels.
 * Convenience methods now log via the caller's class logger.
 */
public class LoggerHelper {
    // SLF4J logger for this helper (only used for internal configuration messages)
    private static final Logger helperLogger = LoggerFactory.getLogger(LoggerHelper.class);

    /**
     * Configures the root logger level and optionally adds file logging.
     */
    public static void configureLogLevel(Settings settings) {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        ch.qos.logback.classic.Logger rootLogger =
                (ch.qos.logback.classic.Logger) loggerContext.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        rootLogger.setLevel(Level.toLevel(settings.getLogLevel(), Level.DEBUG));
        helperLogger.info("Log level configured to: {}", settings.getLogLevel());

        rootLogger.detachAppender("FILE");
        if (settings.isFileLoggingEnabled()) {
            FileAppender fileAppender = new FileAppender();
            fileAppender.setContext(loggerContext);
            fileAppender.setName("FILE");

            String logFolder = settings.getLogFilePath();
            File folder = new File(logFolder);
            if (!folder.exists()) folder.mkdirs();

            String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
            String fileName = logFolder + File.separator + timestamp + ".log";
            fileAppender.setFile(fileName);

            PatternLayout layout = new PatternLayout();
            layout.setContext(loggerContext);
            layout.setPattern("%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n");
            layout.start();

            LayoutWrappingEncoder encoder = new LayoutWrappingEncoder();
            encoder.setContext(loggerContext);
            encoder.setLayout(layout);
            encoder.start();

            fileAppender.setEncoder(encoder);
            fileAppender.start();
            rootLogger.addAppender(fileAppender);
            helperLogger.info("File logging enabled. Log file: {}", fileName);
        } else {
            helperLogger.info("File logging is disabled.");
        }
    }

    // Obtains the SLF4J Logger for the calling class
    private static Logger getCallerLogger() {
        String className = Thread.currentThread().getStackTrace()[3].getClassName();
        return LoggerFactory.getLogger(className);
    }

    public static void info(String message) {
        getCallerLogger().info(message);
    }

    public static void debug(String message) {
        getCallerLogger().debug(message);
    }

    public static void warn(String message) {
        getCallerLogger().warn(message);
    }

    public static void error(String message) {
        getCallerLogger().error(message);
    }

    public static void trace(String message) {
        getCallerLogger().trace(message);
    }

    public static void trace(String message, Throwable t) {
        getCallerLogger().trace(message, t);
    }

    public static void logTest() {
        trace("This is a TRACE level message.");
        debug("This is a DEBUG level message.");
        info("This is an INFO level message.");
        warn("This is a WARN level message.");
        error("This is an ERROR level message.");
    }
}
