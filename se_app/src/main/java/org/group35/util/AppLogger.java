package org.group35.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Global application logger utility using SLF4J and Logback.
 */
public class AppLogger {
    private static final Logger logger = LoggerFactory.getLogger("AppLogger");

    public static void debug(String message, Object... args) {
        logger.debug(message, args);
    }

    public static void info(String message, Object... args) {
        logger.info(message, args);
    }

    public static void warn(String message, Object... args) {
        logger.warn(message, args);
    }

    public static void error(String message, Object... args) {
        logger.error(message, args);
    }
}
