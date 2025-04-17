package org.group35.util;

import org.mindrot.jbcrypt.BCrypt;
import org.group35.util.LoggerHelper;

/**
 *  Password related utility class that performs password hashing and verification.
 */
public class PasswordUtils {

    /**
     * Hash the plaintext password.
     */
    public static String hashPassword(String plainTextPassword) {
        LoggerHelper.trace("hashPassword() called");
        String hashed = BCrypt.hashpw(plainTextPassword, BCrypt.gensalt());
        LoggerHelper.trace("Password hashed successfully");
        return hashed;
    }

    /**
     * Verify whether the plaintext password matches the stored hashed password.
     */
    public static boolean checkPassword(String plainTextPassword, String hashedPassword) {
        if (plainTextPassword == null || hashedPassword == null) {
            LoggerHelper.trace("checkPassword() returned false due to null input");
            return false;
        }
        boolean result = BCrypt.checkpw(plainTextPassword, hashedPassword);
        LoggerHelper.trace("checkPassword() result: " + result);
        return result;
    }
}