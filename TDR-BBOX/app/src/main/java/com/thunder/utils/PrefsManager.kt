package com.thunder.utils

import android.content.Context
import android.content.SharedPreferences

object PrefsManager {
    private const val PREFS_NAME = "mundo_prefs"
    private const val KEY_LICENSE = "license_key"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    private const val KEY_USER_DATA = "user_data"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Save the license key
     */
    fun saveLicenseKey(context: Context, key: String) {
        getPrefs(context).edit().apply {
            putString(KEY_LICENSE, key)
            apply()
        }
    }

    /**
     * Get the saved license key
     */
    fun getLicenseKey(context: Context): String? {
        return getPrefs(context).getString(KEY_LICENSE, null)
    }

    /**
     * Save login status
     */
    fun setLoggedIn(context: Context, isLoggedIn: Boolean) {
        getPrefs(context).edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, isLoggedIn)
            apply()
        }
    }

    /**
     * Check if user is logged in
     */
    fun isLoggedIn(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_IS_LOGGED_IN, false)
    }

    /**
     * Save user data (optional - for caching user info)
     */
    fun saveUserData(context: Context, appName: String?, expiryDate: String?) {
        getPrefs(context).edit().apply {
            putString("app_name", appName)
            putString("expiry_date", expiryDate)
            apply()
        }
    }

    /**
     * Get user app name
     */
    fun getUserAppName(context: Context): String? {
        return getPrefs(context).getString("app_name", null)
    }

    /**
     * Get expiry date
     */
    fun getExpiryDate(context: Context): String? {
        return getPrefs(context).getString("expiry_date", null)
    }

    /**
     * Clear all saved data (logout)
     */
    fun clearAllData(context: Context) {
        getPrefs(context).edit().clear().apply()
    }

    /**
     * Clear only login session (keep license key)
     */
    fun clearSession(context: Context) {
        getPrefs(context).edit().apply {
            remove(KEY_IS_LOGGED_IN)
            remove("app_name")
            remove("expiry_date")
            apply()
        }
    }
}

