package com.potato.timetable.ui.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.bigkoo.pickerview.builder.OptionsPickerBuilder;
import com.bigkoo.pickerview.listener.OnOptionsSelectListener;
import com.bigkoo.pickerview.view.OptionsPickerView;
import com.potato.timetable.R;
import com.potato.timetable.bean.Course;
import com.potato.timetable.colleges.CsuCollege;
import com.potato.timetable.colleges.ShmtuCollege;
import com.potato.timetable.colleges.base.College;
import com.potato.timetable.ui.main.MainActivity;
import com.potato.timetable.util.HttpUtils;
import com.potato.timetable.util.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LoginActivity extends AppCompatActivity {
    private FragmentManager fragmentManager;

    private Handler mHandler = new Handler();

    private boolean judgeFlag = true;//判断网络是否可用的循环退出标志，方便结束线程

    public static final String EXTRA_UPDATE_TIMETABLE = "update_timetable";
    private static final String KEY_ACCOUNT = "account";
    private static final String KEY_PWD = "pwd";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

//        init();
        setActionBar();
        ImageView bgIv = findViewById(R.id.iv_bg);
        Utils.setBackGround(this, bgIv);
        fragmentManager = getSupportFragmentManager();      //初始化管理者
        LoginFragment loginFragment=new LoginFragment();      //第一页Fragment
        fragmentManager.beginTransaction()
                .add(R.id.fragment_container,loginFragment)
                .commit();
    }

    private void judgeConnected() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (judgeFlag) {
                    if (!HttpUtils.isNetworkConnected()) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(
                                        LoginActivity.this,
                                        "当前网络不可用，请检查网络设置！",
                                        Toast.LENGTH_SHORT);
                            }
                        });
                        try {
                            Thread.sleep(30000);//每30秒循环一次
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
        }).start();
    }

    private void setActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null){
            actionBar.setTitle(R.string.title_activity_login);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 通知主界面更新
     */
    private void setUpdateResult() {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_UPDATE_TIMETABLE, true);
        setResult(RESULT_OK, intent);
    }

    /**
     * 隐藏键盘
     */
    private void hideInput() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        View v = getWindow().peekDecorView();
        if (null != v && imm != null) {
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }
}
