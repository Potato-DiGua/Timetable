package com.potato.timetable.activity;


import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.potato.timetable.bean.Course;
import com.potato.timetable.R;
import com.potato.timetable.util.Config;
import com.potato.timetable.util.ExcelUtils;
import com.potato.timetable.util.FileUtils;
import com.potato.timetable.util.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private FrameLayout mFrameLayout;
    private TextView mWeekOfTermTextView;
    private ImageView mbgImageView;

    public static List<Course> sCourseList;

    private static final int REQUEST_CODE_COURSE_DETAILS = 0;
    private static final int REQUEST_CODE_COURSE_EDIT = 1;
    private static final int REQUEST_CODE_FILE_CHOOSE = 2;
    private static final int REQUEST_CODE_CONFIG = 3;
    private static final int CELL_HEIGHT = 70;

    private static int sHeaderClassNumWidth;


    private int choice;

    private static final Map<String, Integer> mMap = new HashMap<String, Integer>() {{
        put("单周", 1);
        put("双周", 0);
        put("周", 2);

    }};//判断是否为本周
    public static float VALUE_1DP;//1dp的值
    private static int DISPLAY_WIDTH;//屏幕宽度
    private static final int REQUEST_EXTERNAL_STORAGE = 1;

    private static final String[] PERMISSIONS_STORAGE = {

            "android.permission.READ_EXTERNAL_STORAGE",

            "android.permission.WRITE_EXTERNAL_STORAGE"};

    private  Handler mHandler=new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            int what=msg.what;
            if(what==MSG_UPDATE)
            {
                String str=msg.obj.toString();
                if(str.isEmpty())
                {
                    Toast.makeText(MainActivity.this,"当前版本已经是最新版",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    showUpdateDialog(str);
                }

            }
        }
    };
    private static int MSG_UPDATE=1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWritePermission();//得到读写权限用于保存课表信息

        TextView[] weekArrayTextView = new TextView[]{//储存周几表头
                findViewById(R.id.tv_sun),
                findViewById(R.id.tv_mon),
                findViewById(R.id.tv_tues),
                findViewById(R.id.tv_wed),
                findViewById(R.id.tv_thur),
                findViewById(R.id.tv_fri),
                findViewById(R.id.tv_sat)

        };
        mWeekOfTermTextView = findViewById(R.id.tv_week_of_term);

        Config.readFormSharedPreferences(this);//读取当前周信息


        mbgImageView = findViewById(R.id.iv_bg_main);

        Utils.setBackGround(mbgImageView);

        sHeaderClassNumWidth = findViewById(R.id.ll_header_class_num).getLayoutParams().width;
        //计算1dp的数值方便接下来设置元素尺寸,提高效率
        VALUE_1DP = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1,
                getResources().getDisplayMetrics());
        //Log.d("1dp",String.valueOf(VALUE_1DP));
        //获取屏幕宽度，用于设置课程视图的宽度
        DISPLAY_WIDTH = getResources().getDisplayMetrics().widthPixels;



        int week = getWeekOfDay();

        //Log.d("week", "" + week);

        updateCurrentWeek(week);


        weekArrayTextView[week - 1].setBackground(getDrawable(R.color.colorLightBlue));

        setBackGroundDash(week);

        Toolbar toolbar = findViewById(R.id.toolbar);
        //设置标题为自定义toolbar
        setSupportActionBar(toolbar);


        mFrameLayout = findViewById(R.id.fl_timetable);

        initTimetable();

        Utils.setPATH(getExternalFilesDir(null).getAbsolutePath() + File.separator + "pictures");
    }

    private void setBackGroundDash(int week)//设置课表当天的虚线框,在create时初始化,在destroy之前不会改变
    {
        View back = findViewById(R.id.back_line_dash);
        FrameLayout.LayoutParams backlayoutParams = (FrameLayout.LayoutParams) back.getLayoutParams();
        float width = (DISPLAY_WIDTH - sHeaderClassNumWidth) / 7.0f;
        backlayoutParams.width = (int) width;
        backlayoutParams.leftMargin = (int) (width * (week == 1 ? 6 : week - 2));
    }

    private int getWeekOfDay()//获取今天周几
    {
        //周日为一个星期的第一天，数值为1-7
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.DAY_OF_WEEK);
    }

    /**
     * 启动应用时进行当前周数的更新,
     * 不足：不能在每周一00:00准时更新数据，需要依靠用户启动来实现更新
     */
    private void updateCurrentWeek(int week) {
        if (week == 2)//判断是否为周一，周一更新当前周数
        {
            if (!Config.isFlagCurrentWeek())//利用flag实现周一只更新一次
            {
                Config.currentWeekAdd();
                Config.setFlagCurrentWeek(true);
                Config.saveSharedPreferences(this);
            }
        } else {
            if (Config.isFlagCurrentWeek()) {
                Config.setFlagCurrentWeek(false);
                Config.saveSharedPreferences(this);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.menu_append://菜单导入Excel
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*Excel/xls");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, REQUEST_CODE_FILE_CHOOSE);
                break;
            case R.id.menu_set_week://菜单设置当前周
                showSelectCurrentWeekDialog();
                break;
            case R.id.menu_append_class://菜单添加课程
                Intent intent1 = new Intent(this, EditActivity.class);
                startActivityForResult(intent1, REQUEST_CODE_COURSE_EDIT);
                break;
            case R.id.menu_config:
                Intent intent2 = new Intent(this, ConfigActivity.class);
                startActivityForResult(intent2, REQUEST_CODE_CONFIG);
                break;
            case R.id.menu_update:
                callCheckUpdate();
                    break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    private void showUpdateDialog(final String url)
    {
        final AlertDialog alertDialog=new AlertDialog.Builder(MainActivity.this)
            .setTitle("提示")
            .setMessage("检测到新版本,是否下载?").create();
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE,"确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Log.d("update",url);
                Uri uri=Uri.parse(url);
                Intent intent3=new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent3);
                alertDialog.dismiss();
            }
        });
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE,"取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                alertDialog.dismiss();
            }
        });
        alertDialog.show();

    }
    private void callCheckUpdate()
    {

        final long versionCode=Utils.getLocalVersionCode(this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                final String url=Utils.checkUpdate(versionCode);
                    Message message=new Message();
                    message.what=MSG_UPDATE;
                    message.obj=url;
                    mHandler.sendMessage(message);
            }
        }).start();
    }

    private void showSelectCurrentWeekDialog() {//显示周数列表,让用户从中选择
        String[] items = new String[25];
        for (int i = 0; i < 25; i++) {
            items[i] = String.valueOf(i + 1);
        }

        AlertDialog.Builder singleChoiceDialog =
                new AlertDialog.Builder(MainActivity.this);
        singleChoiceDialog.setTitle("选择当前周");
        // 第二个参数是默认选项，此处设置为0
        singleChoiceDialog.setSingleChoiceItems(items, Config.getCurrentWeek() - 1,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        choice = which;
                    }
                });
        singleChoiceDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (choice != -1) {
                            Config.setCurrentWeek(choice + 1);//选中项的序号+1即为用户设置的周数
                            updateTimetable();
                            Config.saveSharedPreferences(MainActivity.this);
                        }
                    }
                });
        singleChoiceDialog.show();
    }

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

    private void initTimetable()//根据保存的信息，创建课程表
    {

        //设置标题中显示的当前周数
        mWeekOfTermTextView.setText(String.format(getString(R.string.day_of_week), Config.getCurrentWeek()));
        //sCourseList=mMyDBHelper.getCourseList();

        sCourseList = FileUtils.readFromJson(this);

        //读取失败返回
        if (sCourseList == null)
        {
            sCourseList=new ArrayList<>();
            return;
        }

        //Log.d("courseNum",String.valueOf(sCourseList.size()));

        int size = sCourseList.size();
        if (size != 0) {
            updateTimetable();
        }

    }

    private void updateTimetable() {//更新课程表视图

        //设置标题中显示的当前周数
        mWeekOfTermTextView.setText(String.format(getString(R.string.day_of_week), Config.getCurrentWeek()));

        List<Course> courseList = new ArrayList<>();

        boolean[] flag = new boolean[12];//-1表示节次没有课程,其他代表占用课程的在mCourseList中的索引

        int weekOfDay = 0;

        int size = sCourseList.size();

        for (int index = 0; index < size; index++)//当位置有两个及以上课程时,显示本周上的课程,其他不显示
        {

            Course course = sCourseList.get(index);
            //Log.d("week", course.getDayOfWeek() + "");
            if (course.getDayOfWeek() != weekOfDay) {
                for (int i = 0; i < flag.length; i++) {
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
                        courseList.remove(courseList.size() - 1);

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


        //清空除背景ImageView和充当背景的虚线框view以外的所有子对象
        int count = mFrameLayout.getChildCount();
        for (int i = count - 1; i > 1; i--)
            mFrameLayout.removeViewAt(i);



        float width = (DISPLAY_WIDTH - sHeaderClassNumWidth) / 7.0f;

        //int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, CELL_HEIGHT, getResources().getDisplayMetrics());
        int height = (int) (CELL_HEIGHT * VALUE_1DP);

        size = courseList.size();

        StringBuilder stringBuilder = new StringBuilder();


        int[] color = new int[]{
                ContextCompat.getColor(this, R.color.colorOrange),
                ContextCompat.getColor(this, R.color.colorTomato),
                ContextCompat.getColor(this, R.color.colorGreen),
                ContextCompat.getColor(this, R.color.colorCyan),
                ContextCompat.getColor(this, R.color.colorPurple),
        };

        //Log.d("size", size + "");
        for (int i = 0; i < size; i++) {

            Course course = courseList.get(i);
            int class_num = course.getClassLength();
            int week = course.getDayOfWeek() - 1;
            int class_start = course.getClassStart() - 1;

            View view = initTextView(class_num, (int) (week * width), class_start * height);

            TextView textView = view.findViewById(R.id.grid_item_text_view);

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
                myGrad.setColor(getResources().getColor(R.color.colorGray));
                stringBuilder.insert(0, "<small>[非本周]</small>\n");
                String str = stringBuilder.toString();
                str = str.replaceAll("\n", "<br />");
                textView.setText(Html.fromHtml(str));

            }

            textView.setBackground(myGrad);

            textView.getLayoutParams().width = (int) (width - 6 * VALUE_1DP);

            mFrameLayout.addView(view);

            stringBuilder.delete(0, stringBuilder.length());
        }

    }

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

    private View initTextView(int class_num, final int left, final int top) {

        View view = getLayoutInflater().inflate(R.layout.item_timetable, mFrameLayout, false);

        TextView textView = view.findViewById(R.id.grid_item_text_view);
        if (class_num != 2) {
            textView.getLayoutParams().height = (int) (VALUE_1DP * (CELL_HEIGHT * class_num - 6));//设置课程视图高度
        }

        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) textView.getLayoutParams();
        layoutParams.topMargin = (int) (top + 3 * VALUE_1DP);
        layoutParams.leftMargin = (int) (left + 3 * VALUE_1DP);

        return view;
    }

    private boolean courseIsThisWeek(Course course)//判断是否为本周
    {
        String class_week = course.getWeekOfTerm();
        if(class_week==null)
            return false;
        String[] strings = class_week.split(",");
        int currentWeek = Config.getCurrentWeek();
        for (String s : strings) {
            if (s.contains("-")) {
                String[] str = s.split("-");
                int start = Integer.parseInt(str[0]);
                int end = Integer.parseInt(str[1]);

                if (currentWeek >= start && currentWeek <= end) {
                    int i = mMap.get(course.getWeekOptions());
                    if (i == 2 || currentWeek % 2 == i)
                        return true;

                }
            } else {
                if (currentWeek == Integer.parseInt(s))
                    return true;
            }

        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK)
            return;
        if (requestCode == REQUEST_CODE_FILE_CHOOSE) {


            Uri uri= data.getData();

            String path = FileUtils.getPath(MainActivity.this, uri);
            sCourseList = ExcelUtils.handleExcel(path);
            if (path == null || path.isEmpty())
                return;
            //mMyDBHelper.insertItems(sCourseList);
            FileUtils.saveToJson(sCourseList, MainActivity.this);
            initTimetable();
            //Log.d("path", path);
        } else if (requestCode == REQUEST_CODE_COURSE_EDIT) {
            if (data == null)
                return;
            boolean update = data.getBooleanExtra(EditActivity.EXTRA_UPDATE_TIMETABLE, false);
            if (update)
                updateTimetable();
        } else if (requestCode == REQUEST_CODE_COURSE_DETAILS) {
            if (data == null)
                return;
            boolean update = data.getBooleanExtra(EditActivity.EXTRA_UPDATE_TIMETABLE, false);
            if (update)
                updateTimetable();
        } else if (requestCode == REQUEST_CODE_CONFIG) {//更新背景
            if (data == null)
                return;
            boolean update = data.getBooleanExtra(ConfigActivity.EXTRA_UPDATE_BG, false);
            if (update)
                Utils.setBackGround(mbgImageView);
        }
    }
}
