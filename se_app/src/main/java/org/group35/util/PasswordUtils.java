package org.group35.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 *  Password related utility class that performs password hashing and verification.
 */
public class PasswordUtils {

    /**
     * Hash the plaintext password.
     */
    public static String hashPassword(String plainTextPassword) {
        LogUtils.trace("Starting to hash the password");
        String hashed = BCrypt.hashpw(plainTextPassword, BCrypt.gensalt());
        LogUtils.trace("Password hashing completed successfully");
        return hashed;
    }

    /**
     * Verify whether the plaintext password matches the stored hashed password.
     */
    public static boolean checkPassword(String plainTextPassword, String hashedPassword) {
        if (plainTextPassword == null || hashedPassword == null) {
            LogUtils.trace("Password verification aborted: missing input");
            return false;
        }
        boolean result = BCrypt.checkpw(plainTextPassword, hashedPassword);
        LogUtils.trace("Password verification result: " + result);
        return result;
    }
}
