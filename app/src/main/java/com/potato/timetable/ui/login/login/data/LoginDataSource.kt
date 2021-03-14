package com.potato.timetable.ui.login.login.data

import android.util.Log
import com.google.gson.Gson
import com.potato.timetable.httpservice.UserService
import com.potato.timetable.model.User
import com.potato.timetable.util.RetrofitUtils
import java.io.IOException

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class LoginDataSource {

    fun login(username: String, password: String): Result<User> {
        try {
            val resp = RetrofitUtils.retrofit.create(UserService::class.java).login(username, password).execute();
            val user = resp.body();
            Log.d("test", Gson().toJson(user))
            if (resp.isSuccessful && user != null) {
                return if (user.status == 0) {
                    Result.Success(user.data)
                } else {
                    Result.Error(Exception(user.msg))
                }
            }
            return Result.Error(Exception("服务器不可用"))
        } catch (e: Throwable) {
            Log.d("test", e.stackTraceToString())
            return Result.Error(IOException("Error logging in", e))
        }
    }

    fun logout() {
        // TODO: revoke authentication
    }
}