package com.example.courseprifs.utils;

import org.mindrot.jbcrypt.BCrypt;

public final class PasswordUtils {
    private PasswordUtils() {
    }

    public static String hashPassword(String plain) {
        return BCrypt.hashpw(plain, BCrypt.gensalt());
    }

    public static boolean verifyPassword(String plain, String hashed) {
        if (plain == null || hashed == null) {
            return false;
        }
        return BCrypt.checkpw(plain, hashed);
    }
}
