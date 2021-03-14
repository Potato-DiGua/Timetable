package com.potato.timetable.ui.login.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.potato.timetable.R
import com.potato.timetable.data.LoginRepository
import com.potato.timetable.data.Result
import com.potato.timetable.util.PatternUtils

class LoginViewModel(private val loginRepository: LoginRepository) : ViewModel() {

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    fun login(username: String, password: String) {
        when (val result = loginRepository.login(username, password)) {
            is Result.Success -> {
                _loginResult.postValue(LoginResult(success = LoggedInUserView(displayName = result.data.userName)))
            }
            is Result.Error -> {
                _loginResult.postValue(LoginResult(error = result.exception.message))
            }
            else -> {
                _loginResult.postValue(LoginResult(error = "登录失败"))
            }
        }
    }

    fun loginDataChanged(username: String, password: String) {
        if (!PatternUtils.isAccountValid(username)) {
            _loginForm.value = LoginFormState(usernameError = R.string.invalid_account)
        } else if (!PatternUtils.isPasswordValid(password)) {
            _loginForm.value = LoginFormState(passwordError = R.string.invalid_password)
        } else {
            _loginForm.value = LoginFormState(isDataValid = true)
        }
    }
}