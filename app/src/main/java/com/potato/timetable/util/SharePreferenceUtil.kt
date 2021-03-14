package com.potato.timetable.util

import android.content.Context
import com.potato.timetable.MyApplication

object SharePreferenceUtil {
    const val TOKEN_KEY = "token"


    fun getSharedPreferences(name: String) =
            MyApplication.getApplication().getSharedPreferences(name, Context.MODE_PRIVATE)

    @JvmStatic
    fun saveToken(token: String) {
        getSharedPreferences("data").edit().putString(TOKEN_KEY, token).apply()
    }

    @JvmStatic
    fun getToken(): String {
        val sp = getSharedPreferences("data")
        return sp.getString(TOKEN_KEY, "") ?: ""
    }
}