package com.potato.timetable.ui.collegelogin

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.InputFilter.LengthFilter
import android.text.TextUtils
import android.util.Base64
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.lifecycle.Lifecycle
import com.bigkoo.pickerview.builder.OptionsPickerBuilder
import com.potato.timetable.R
import com.potato.timetable.base.ViewBindingFragment
import com.potato.timetable.databinding.FragmentCollegeLoginBinding
import com.potato.timetable.httpservice.CollegeService
import com.potato.timetable.ui.main.MainActivity
import com.potato.timetable.util.Config
import com.potato.timetable.util.KeyStoreUtils.decrypt
import com.potato.timetable.util.KeyStoreUtils.encrypt
import com.potato.timetable.util.RetrofitUtils.Companion.retrofit
import com.potato.timetable.util.Utils
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.*


class CollegeLoginFragment : ViewBindingFragment<FragmentCollegeLoginBinding>() {
    private var collegeService: CollegeService = retrofit.create(CollegeService::class.java)

    companion object {
        const val EXTRA_UPDATE_TIMETABLE = "update_timetable"
        private const val KEY_ACCOUNT = "encryption_account"
        private const val KEY_PWD = "encryption_pwd"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        super.onCreateView(inflater, container, savedInstanceState)
        init()
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_login, menu)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        collegeService
                .isLogin
                .compose(lifecycleProvider.bindUntilEvent(Lifecycle.Event.ON_DESTROY))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ resp ->
                    if (resp.status == 0 && resp.data) {
                        selectTerm()
                    } else {
                        setRandomCodeLength()
                    }
                }, {
                    if (TextUtils.isEmpty(it.message)) {
                        Utils.showToast("服务器不可用,判断是否登录 失败")
                    } else {
                        Utils.showToast(it.message)
                    }
                    setRandomCodeLength()

                })
    }

    /**
     * 初始化
     */
    private fun init() {
        binding.tvCollegeName.text = Config.getCollegeName()
        binding.btnLogin.setOnClickListener {
            hideInput()
            val account = binding.etAccount.text.toString()
            val pw = binding.etPassword.text.toString()
            val randomCode = binding.etRandomCode.text.toString()
            if (pw.isEmpty() || account.isEmpty() || randomCode.isEmpty()) {
                Utils.showToast("内容不能为空")
            } else {
                setLoading(true)
                login(account, pw, randomCode)
            }
        }

        binding.ivRandomCode.setOnClickListener { setRandomCodeImg() }
        readAccountFromLocal()
    }

    private fun setRandomCodeLength() {
        collegeService.getRandomCodeLength(Config.getCollegeName())
                .compose(lifecycleProvider.bindUntilEvent(Lifecycle.Event.ON_DESTROY))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ resp ->
                    if (resp != null && resp.status == 0 && resp.data != null) {
                        binding.etRandomCode.filters = arrayOf(LengthFilter(resp.data))
                        setRandomCodeImg()
                    }
                }, { Utils.showToast("无法获取验证码长度") })
    }

    private fun saveAccountToLocal(account: String, pwd: String) {
        context?.getSharedPreferences("account", Context.MODE_PRIVATE)
                ?.edit()
                ?.putString(KEY_ACCOUNT, encrypt(account))
                ?.putString(KEY_PWD, encrypt(pwd))
                ?.apply()
    }

    private fun readAccountFromLocal() {
        val sharedPreferences = context?.getSharedPreferences("account", Context.MODE_PRIVATE)
        binding.etAccount.setText(decrypt(sharedPreferences?.getString(KEY_ACCOUNT, "")))
        binding.etPassword.setText(decrypt(sharedPreferences?.getString(KEY_PWD, "")))

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            activity?.finish()
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * 通知主界面更新
     */
    private fun setUpdateResult() {
        val intent = Intent()
        intent.putExtra(EXTRA_UPDATE_TIMETABLE, true)
        activity?.setResult(Activity.RESULT_OK, intent)
    }

    private fun selectTerm() {
        collegeService
                .termOptions
                .compose(lifecycleProvider.bindUntilEvent(Lifecycle.Event.ON_DESTROY))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ resp ->
                    showSelectDialog(resp.data)
                }, { Utils.showToast("服务器不可用,获取学期选项失败！") })
    }

    /**
     * 显示学期选择对话框
     *
     * @param termOptions 学期选项
     */
    private fun showSelectDialog(termOptions: List<String>?) {
        if (termOptions == null || termOptions.isEmpty()) {
            Utils.showToast("无法获取学期选项")
            return
        }
        val mOptionsPv = OptionsPickerBuilder(activity) { options1: Int, _: Int, _: Int, _: View? ->
            setLoading(true)
            getCourses(termOptions[options1])
        }.build<String>()

        mOptionsPv.setTitleText("选择学期")
        mOptionsPv.setNPicker(termOptions, null, null)
        mOptionsPv.setSelectOptions(0)
        mOptionsPv.show()
    }

    /**
     * 获取课程表中的课程
     *
     * @param term
     */
    private fun getCourses(term: String) {
        collegeService.getCourses(term)
                .compose(lifecycleProvider.bindUntilEvent(Lifecycle.Event.ON_DESTROY))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ resp ->
                    setLoading(false)
                    if (resp.status == 0 && resp.data != null) {
                        resp.data.sort()

                        Utils.showToast(if (resp.data.isEmpty()) "该学期没有课程" else "导入成功")
                        MainActivity.sCourseList = resp.data
                        setUpdateResult()
                        activity?.finish()
                    } else {
                        Utils.showToast("导入失败")
                    }
                }, {
                    Utils.showToast("服务器不可用,获取课程失败")
                })
    }

    /**
     * 设置是否进入加载状态
     *
     * @param b
     */
    private fun setLoading(b: Boolean) {
        binding.btnLogin.isEnabled = !b
        if (b) {
            binding.loading.visibility = View.VISIBLE
        } else {
            binding.loading.visibility = View.GONE
        }
    }

    /**
     * 隐藏键盘
     */
    private fun hideInput() {
        val activity: Activity? = activity
        if (activity != null) {
            val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager?
            val v = activity.window.peekDecorView()
            if (null != v) {
                imm?.hideSoftInputFromWindow(v.windowToken, 0)
            }
        }
    }

    /**
     * 登录
     *
     * @param account
     * @param pw
     * @param randomCode String 验证码
     */
    private fun login(account: String, pwd: String, randomCode: String) {
        collegeService.login(account, pwd, randomCode)
                .compose(lifecycleProvider.bindUntilEvent(Lifecycle.Event.ON_DESTROY))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ resp ->
                    setLoading(false)
                    if (resp.status == 0 && resp.data) {
                        saveAccountToLocal(binding.etAccount.text.toString(), binding.etPassword.text.toString())
                        selectTerm()
                    } else {
                        Utils.showToast("账户或密码或验证码错误，登陆失败")
                        setRandomCodeImg()
                    }
                }, { Utils.showToast("服务器不可用,登录失败!") })
    }

    /**
     * 从登录页面下载并加载验证码
     */
    private fun setRandomCodeImg() {
        collegeService.randomImg
                .compose(lifecycleProvider.bindUntilEvent(Lifecycle.Event.ON_DESTROY))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ resp ->
                    if (resp.status == 0) {
                        val img = Base64.decode(resp.data.base64, Base64.DEFAULT)
                        binding.ivRandomCode.setImageBitmap(BitmapFactory.decodeByteArray(img, 0, img.size))
                    } else {
                        if (resp.msg.isNotEmpty()) {
                            Utils.showToast(resp.msg)
                        }
                    }
                }, { Utils.showToast("服务器不可用,获取验证码失败!") })
    }
}