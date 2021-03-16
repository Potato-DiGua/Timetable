package com.potato.timetable.ui.login.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.potato.timetable.R
import com.potato.timetable.base.ViewBindingFragment
import com.potato.timetable.databinding.FragmentRegisterBinding
import com.potato.timetable.ext.afterTextChanged
import com.potato.timetable.ui.login.LoginActivity
import com.potato.timetable.ui.login.login.LoginFragment
import com.potato.timetable.util.PatternUtils
import com.potato.timetable.util.Utils

class RegisterFragment : ViewBindingFragment<FragmentRegisterBinding>() {
    companion object {
        fun newInstance() = RegisterFragment()
    }

    private lateinit var viewModel: RegisterViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(RegisterViewModel::class.java)


        viewModel.registerResult.observe(viewLifecycleOwner, { result ->
            binding.register.isEnabled = true
            binding.loading.visibility = View.GONE
            if (result.success) {
                Utils.showToast("注册成功")
                if (activity is LoginActivity) {
                    val bundle = Bundle()
                    bundle.putString(LoginFragment.ACCOUNT_KEY, binding.account.text.toString())
                    (activity as LoginActivity).navigate(0, bundle)
                }
            } else {
                Utils.showToast(result.error)
            }
        })

        binding.register.setOnClickListener {
            binding.loading.visibility = View.VISIBLE
            binding.register.isEnabled = false
            viewModel.register(
                    binding.username.text.toString(),
                    binding.account.text.toString(),
                    binding.password.text.toString(),
            )
        }

        binding.username.afterTextChanged {
            isValid()
        }
        binding.account.afterTextChanged {
            isValid()
        }
        binding.password.afterTextChanged {
            isValid()
        }
    }

    private fun isValid() {
        var temp = false
        if (!PatternUtils.isUserNameValid(binding.username.text.toString())) {
            binding.username.error = getString(R.string.invalid_name)
        } else if (!PatternUtils.isAccountValid(binding.account.text.toString())) {
            binding.account.error = getString(R.string.invalid_account)
        } else if (!PatternUtils.isPasswordValid(binding.password.text.toString())) {
            binding.password.error = getString(R.string.invalid_password)
        } else {
            temp = true
        }
        binding.register.isEnabled = temp
    }
}