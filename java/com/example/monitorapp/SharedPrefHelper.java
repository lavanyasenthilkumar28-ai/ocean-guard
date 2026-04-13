package com.example.monitorapp;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefHelper {

    private static final String PREF_NAME = "monitor_app_pref";
    private static final String KEY_REGISTERED = "is_registered";
    private static final String KEY_LOGGED_IN = "is_logged_in";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_ROLE = "role";

    public static void setRegistered(Context c, boolean value) {
        SharedPreferences sp = c.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sp.edit().putBoolean(KEY_REGISTERED, value).apply();
    }

    public static boolean isRegistered(Context c) {
        return c.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_REGISTERED, false);
    }

    public static void setLoggedIn(Context c, boolean value) {
        SharedPreferences sp = c.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sp.edit().putBoolean(KEY_LOGGED_IN, value).apply();
    }

    public static boolean isLoggedIn(Context c) {
        return c.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_LOGGED_IN, false);
    }

    public static void saveUser(Context c, String username, String email, String role) {
        SharedPreferences sp = c.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sp.edit()
                .putString(KEY_USERNAME, username)
                .putString(KEY_EMAIL, email)
                .putString(KEY_ROLE, role)
                .apply();
    }

    public static String getUsername(Context c) {
        return c.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getString(KEY_USERNAME, "User");
    }

    public static String getRole(Context c) {
        return c.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getString(KEY_ROLE, "USER");
    }

    public static void logout(Context c) {
        SharedPreferences sp = c.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sp.edit().clear().apply();
    }

    public static Object getEmail(SOSActivity adminActivity) {
        return null;
    }

    public static Object getEmail(UserActivity userActivity) {
        return null;

    }

    public static Object getEmail(AdminActivity adminActivity) {
        return null;
    }
}