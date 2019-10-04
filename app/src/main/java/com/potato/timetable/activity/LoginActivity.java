package com.potato.timetable.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.bigkoo.pickerview.builder.OptionsPickerBuilder;
import com.bigkoo.pickerview.listener.OnOptionsSelectListener;
import com.bigkoo.pickerview.view.OptionsPickerView;
import com.potato.timetable.CsuCollege;
import com.potato.timetable.R;
import com.potato.timetable.bean.Course;
import com.potato.timetable.util.ExcelUtils;
import com.potato.timetable.util.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private CsuCollege mCsuCollege=new CsuCollege();

    private EditText mAccountEt;
    private EditText mPwEt;
    private EditText mRandomCodeEt;
    private ImageView mRandomCodeIv;
    private String mRandomCodeImgPath;
    private Button mLoginBtn;
    private ProgressBar mProgressBar;
    private OptionsPickerView mOptionsPv;

    private Handler mHandler=new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            int what=msg.what;
            switch (what)
            {
                case MSG_RANDOM_CODE:
                    Bitmap bitmap= BitmapFactory.decodeFile(mRandomCodeImgPath);
                    mRandomCodeIv.setImageBitmap(bitmap);
                    break;
                case MSG_LOGIN_SUCCESS:
                    setLoading(false);
                    showSelectDialog(msg.obj.toString());
                    break;
                case MSG_LOGIN_FAILED:
                    setLoading(false);
                    setRandomCodeImg();
                    Toast.makeText(LoginActivity.this,"账号密码验证码错误",Toast.LENGTH_SHORT).show();
                    break;
                case MSG_GET_COURSES_SUCCESS:
                    setUpdateResult();
                    Toast.makeText(LoginActivity.this,"导入成功",Toast.LENGTH_SHORT).show();
                    setLoading(false);
                    LoginActivity.this.finish();
                    break;
                case MSG_GET_COURSES_FAILED:
                    Toast.makeText(LoginActivity.this,"导入失败",Toast.LENGTH_SHORT).show();
                    setLoading(false);
                    break;
                    default:
                        break;
            }

        }
    };


    private static final int MSG_RANDOM_CODE=1;
    private static final int MSG_LOGIN_SUCCESS=2;
    private static final int MSG_LOGIN_FAILED=3;
    private static final int MSG_GET_COURSES_SUCCESS=4;
    private static final int MSG_GET_COURSES_FAILED=5;

    public static final String EXTRA_UPDATE_TIMETABLE="update_timetable";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        init();
        setActionBar();
        ImageView bgIv=findViewById(R.id.iv_bg);
        Utils.setBackGround(bgIv);
        File file =getExternalCacheDir();
        mRandomCodeImgPath=file.getAbsolutePath()+File.separator+"randomcode.jpg";
        setRandomCodeImg();
    }

    /**
     * 初始化
     */
    private void init()
    {
        mRandomCodeIv=findViewById(R.id.iv_random_code);
        mLoginBtn=findViewById(R.id.btn_login);
        mProgressBar=findViewById(R.id.loading);
        mAccountEt=findViewById(R.id.et_account);
        mPwEt=findViewById(R.id.et_password);
        mRandomCodeEt=findViewById(R.id.et_random_code);

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideInput();

                String account=mAccountEt.getText().toString();
                String pw=mPwEt.getText().toString();
                String randomCode=mRandomCodeEt.getText().toString();

                if(pw.isEmpty()||account.isEmpty()||randomCode.isEmpty())
                {
                    Toast.makeText(LoginActivity.this,"内容不能为空",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    setLoading(true);
                    login(account,pw,randomCode);
                }

            }
        });

        mRandomCodeIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setRandomCodeImg();
            }
        });
    }

    private void setActionBar()
    {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(String.format(getString(R.string.title_activity_login),CsuCollege.COLLEGE_NAME));
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId();
        if(id==android.R.id.home)
        {
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
     * 显示学期选择对话框
     * @param str 学期用&&隔开 形成字符串
     */
    private void showSelectDialog(String str)
    {
        final List<String> items=new ArrayList<>();
        String[] strings=str.split("&&");
        for(String s:strings)
        {
            if(!s.isEmpty())
                items.add(s);
        }
        mOptionsPv = new OptionsPickerBuilder(this, new OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int options2, int options3, View v) {
                setLoading(true);
                downloadTimetableExcel(items.get(options1));
            }
        }).build();

        mOptionsPv.setTitleText("选择学期");

        mOptionsPv.setNPicker(items, null, null);
        mOptionsPv.setSelectOptions(0);
        mOptionsPv.show();

    }

    /**
     * 从教务系统下载课程表Excel
     * @param term
     */
    private void downloadTimetableExcel(final String term)
    {
        new Thread(new Runnable() {
            @Override
            public void run() {

                String filepath=getExternalFilesDir(null)+File.separator+"timetable.xls";
                if(mCsuCollege.downloadTimetableExcel(term,filepath))
                {
                    Message msg=new Message();
                    List<Course> courseList=ExcelUtils.handleExcel(filepath,4,2);
                    if(courseList==null)
                    {
                        msg.what=MSG_GET_COURSES_FAILED;
                    }
                    else
                    {
                        MainActivity.sCourseList=courseList;
                        msg.what=MSG_GET_COURSES_SUCCESS;
                    }
                    mHandler.sendMessage(msg);
                }
            }
        }).start();


    }

    /**
     * 设置是否进入加载状态
     * @param b
     */
    private void setLoading(boolean b)
    {

        mLoginBtn.setEnabled(!b);
        if(b)
        {
            mProgressBar.setVisibility(View.VISIBLE);
        }
        else
        {
            mProgressBar.setVisibility(View.GONE);
        }

    }

    /**
     * 隐藏键盘
     */
    private void hideInput() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        View v = getWindow().peekDecorView();
        if (null != v) {
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }
    /**
     * 登录
     * @param account
     * @param pw
     * @param randomCode String 验证码
     */
    private void login(final String account,final String pw,final String randomCode)
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String str=mCsuCollege.login(account,pw,randomCode);
                Message msg=new Message();
                if(str.isEmpty())
                {
                    msg.what=MSG_LOGIN_FAILED;
                }
                else
                {
                    msg.what=MSG_LOGIN_SUCCESS;
                    msg.obj=str;
                }
                mHandler.sendMessage(msg);
            }
        }).start();

    }

    /**
     * 从登录页面下载并加载验证码
     */
    private void setRandomCodeImg()
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(mCsuCollege.mCookie==null||mCsuCollege.mCookie.isEmpty())
                {
                    if(mCsuCollege.getCookie())
                    {
                        if(mCsuCollege.downloadRandomCodeImg(mRandomCodeImgPath))
                        {
                            Message msg=new Message();
                            msg.what=MSG_RANDOM_CODE;
                            mHandler.sendMessage(msg);

                        }
                    }
                }
                else
                {
                    if(mCsuCollege.downloadRandomCodeImg(mRandomCodeImgPath))
                    {
                        Message msg=new Message();
                        msg.what=MSG_RANDOM_CODE;
                        mHandler.sendMessage(msg);

                    }
                }


            }
        }).start();

    }
}
