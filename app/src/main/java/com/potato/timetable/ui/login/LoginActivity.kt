package com.potato.timetable.ui.login

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.potato.timetable.R
import com.potato.timetable.ui.login.login.LoginFragment
import com.potato.timetable.ui.login.register.RegisterFragment

class LoginActivity : AppCompatActivity() {

    private var _viewPager: ViewPager? = null
    private val viewPager get() = _viewPager!!
    private lateinit var fragments: Array<Fragment>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        fragments = arrayOf(LoginFragment.newInstance(), RegisterFragment.newInstance())
        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager,
                fragments,
                arrayOf("登录", "注册"))

        _viewPager = findViewById(R.id.view_pager)
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = findViewById(R.id.tabs)
        tabs.setupWithViewPager(viewPager)
    }

    fun navigate(currentItem: Int, bundle: Bundle?) {
        if (currentItem < fragments.size && currentItem >= 0) {
            if (bundle != null) {
                fragments[currentItem].arguments = bundle
            }
            viewPager.currentItem = currentItem
        }

    }
}