package com.phaswana.smartpantrymanager;

import android.content.Context;
import android.content.SharedPreferences;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class AuthManager {

    private static final String PREFS_NAME = "SmartPantryAuthPrefs";
    private static final String KEY_HAS_REGISTERED = "has_registered";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USERNAME = "auth_username";
    private static final String KEY_PASSWORD_HASH = "auth_password_hash";

    private final SharedPreferences prefs;

    public AuthManager(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public boolean isRegistered() {
        return prefs.getBoolean(KEY_HAS_REGISTERED, false);
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public boolean register(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
            return false;
        }

        String hash = hashPassword(password);
        prefs.edit()
                .putString(KEY_USERNAME, username.trim())
                .putString(KEY_PASSWORD_HASH, hash)
                .putBoolean(KEY_HAS_REGISTERED, true)
                .putBoolean(KEY_IS_LOGGED_IN, false)
                .apply();
        return true;
    }

    public boolean login(String username, String password) {
        if (!isRegistered() || username == null || password == null) {
            return false;
        }

        String storedUser = prefs.getString(KEY_USERNAME, "");
        String storedHash = prefs.getString(KEY_PASSWORD_HASH, "");
        String inputHash = hashPassword(password);

        if (storedUser.equalsIgnoreCase(username.trim()) && storedHash.equals(inputHash)) {
            prefs.edit().putBoolean(KEY_IS_LOGGED_IN, true).apply();
            return true;
        }
        return false;
    }

    public void logout() {
        prefs.edit().putBoolean(KEY_IS_LOGGED_IN, false).apply();
    }

    public String getRegisteredUsername() {
        return prefs.getString(KEY_USERNAME, "User");
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            return String.valueOf(password.hashCode());
        }
    }
}
