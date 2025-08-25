package com.example.zakazivanjeaplikacija.utils

import android.content.Context
import android.content.SharedPreferences


class TokenManager(context: Context) {


    private val PREF_NAME = "MyAuthPrefs"

    private val KEY_JWT_TOKEN = "jwt_token"


    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        prefs.edit().putString(KEY_JWT_TOKEN, token).apply()
    }

    fun getToken(): String? {
        return prefs.getString(KEY_JWT_TOKEN, null)
    }

    fun deleteToken() {
        prefs.edit().remove(KEY_JWT_TOKEN).apply()
    }
}
