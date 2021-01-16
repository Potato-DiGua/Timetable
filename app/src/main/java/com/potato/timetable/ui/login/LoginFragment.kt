package com.potato.timetable.ui.login

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.util.Base64
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.fragment.app.Fragment
import com.bigkoo.pickerview.builder.OptionsPickerBuilder
import com.potato.timetable.R
import com.potato.timetable.httpservice.CollegeService
import com.potato.timetable.ui.main.MainActivity
import com.potato.timetable.util.Config
import com.potato.timetable.util.KeyStoreUtils.decrypt
import com.potato.timetable.util.KeyStoreUtils.encrypt
import com.potato.timetable.util.RetrofitUtils.Companion.retrofit
import com.potato.timetable.util.Utils.showToast
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.*


class LoginFragment : Fragment() {
    private var mAccountEt: EditText? = null
    private var mPwEt: EditText? = null
    private var mRandomCodeEt: EditText? = null
    private var mRandomCodeIv: ImageView? = null
    private var mLoginBtn: Button? = null
    private var mProgressBar: ProgressBar? = null

    private var collegeService: CollegeService = retrofit.create(CollegeService::class.java)
    private val compositeDisposable = CompositeDisposable()

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
        val view = inflater.inflate(R.layout.fragment_login, container, false)
        init(view)
        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_login, menu)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        collegeService
                .isLogin
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ resp ->
                    if (resp.status == 0 && resp.data) {
                        selectTerm()
                    } else {
                        setRandomCodeImg()
                    }
                }, {
                    showToast("服务器不可用,判断是否登录 失败")
                })
                .add(compositeDisposable)
    }

    /**
     * 初始化
     */
    private fun init(view: View) {
        val collegeName = view.findViewById<TextView>(R.id.tv_college_name)
        collegeName.text = Config.getCollegeName()

        mRandomCodeIv = view.findViewById(R.id.iv_random_code)
        mLoginBtn = view.findViewById(R.id.btn_login)
        mProgressBar = view.findViewById(R.id.loading)
        mAccountEt = view.findViewById(R.id.et_account)
        mPwEt = view.findViewById(R.id.et_password)
        mRandomCodeEt = view.findViewById(R.id.et_random_code)

        mLoginBtn?.setOnClickListener {
            hideInput()
            val account = mAccountEt?.text.toString()
            val pw = mPwEt?.text.toString()
            val randomCode = mRandomCodeEt?.text.toString()
            if (pw.isEmpty() || account.isEmpty() || randomCode.isEmpty()) {
                showToast("内容不能为空")
            } else {
                setLoading(true)
                login(account, pw, randomCode)
            }
        }

        mRandomCodeIv?.setOnClickListener { setRandomCodeImg() }
        readAccountFromLocal()
    }

    private fun saveAccountToLocal(account: String, pwd: String) {
        val sharedPreferences = context?.getSharedPreferences("account", Context.MODE_PRIVATE)
        val editor = sharedPreferences?.edit()
        editor?.putString(KEY_ACCOUNT, encrypt(account))
        editor?.putString(KEY_PWD, encrypt(pwd))
        editor?.apply()
    }

    private fun readAccountFromLocal() {
        val sharedPreferences = context?.getSharedPreferences("account", Context.MODE_PRIVATE)
        mAccountEt!!.setText(decrypt(sharedPreferences?.getString(KEY_ACCOUNT, "")))
        mPwEt!!.setText(decrypt(sharedPreferences?.getString(KEY_PWD, "")))
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
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ resp ->
                    showSelectDialog(resp.data)
                }, { showToast("服务器不可用,获取学期选项失败！") })
                .add(compositeDisposable)
    }

    /**
     * 显示学期选择对话框
     *
     * @param termOptions 学期选项
     */
    private fun showSelectDialog(termOptions: List<String>?) {
        if (termOptions == null || termOptions.isEmpty()) {
            showToast("无法获取学期选项")
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
        collegeService.getCourses(term).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ resp ->
                    setLoading(false)
                    if (resp.status == 0 && resp.data != null) {
                        resp.data.sort()

                        showToast(if (resp.data.isEmpty()) "该学期没有课程" else "导入成功")
                        MainActivity.sCourseList = resp.data
                        setUpdateResult()
                        activity?.finish()
                    } else {
                        showToast("导入失败")
                    }
                }, {
                    showToast("服务器不可用,获取课程失败")
                })
                .add(compositeDisposable)
    }

    /**
     * 设置是否进入加载状态
     *
     * @param b
     */
    private fun setLoading(b: Boolean) {
        mLoginBtn!!.isEnabled = !b
        if (b) {
            mProgressBar!!.visibility = View.VISIBLE
        } else {
            mProgressBar!!.visibility = View.GONE
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
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ resp ->
                    setLoading(false)
                    if (resp.status == 0 && resp.data) {
                        saveAccountToLocal(mAccountEt!!.text.toString(), mPwEt!!.text.toString())
                        selectTerm()
                    } else {
                        showToast("账户或密码或验证码错误，登陆失败")
                        setRandomCodeImg()
                    }
                }, { showToast("服务器不可用,登录失败!") })
                .add(compositeDisposable)
    }

    /**
     * 从登录页面下载并加载验证码
     */
    private fun setRandomCodeImg() {
        collegeService.randomImg.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ resp ->
                    if (resp.status == 0) {
                        val img = Base64.decode(resp.data.base64, Base64.DEFAULT)
                        mRandomCodeIv?.setImageBitmap(BitmapFactory.decodeByteArray(img, 0, img.size))
                        mRandomCodeEt?.filters = arrayOf<InputFilter>(LengthFilter(resp.data.randomCodeLength))
                    } else {
                        if (resp.msg.isNotEmpty()) {
                            showToast(resp.msg)
                        }
                    }
                }, { showToast("服务器不可用,获取验证码失败!") })
                .add(compositeDisposable)
    }

    override fun onDetach() {
//        mHandler.removeCallbacksAndMessages(null)
        compositeDisposable.dispose()
        super.onDetach()
    }
}

fun Disposable.add(c: CompositeDisposable) {
    c.add(this)
}