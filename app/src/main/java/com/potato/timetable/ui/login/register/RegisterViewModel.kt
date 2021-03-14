package com.potato.timetable.ui.login.register

import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.potato.timetable.httpservice.UserService
import com.potato.timetable.util.RetrofitUtils
import com.potato.timetable.util.Utils
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers

class RegisterViewModel : ViewModel() {
    private val _registerResult = MutableLiveData<Boolean>()
    val registerResult: LiveData<Boolean> = _registerResult

    private val userService: UserService = RetrofitUtils.retrofit.create(UserService::class.java)

    fun register(name: String, account: String, pwd: String) {
        userService
                .register(name, account, pwd)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ it ->
                    _registerResult.value = it.status == 0

                    if (it.status != 0 && TextUtils.isEmpty(it.msg)) {
                        Utils.showToast(it.msg)
                    } else {
                        Utils.showToast("注册失败")
                    }

                }, { error ->
                    Log.e(javaClass.name, error.message, error)
                    Utils.showToast("注册失败")
                })

    }
}