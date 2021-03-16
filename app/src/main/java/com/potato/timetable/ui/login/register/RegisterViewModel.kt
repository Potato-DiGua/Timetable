package com.potato.timetable.ui.login.register

import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.potato.timetable.httpservice.UserService
import com.potato.timetable.model.ResponseWrap
import com.potato.timetable.util.RetrofitUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterViewModel : ViewModel() {
    private val _registerResult = MutableLiveData<RegisterResult>()
    val registerResult: LiveData<RegisterResult> = _registerResult

    private val userService: UserService = RetrofitUtils.retrofit.create(UserService::class.java)

    fun register(name: String, account: String, pwd: String) {
        userService
                .register(name, account, pwd)
                .enqueue(object : Callback<ResponseWrap<Any>> {
                    override fun onResponse(call: Call<ResponseWrap<Any>>, response: Response<ResponseWrap<Any>>) {
                        val result = RegisterResult(false)

                        if (response.code() == 200 && response.body() != null) {
                            val body = response.body()!!

                            result.success = body.status == 0
                            result.error = if (body.status != 0 && TextUtils.isEmpty(body.msg)) {
                                body.msg
                            } else {
                                "注册失败"
                            }
                        }
                        _registerResult.postValue(result)
                    }

                    override fun onFailure(call: Call<ResponseWrap<Any>>, t: Throwable) {
                        Log.e(javaClass.name, t.message, t)
                        _registerResult.postValue(RegisterResult(false, "注册失败"))
                    }
                })

    }

    data class RegisterResult(var success: Boolean,
                              var error: String = "")
}