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
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt());
    }

    /**
     * Verify whether the plaintext password matches the stored hashed password.
     */
    public static boolean checkPassword(String plainTextPassword, String hashedPassword) {
        if (plainTextPassword == null || hashedPassword == null) {
            return false;
        }
        return BCrypt.checkpw(plainTextPassword, hashedPassword);
    }
}
