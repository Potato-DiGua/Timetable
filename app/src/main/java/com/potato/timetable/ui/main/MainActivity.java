package com.potato.timetable.ui.main;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bigkoo.pickerview.builder.OptionsPickerBuilder;
import com.bigkoo.pickerview.listener.OnOptionsSelectChangeListener;
import com.bigkoo.pickerview.listener.OnOptionsSelectListener;
import com.bigkoo.pickerview.view.OptionsPickerView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.potato.timetable.R;
import com.potato.timetable.bean.Course;
import com.potato.timetable.bean.Send;
import com.potato.timetable.bean.Time;
import com.potato.timetable.ui.config.ConfigActivity;
import com.potato.timetable.ui.coursedetails.CourseDetailsActivity;
import com.potato.timetable.ui.settime.SetTimeActivity;
import com.potato.timetable.ui.editcourse.EditActivity;
import com.potato.timetable.ui.login.LoginActivity;
import com.potato.timetable.util.CalendarReminderUtils;
import com.potato.timetable.util.Config;
import com.potato.timetable.util.ExcelUtils;
import com.potato.timetable.util.FileUtils;
import com.potato.timetable.util.OkHttpUtils;
import com.potato.timetable.util.ShareUtils;
import com.potato.timetable.util.Utils;
import com.uuzuche.lib_zxing.activity.CaptureActivity;
import com.uuzuche.lib_zxing.activity.CodeUtils;
import com.uuzuche.lib_zxing.activity.ZXingLibrary;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private FrameLayout mFrameLayout;
    private TextView mWeekOfTermTextView;
    private ImageView mBgImageView;
    private ImageButton mAddImgBtn;
    private LinearLayout headerClassNumLl;
    private boolean flagUpdateCalendar=false;

    public static List<Course> sCourseList;
    public static Time[] sTimes;

    private List<TextView> mClassTableTvList = new ArrayList<>();
    private TextView[] mClassNumHeaders = null;


    private static final int REQUEST_CODE_COURSE_DETAILS = 0;
    private static final int REQUEST_CODE_COURSE_EDIT = 1;
    private static final int REQUEST_CODE_FILE_CHOOSE = 2;
    private static final int REQUEST_CODE_CONFIG = 3;
    private static final int REQUEST_CODE_LOGIN = 4;
    private static final int REQUEST_CODE_SCAN = 5;
    private static final int REQUEST_CODE_SET_TIME = 6;

    private static final int REQ_PER_Calendar = 0x11;//日历权限申请

    private OptionsPickerView mOptionsPv;

    public static float VALUE_1DP;//1dp的值

    private static float sCellWidthPx;//课程视图的宽度(px)
    private static float sCellHeightPx;//课程视图的高度;


    private static final int REQUEST_EXTERNAL_STORAGE = 1;

    private static final String[] PERMISSIONS_STORAGE = {

            "android.permission.READ_EXTERNAL_STORAGE",

            "android.permission.WRITE_EXTERNAL_STORAGE"};

    private MyHandler mHandler = new MyHandler(this);

    private static class MyHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;

        public MyHandler(MainActivity activityCompat) {
            mActivity = new WeakReference<>(activityCompat);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            final MainActivity mainActivity = mActivity.get();
            int what = msg.what;
            if (what == MSG_UPDATE) {
                String str = msg.obj.toString();
                if (str.isEmpty()) {
                    Toast.makeText(mainActivity, "当前版本已经是最新版", Toast.LENGTH_SHORT).show();
                } else {
                    mainActivity.showUpdateDialog(str);
                }

            }
        }
    }

    private static int MSG_UPDATE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWritePermission();//得到读写权限用于保存课表信息

        OkHttpUtils.init(this);//初始化okhttp

        int[] weekTextView = new int[]{//储存周几表头
                R.id.tv_sun,
                R.id.tv_mon,
                R.id.tv_tues,
                R.id.tv_wed,
                R.id.tv_thur,
                R.id.tv_fri,
                R.id.tv_sat

        };
        mWeekOfTermTextView = findViewById(R.id.tv_week_of_term);
        mAddImgBtn = findViewById(R.id.img_btn_add);
        mBgImageView = findViewById(R.id.iv_bg_main);
        mFrameLayout = findViewById(R.id.fl_timetable);
        headerClassNumLl = findViewById(R.id.ll_header_class_num);


        Config.readFormSharedPreferences(this);//读取当前周信息

        Utils.setPATH(getExternalFilesDir(null).getAbsolutePath() + File.separator + "pictures");

        //计算1dp的数值方便接下来设置元素尺寸,提高效率
        VALUE_1DP = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1,
                getResources().getDisplayMetrics());

        //获取课程节数表头的宽度
        float headerClassNumWidth = getResources().getDimension(R.dimen.table_header_class_width);
        //设置课程格子高度和宽度
        setTableCellDimens(headerClassNumWidth);

        int week = getWeekOfDay();

        //Log.d("week", "" + week);
        TextView weekTv = findViewById(weekTextView[week - 1]);
        weekTv.setBackground(getDrawable(R.color.day_of_week_color));
        //设置标题为自定义toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        initScanQRCode();

        initTimetable();

        initAddBtn();

        Utils.setBackGround(this, mBgImageView);

        setCalendarEvent();
    }
    private void updateCalendarEvent(){
        CalendarReminderUtils.deleteCalendarEvent(this, CalendarReminderUtils.DESCRIPTION);
        addClassCalendarEvent(getCoursesNeedToTake());
    }
    private void setCalendarEvent() {
        if(sTimes==null)
            return;
        //获取权限
        if (CalendarReminderUtils.checkPermission(this)) {
            Calendar calendar = initCalendar();
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
            calendar.add(Calendar.DATE, -dayOfWeek);
            final long start = calendar.getTimeInMillis();
            calendar.add(Calendar.DATE, 7);
            final long end = calendar.getTimeInMillis();
            int size= CalendarReminderUtils.findCalendarEvent(
                    this, CalendarReminderUtils.DESCRIPTION, start, end);
            List<Course> courseList=getCoursesNeedToTake();
            if(size!=courseList.size()){
                //依靠备注清除日程
                CalendarReminderUtils.deleteCalendarEvent(this, CalendarReminderUtils.DESCRIPTION);
                addClassCalendarEvent(courseList,start);
            }

        } else {
            CalendarReminderUtils.fetchPermission(this, REQ_PER_Calendar);
        }
    }
    private List<Course> getCoursesNeedToTake(){
        int currentWeek = Config.getCurrentWeek();
        if (getWeekOfDay() == 1) {//周日插入下周课程的日程
            currentWeek++;
        }
        //周日更新下周课程表
        List<Course> tempList = new LinkedList<>();

        for (Course course : sCourseList) {
            if (courseIsThisWeek(course, currentWeek)) {
                tempList.add(course);
            }
        }
        return tempList;
    }
    private void addClassCalendarEvent(List<Course> courses) {
        Calendar calendar = initCalendar();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        calendar.add(Calendar.DATE, -dayOfWeek);
        final long start = calendar.getTimeInMillis();
        addClassCalendarEvent(courses, start);
    }

    private void addClassCalendarEvent(List<Course> courses, final long start) {
        final int minute = 1000 * 60;
        final int hour = minute * 60;
        final int day = 24 * hour;
        Log.d("stimes",courses.size()+"");
        for (Course course : courses) {
            Log.d("stimes",course.getClassStart()+"");
            String classStart = sTimes[course.getClassStart() - 1].getStart();
            String classEnd = sTimes[course.getClassStart() - 1 + course.getClassLength() - 1].getEnd();
            if (!classStart.isEmpty() && !classEnd.isEmpty()) {
                String[] strings = classStart.split(":");
                int startHour = Integer.parseInt(strings[0]);
                int startMinute = Integer.parseInt(strings[1]);
                strings = classEnd.split(":");
                int endHour = Integer.parseInt(strings[0]);
                int endMinute = Integer.parseInt(strings[1]);
                Uri uri = CalendarReminderUtils.addCalendarEvent(this,
                        course.getName(),
                        CalendarReminderUtils.DESCRIPTION,
                        course.getClassRoom(),
                        start + day * course.getDayOfWeek() + hour * startHour + minute * startMinute,
                        hour * (endHour - startHour) + minute * (endMinute - startMinute));
                CalendarReminderUtils.addCalendarAlarm(this, uri, 10);
            }
        }
    }

    /**
     * 初始化Calendar
     *
     * @return
     */
    private Calendar initCalendar() {
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        //重置
        // 时
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        // 分
        calendar.set(Calendar.MINUTE, 0);
        // 秒
        calendar.set(Calendar.SECOND, 0);
        // 毫秒
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    /**
     * 初始化二维码扫描工具
     */
    private void initScanQRCode() {
        ZXingLibrary.initDisplayOpinion(this);
        ImageView imageView = findViewById(R.id.img_btn_scan);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    // Do not have the permission of camera, request it.
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, 1);
                } else {
                    // Have gotten the permission
                    startActivityForResult(
                            new Intent(MainActivity.this, CaptureActivity.class),
                            REQUEST_CODE_SCAN);
                }

            }
        });
    }

    /**
     * 计算课程格子的长宽
     *
     * @param headerWidth
     */
    private void setTableCellDimens(float headerWidth) {
        //获取屏幕宽度，用于设置课程视图的宽度
        int displayWidth = getResources().getDisplayMetrics().widthPixels;

        //课程视图宽度
        sCellWidthPx = (displayWidth - headerWidth) / 7.0f;
        sCellHeightPx = sCellWidthPx;//小格为正方形
    }


    @SuppressLint("ClickableViewAccessibility")
    private void initFrameLayout() {

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) mFrameLayout.getLayoutParams();
        //设置课程表高度
        layoutParams.height = (int) sCellHeightPx * Config.getMaxClassNum();
        //设置课程表宽度
        layoutParams.width = (int) sCellWidthPx * 7;


        mFrameLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int event = motionEvent.getAction();
                if (event == MotionEvent.ACTION_UP) {
                    if (mAddImgBtn.getVisibility() == View.VISIBLE) {
                        mAddImgBtn.setVisibility(View.GONE);
                    } else {
                        int x = (int) motionEvent.getX();
                        int y = (int) motionEvent.getY();
                        x = x - x % (int) sCellWidthPx;
                        y = y - y % (int) sCellHeightPx;
                        setAddImgBtn(x, y);
                    }
                }
                return true;
            }
        });
    }

    private void initAddBtn() {
        final FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mAddImgBtn.getLayoutParams();
        layoutParams.width = (int) (sCellWidthPx);
        mAddImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditActivity.class);
                int dayOfWeek = layoutParams.leftMargin / (int) sCellWidthPx;
                int classStart = layoutParams.topMargin / (int) sCellHeightPx;
                mAddImgBtn.setVisibility(View.INVISIBLE);
                intent.putExtra(EditActivity.EXTRA_Day_OF_WEEK, dayOfWeek + 1);
                intent.putExtra(EditActivity.EXTRA_CLASS_START, classStart + 1);
                startActivityForResult(intent, REQUEST_CODE_COURSE_EDIT);
            }
        });
    }

    private void setAddImgBtn(int left, int top) {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mAddImgBtn.getLayoutParams();
        layoutParams.leftMargin = left;
        layoutParams.topMargin = top;
        mAddImgBtn.setVisibility(View.VISIBLE);
    }

    /**
     * 获取星期
     *
     * @return 返回1-7代表周几 周日为1
     */
    private int getWeekOfDay() {

        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.DAY_OF_WEEK);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        Intent intent;

        switch (id) {
            case R.id.menu_config://菜单设置
                intent = new Intent(this, ConfigActivity.class);
                startActivityForResult(intent, REQUEST_CODE_CONFIG);
                break;

            case R.id.menu_set_week://菜单设置当前周
                showSelectCurrentWeekDialog();
                break;

            case R.id.menu_import://菜单登录教务系统
                intent = new Intent(this, LoginActivity.class);
                startActivityForResult(intent, REQUEST_CODE_LOGIN);
                break;
            case R.id.menu_append://菜单导入Excel
                intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*Excel/xls");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, REQUEST_CODE_FILE_CHOOSE);
                break;
            case R.id.menu_append_class://菜单添加课程
                intent = new Intent(this, EditActivity.class);
                startActivityForResult(intent, REQUEST_CODE_COURSE_EDIT);
                break;
            case R.id.menu_share_timetable:
                Gson gson = new Gson();
                String json = gson.toJson(sCourseList);
                Log.d("share", json);
                RequestBody requestBody = RequestBody.create(json, OkHttpUtils.JSON);
                Request request = new Request.Builder()
                        .url(ShareUtils.SHARE_URL)
                        .post(requestBody)
                        .build();
                OkHttpUtils.getOkHttpClient().newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "服务器不可用", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        if (response.code() == 200) {
                            String json = response.body().string();
                            Log.d("share", json);
                            final Send<String> send = gson.fromJson(json, new TypeToken<Send>() {
                            }.getType());
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (send.getStatus().equals("ok")) {
                                        showQRCodeDialog(send.getData());
                                    } else {
                                        if (TextUtils.isEmpty(send.getMessage()))
                                            Toast.makeText(MainActivity.this, send.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }

                    }
                });
                break;
            case R.id.menu_set_time://设置上课时间
                startActivityForResult(
                        new Intent(this, SetTimeActivity.class),
                        REQUEST_CODE_SET_TIME);
                break;
            case R.id.menu_update://菜单检查更新
                checkUpdate();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showQRCodeDialog(final String id) {
        if (TextUtils.isEmpty(id)) {
            Toast.makeText(this, "获取二维码失败", Toast.LENGTH_SHORT).show();
        } else {
            final Bitmap bitmap = CodeUtils.createImage(ShareUtils.SHARE_URL + "/" + id, 400, 400, null);
            ImageView imageView = new ImageView(this);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setImageBitmap(bitmap);
            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setTitle("二维码")
                    .setView(imageView)
                    .setPositiveButton("保存", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            FileUtils.saveBitmap(MainActivity.this, bitmap, "LightTimetable-" + Utils.getDate());
                            Toast.makeText(MainActivity.this,
                                    "二维码图片已保存",
                                    Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    })
                    .create();
            alertDialog.show();
        }
    }

    /**
     * 更新对话框
     *
     * @param url
     */
    private void showUpdateDialog(final String url) {
        final AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this)
                .setTitle("提示")
                .setMessage("检测到新版本,是否下载?").create();
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Log.d("update",url);
                Uri uri = Uri.parse(url);
                Intent intent3 = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent3);
                alertDialog.dismiss();
            }
        });
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                alertDialog.dismiss();
            }
        });
        alertDialog.show();

    }

    /**
     * 检查更新
     */
    private void checkUpdate() {

        final long versionCode = Utils.getLocalVersionCode(this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                final String url = Utils.checkUpdate(versionCode);
                Message message = new Message();
                message.what = MSG_UPDATE;
                message.obj = url;
                mHandler.sendMessage(message);
            }
        }).start();
    }

    /**
     * 显示周数列表,让用户从中选择
     */
    private void showSelectCurrentWeekDialog() {
        //String[] items = new String[25];

        final int currentWeek = Config.getCurrentWeek();
        final String str = "当前周为：";
        final List<String> items = new ArrayList<>();
        for (int i = 1; i <= Config.getMaxWeekNum(); i++) {
            //items[i] = String.valueOf(i + 1);
            items.add("第" + i + "周");
        }

        mOptionsPv = new OptionsPickerBuilder(this, new OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int options2, int options3, View v) {
                int selectCurrentWeek = options1 + 1;
                if (selectCurrentWeek != currentWeek) {
                    Config.setCurrentWeek(selectCurrentWeek);
                    updateTimetable();
                    Config.saveCurrentWeek(MainActivity.this);
                }
            }
        }).setOptionsSelectChangeListener(new OnOptionsSelectChangeListener() {
            @Override
            public void onOptionsSelectChanged(int options1, int options2, int options3) {
                mOptionsPv.setTitleText(str + items.get(options1));
            }
        }).build();

        mOptionsPv.setTitleText("当前周为:" + items.get(currentWeek - 1));

        mOptionsPv.setNPicker(items, null, null);
        mOptionsPv.setSelectOptions(currentWeek - 1);
        mOptionsPv.show();
    }

    /**
     * 获取读写权限
     */

    private void getWritePermission() {
        try {

            //检测是否有写的权限

            int permission = ActivityCompat.checkSelfPermission(this,

                    "android.permission.WRITE_EXTERNAL_STORAGE");

            if (permission != PackageManager.PERMISSION_GRANTED) {

                // 没有写的权限，去申请写的权限，会弹出对话框

                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);

            }

        } catch (Exception e) {

            e.printStackTrace();

        }
    }

    /**
     * 初始化课表
     */
    private void initTimetable()//根据保存的信息，创建课程表
    {


        //设置标题中显示的当前周数
        mWeekOfTermTextView.setText(String.format(getString(R.string.day_of_week), Config.getCurrentWeek()));
        //sCourseList=mMyDBHelper.getCourseList();
        //初始化课程表视图
        initFrameLayout();

        //读取时间数据
        sTimes = new FileUtils<Time[]>().readFromJson(this, FileUtils.TIME_FILE_NAME, Time[].class);

        //读取课程数据
        sCourseList = new FileUtils<ArrayList<Course>>().readFromJson(
                this,
                FileUtils.TIMETABLE_FILE_NAME,
                new TypeToken<ArrayList<Course>>() {
                }.getType());

        //更新节数表头
        updateClassNumHeader();
        //读取失败返回
        if (sCourseList == null) {
            sCourseList = new ArrayList<>();
            return;
        }

        //Log.d("courseNum",String.valueOf(sCourseList.size()));

        int size = sCourseList.size();
        if (size != 0) {
            updateTimetable();
        }

        flagUpdateCalendar=false;
    }

    /**
     * 选择需要显示的课程
     *
     * @return
     */
    private List<Course> selectNeedToShowCourse() {
        LinkedList<Course> courseList = new LinkedList<>();

        boolean[] flag = new boolean[12];//-1表示节次没有课程,其他代表占用课程的在mCourseList中的索引

        int weekOfDay = 0;//记录周几

        int size = sCourseList.size();

        for (int index = 0; index < size; index++)//当位置有两个及以上课程时,显示本周上的课程,其他不显示
        {

            Course course = sCourseList.get(index);
            if (!isNotThisWeekCourseNeedToShow(course.getWeekOfTerm())) {
                continue;
            }

            //Log.d("week", course.getDayOfWeek() + "");
            if (course.getDayOfWeek() != weekOfDay) {
                for (int i = 0; i < flag.length; i++) {//初始化flag
                    flag[i] = false;
                }
                weekOfDay = course.getDayOfWeek();
            }

            int class_start = course.getClassStart();
            int class_num = course.getClassLength();

            int i;

            for (i = 0; i < class_num; i++) {
                if (flag[class_start + i - 1]) {
                    //Log.d("action", "if");
                    if (!courseIsThisWeek(course)) {
                        break;
                    } else {
                        courseList.removeLast();//删除最后一个元素
                        courseList.add(course);
                        for (int j = 0; j < class_num; j++) {
                            flag[class_start + j - 1] = true;
                        }
                        break;
                    }
                }
            }
            if (i == class_num) {
                courseList.add(course);
                for (int j = 0; j < class_num; j++) {
                    flag[class_start + j - 1] = true;
                }
            }
        }
        return courseList;
    }

    /**
     * 判断非本周课程是否显示
     *
     * @param weekOfTerm 二进制储存在第几周上课
     */
    private boolean isNotThisWeekCourseNeedToShow(int weekOfTerm) {
        int off = Config.getMaxWeekNum() - Config.getCurrentWeek();
        //判断是否未到上课时间
        if ((1 << off) > weekOfTerm) {
            Log.d("course", "未开始" + Integer.toBinaryString(weekOfTerm));
            return false;
        }
        //判断课程是否已结束
        for (int i = off; i >= 0; i--) {
            if (((1 << i) & weekOfTerm) > 0) {
                return true;
            }
        }
        Log.d("course", weekOfTerm + "结束" + Integer.toBinaryString(weekOfTerm));
        return false;
    }

    private void updateClassNumHeader() {

        headerClassNumLl.getLayoutParams().height = (int) sCellHeightPx * Config.getMaxClassNum();
        if (mClassNumHeaders == null) {
            mClassNumHeaders = new TextView[Config.getMaxClassNum()];
            for (int i = 0, len = mClassNumHeaders.length; i < len; i++) {
                mClassNumHeaders[i] = null;
            }
            headerClassNumLl.removeAllViews();
        }

        //int width = (int) getResources().getDimension(R.dimen.table_header_class_width);
        int height = (int) sCellHeightPx;
        float textSize = getResources().getDimensionPixelSize(R.dimen.class_num_header_text_size);
        StringBuilder stringBuilder = new StringBuilder("12\n22:00\n23:00".length());
        for (int i = 0; i < Config.getMaxClassNum(); i++) {
            TextView textView;
            if (mClassNumHeaders[i] == null) {
                textView = new TextView(this);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, height);

                //默认使用sp为单位，传入的为px,不指定单位字体会变大
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
                //设置对齐方式
                textView.setGravity(Gravity.CENTER);
                //设置文本颜色为黑色
                textView.setTextColor(getResources().getColor(R.color.colorBlack));
                textView.setLayoutParams(layoutParams);
                mClassNumHeaders[i] = textView;
                headerClassNumLl.addView(textView);
            } else {
                textView = mClassNumHeaders[i];
            }
            stringBuilder.append(i + 1);
            if (sTimes != null && i < sTimes.length) {
                stringBuilder.append('\n');
                stringBuilder.append(sTimes[i].getStart());
                stringBuilder.append('\n');
                stringBuilder.append(sTimes[i].getEnd());
            }
            textView.setText(stringBuilder.toString());
            stringBuilder.delete(0, stringBuilder.length());
        }
        //如果修改上课节数则删除多余的textview
        for (int i = Config.getMaxClassNum(); i < mClassNumHeaders.length; i++) {
            headerClassNumLl.removeViewAt(i);
        }
        flagUpdateCalendar=true;//更新日程

    }

    /**
     * 更新课程表视图
     */
    private void updateTimetable() {
        //设置标题中显示的当前周数
        mWeekOfTermTextView.setText(String.format(getString(R.string.day_of_week), Config.getCurrentWeek()));

        List<Course> courseList = selectNeedToShowCourse();

        int size = courseList.size();
        StringBuilder stringBuilder = new StringBuilder();
        int[] color = new int[]{//课程表循环颜色
                ContextCompat.getColor(this, R.color.item_orange),
                ContextCompat.getColor(this, R.color.item_tomato),
                ContextCompat.getColor(this, R.color.item_green),
                ContextCompat.getColor(this, R.color.item_cyan),
                ContextCompat.getColor(this, R.color.item_purple),
        };

        //Log.d("size", size + "");
        int mClassTableListSize = mClassTableTvList.size();

        for (int i = 0; i < size; i++) {
            Course course = courseList.get(i);
            int class_num = course.getClassLength();
            int week = course.getDayOfWeek() - 1;
            int class_start = course.getClassStart() - 1;

            //View view = initTextView(class_num, (int) (week * sCellWidthPx), class_start * height);

            TextView textView;
            //复用课程格，提高性能
            if (i < mClassTableListSize) {
                textView = mClassTableTvList.get(i);
            } else {//已有课程格数量不足新建
                textView = new TextView(this);
                mClassTableTvList.add(textView);
                mFrameLayout.addView(textView);
            }
            setTableCellTextView(textView,
                    class_num, week,
                    class_start);

            setTableClickListener(textView, sCourseList.indexOf(course));

            String name = course.getName();
            if (name.length() > 10) {
                name = name.substring(0, 10) + "...";
            }
            stringBuilder.append(name);
            stringBuilder.append("\n@");
            stringBuilder.append(course.getClassRoom());

            GradientDrawable myGrad = new GradientDrawable();//动态设置TextView背景
            myGrad.setCornerRadius(5 * VALUE_1DP);

            if (courseIsThisWeek(course))//判断是否为当前周课程，如果不是，设置背景为灰色
            {
                myGrad.setColor(color[i % 5]);
                textView.setText(stringBuilder.toString());
            } else {
                myGrad.setColor(getResources().getColor(R.color.item_gray));
                stringBuilder.insert(0, "[非本周]\n");
                textView.setText(stringBuilder.toString());
            }
            textView.setBackground(myGrad);

            stringBuilder.delete(0, stringBuilder.length());
        }

        //删除多余的课程格
        for (int i = size, len = mClassTableTvList.size(); i < len; i++) {
            mFrameLayout.removeView(mClassTableTvList.get(i));
        }
        flagUpdateCalendar=true;//更新日程
    }


    /**
     * 设置课程视图的监听
     *
     * @param textView
     * @param index
     */
    private void setTableClickListener(TextView textView, final int index)//设置课程视图的监听
    {
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CourseDetailsActivity.class);
                intent.putExtra(CourseDetailsActivity.KEY_COURSE_INDEX, index);
                startActivityForResult(intent, REQUEST_CODE_COURSE_DETAILS);
            }
        });
    }

    /**
     * 设置课程格
     *
     * @param textView
     * @param class_num 节数
     * @param left      距左边界的格数
     * @param top       距上边界的格数
     */
    private void setTableCellTextView(TextView textView, int class_num, final int left,
                                      final int top) {

        //Log.d("tablecell", left + "," + top);
        float leftMargin = left * sCellWidthPx;
        float topMargin = top * sCellHeightPx;

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                (int) (sCellWidthPx - 6 * VALUE_1DP),
                (int) (class_num * sCellHeightPx - 6 * VALUE_1DP));

        layoutParams.topMargin = (int) (topMargin + 3 * VALUE_1DP);
        layoutParams.leftMargin = (int) (leftMargin + 3 * VALUE_1DP);

        //设置对齐方式
        textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
        //设置文本颜色为白色
        textView.setTextColor(getResources().getColor(R.color.colorWhite));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.timetable_cell_text_size));

        textView.setLayoutParams(layoutParams);
    }

    /**
     * @param course 课程
     * @return 是否为本周应该上的课程
     */
    private boolean courseIsThisWeek(Course course) {
        return courseIsThisWeek(course, Config.getCurrentWeek());
    }

    /**
     * @param course      课程
     * @param currentWeek 周数
     * @return 是否为currentWeek周数应该上的课程
     */
    private boolean courseIsThisWeek(Course course, int currentWeek) {
        return (course.getWeekOfTerm() >> (Config.getMaxWeekNum() - currentWeek) & 0x01) == 1;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_PER_Calendar) {
            for (int r : grantResults) {
                if (r != PackageManager.PERMISSION_DENIED)
                    finish();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_FILE_CHOOSE:
                    Uri uri = data.getData();

                    String path = FileUtils.getPath(MainActivity.this, uri);
                    sCourseList = ExcelUtils.handleExcel(path);
                    if (path == null || path.isEmpty())
                        return;
                    //mMyDBHelper.insertItems(sCourseList);
                    new FileUtils<List<Course>>().saveToJson(this, sCourseList, FileUtils.TIMETABLE_FILE_NAME);
                    updateTimetable();
                    //Log.d("path", path);
                    break;

                    //更新课程表
                case REQUEST_CODE_COURSE_EDIT:
                case REQUEST_CODE_COURSE_DETAILS:
                    if (data == null)
                        return;
                    boolean update = data.getBooleanExtra(EditActivity.EXTRA_UPDATE_TIMETABLE, false);
                    if (update)
                    {
                        updateTimetable();
                    }
                    break;

                case REQUEST_CODE_CONFIG:
                    if (data == null)
                        return;
                    boolean update_bg = data.getBooleanExtra(ConfigActivity.EXTRA_UPDATE_BG, false);
                    if (update_bg)
                        Utils.setBackGround(this, mBgImageView);
                    break;

                case REQUEST_CODE_LOGIN:
                    if (data == null)
                        return;
                    boolean update_login = data.getBooleanExtra(LoginActivity.EXTRA_UPDATE_TIMETABLE, false);
                    if (update_login)
                        updateTimetable();
                    new FileUtils<List<Course>>().saveToJson(this, sCourseList, FileUtils.TIMETABLE_FILE_NAME);
                    break;
                case REQUEST_CODE_SCAN://处理二维码返回结果
                    //处理扫描结果
                    if (null != data) {
                        Bundle bundle = data.getExtras();
                        if (bundle == null) {
                            return;
                        }
                        if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
                            String url = bundle.getString(CodeUtils.RESULT_STRING);
                            //判断扫描出的二维码的网址是否为本app服务器的网址
                            if (!TextUtils.isEmpty(url) && ShareUtils.judgeURL(url)) {
                                Request request = new Request.Builder()
                                        .url(url)
                                        .build();
                                //使用http获取分享的课程表
                                OkHttpUtils.getOkHttpClient().newCall(request).enqueue(new Callback() {
                                    @Override
                                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                                        mHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(MainActivity.this, "服务器不可用", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }

                                    @Override
                                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                                        if (response.code() == 200) {
                                            String json = response.body().string();
                                            if (!TextUtils.isEmpty(json)) {
                                                Gson gson = new Gson();
                                                //解析获取的json
                                                Send<List<Course>> send = gson.fromJson(
                                                        json, new TypeToken<Send<List<Course>>>() {
                                                        }.getType());
                                                if (send.getData() != null && send.getData().size() > 0) {
                                                    sCourseList = send.getData();
                                                    mHandler.post(() -> {
                                                        updateTimetable();
                                                    });
                                                }
                                            }
                                        }
                                    }
                                });
                            } else {
                                Toast.makeText(this, "无效二维码", Toast.LENGTH_SHORT).show();
                            }
                        } else if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_FAILED) {
                            Toast.makeText(MainActivity.this, "解析二维码失败", Toast.LENGTH_LONG).show();
                        }
                    }
                    break;
                case REQUEST_CODE_SET_TIME:
                    if (data != null && data.getBooleanExtra(SetTimeActivity.EXTRA_UPDATE_Time, false)) {
                        Log.d("update", "更新时间");
                        updateClassNumHeader();
                    }
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        if(flagUpdateCalendar){
            updateCalendarEvent();
        }
        super.onDestroy();
    }
}