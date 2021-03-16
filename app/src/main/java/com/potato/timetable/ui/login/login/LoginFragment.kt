package com.potato.timetable.ui.login.login

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.potato.timetable.base.ViewBindingFragment
import com.potato.timetable.databinding.FragmentLoginBinding
import com.potato.timetable.ext.afterTextChanged
import com.potato.timetable.util.PatternUtils
import com.potato.timetable.util.Utils

class LoginFragment : ViewBindingFragment<FragmentLoginBinding>() {
    private lateinit var loginViewModel: LoginViewModel

    companion object {
        const val ACCOUNT_KEY = "account"
        fun newInstance() = LoginFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        val account = arguments?.getString(ACCOUNT_KEY)
        if (!TextUtils.isEmpty(account)) {
            binding.username.setText(account)
            binding.username.invalidate()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        loginViewModel = ViewModelProvider(this, LoginViewModelFactory())
                .get(LoginViewModel::class.java)

        loginViewModel.loginFormState.observe(viewLifecycleOwner, Observer {
            val loginState = it ?: return@Observer

            // disable login button unless both username / password is valid
            binding.login.isEnabled = loginState.isDataValid

            if (loginState.usernameError != null) {
                binding.username.error = getString(loginState.usernameError)
            }
            if (loginState.passwordError != null) {
                binding.password.error = getString(loginState.passwordError)
            }
        })

        loginViewModel.loginResult.observe(viewLifecycleOwner, Observer {
            val loginResult = it ?: return@Observer

            binding.loading.visibility = View.GONE
            binding.login.isEnabled = true
            if (loginResult.error != null) {
                showLoginFailed(loginResult.error)
            }
            if (loginResult.success != null) {
                updateUiWithUser(loginResult.success)
            }

            //Complete and destroy login activity once successful
//            finish()
        })

        binding.username.afterTextChanged {
            loginViewModel.loginDataChanged(
                    binding.username.text.toString(),
                    binding.password.text.toString()
            )
        }

        binding.password.apply {
            afterTextChanged {
                loginViewModel.loginDataChanged(
                        binding.username.text.toString(),
                        binding.password.text.toString()
                )
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE -> {
                        val account = binding.username.text.toString()
                        val pwd = binding.password.text.toString()
                        if (PatternUtils.isAccountValid(account) && PatternUtils.isPasswordValid(pwd)) {
                            loginViewModel.login(
                                    account,
                                    pwd
                            )
                        } else {
                            Utils.showToast("请输入有效账号和密码")
                        }

                    }
                }
                false
            }

            binding.login.setOnClickListener {
                Log.d("test", "登录")
                binding.loading.visibility = View.VISIBLE
                binding.login.isEnabled = false
                Thread {
                    loginViewModel.login(binding.username.text.toString(), binding.password.text.toString())
                }.start()

            }
        }
    }

    private fun updateUiWithUser(model: LoggedInUserView) {
        val displayName = model.displayName
        Utils.showToast("欢迎回来 $displayName");
        activity?.finish()
    }

    private fun showLoginFailed(errorString: String) {
        Utils.showToast(errorString)
    }
}

