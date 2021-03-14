package com.potato.timetable.util

import android.util.Patterns
import java.util.regex.Pattern

object PatternUtils {

    private val phonePattern = Pattern.compile("^1[3-9]\\d{9}$")

    @JvmStatic
    fun isChinesePhone(phone: String): Boolean {
        return phonePattern.matcher(phone).matches()
    }

    @JvmStatic
    fun isEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    @JvmStatic
    fun isUserNameValid(name: String): Boolean {
        return name.length >= 3
    }

    @JvmStatic
    fun isAccountValid(account: String): Boolean {
        return if (account.contains('@')) {
            isEmail(account)
        } else {
            isChinesePhone(account)
        }
    }

    @JvmStatic
    fun isPasswordValid(password: String): Boolean {
        return password.length >= 6
    }
}