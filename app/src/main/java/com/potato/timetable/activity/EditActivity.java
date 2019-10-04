package com.potato.timetable.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.bigkoo.pickerview.builder.OptionsPickerBuilder;
import com.bigkoo.pickerview.listener.OnOptionsSelectChangeListener;
import com.bigkoo.pickerview.listener.OnOptionsSelectListener;
import com.bigkoo.pickerview.view.OptionsPickerView;
import com.potato.timetable.R;
import com.potato.timetable.bean.Course;
import com.potato.timetable.util.Config;
import com.potato.timetable.util.FileUtils;
import com.potato.timetable.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EditActivity extends AppCompatActivity {

    public static final String EXTRA_UPDATE_TIMETABLE = "update_timetable";
    private List<String> weekItems;
    private List<String> startItems;
    private List<String> endItems;

    private TextView mClassNumTextView;
    private EditText mNameEditText;
    private EditText mClassRoomEditText;
    private EditText mWeekOfTermEditText;
    private EditText mTeacherEditText;
    private OptionsPickerView pvOptions;
    private Spinner mSpinner;

    private Course mCourse;

    private static final String[] WEEK_ARRAY = new String[]{
            "周一", "周二", "周三", "周四", "周五", "周六", "周日"};

    public static final String KEY_COURSE_INDEX = "course_index";

    /**
     * 保存在MainActivity.WeekOfTerm中的索引值
     */
    private int mIndex;

    private int mClassStart = 0;
    private int mClassEnd = 0;
    private int mDayOfWeek = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        mClassNumTextView = findViewById(R.id.tv_class_num);
        mNameEditText = findViewById(R.id.name_editText);
        mClassRoomEditText = findViewById(R.id.et_class_room);
        mWeekOfTermEditText = findViewById(R.id.et_week_of_term);
        mTeacherEditText = findViewById(R.id.et_teacher);
        mSpinner = findViewById(R.id.spinner_week_options);

        setActionBar();
        mIndex = getIntent().getIntExtra(KEY_COURSE_INDEX, -1);

        if (mIndex != -1) {
            try {
                mCourse = (Course) MainActivity.sCourseList.get(mIndex).clone();
                mClassStart = mCourse.getClassStart();
                mClassEnd = mClassStart + mCourse.getClassLength() - 1;
                mDayOfWeek = mCourse.getDayOfWeek();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
            setDefaultValue();
        } else {
            mCourse = new Course();
        }

        setData();
        setCardViewAlpha();
        ImageView imageView = findViewById(R.id.iv_bg_edit);

        Utils.setBackGround(imageView);

        LinearLayout linearLayout = findViewById(R.id.class_num_layout);
        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideInput();//关闭软键盘防止挡住选择控件
                int start = mCourse.getClassStart();
                if (start == -1)
                    initOptionsPicker(0, 0, 1);
                else
                    initOptionsPicker(mCourse.getDayOfWeek() - 1,
                            start - 1,
                            start - 2 + mCourse.getClassLength());
            }
        });

    }

    /**
     * 设置CardView透明度
     */
    private void setCardViewAlpha() {
        float alpha = Config.getCardViewAlpha();
        CardView cardView = findViewById(R.id.cv_edit_1);
        cardView.setAlpha(alpha);
        cardView = findViewById(R.id.cv_edit_2);
        cardView.setAlpha(alpha);
    }

    /**
     * 通知界面更新
     */
    private void setUpdateResult() {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_UPDATE_TIMETABLE, true);
        setResult(RESULT_OK, intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return super.onCreateOptionsMenu(menu);
    }


    /**
     * 从界面中读取课程信息
     *
     * @return 是否读取成功
     */
    private boolean setCourseFromView() {
        String name = mNameEditText.getText().toString();
        String classroom = mClassRoomEditText.getText().toString();
        String weekOfTerm = mWeekOfTermEditText.getText().toString();
        String teacher = mTeacherEditText.getText().toString();

        weekOfTerm = weekOfTermTrim(weekOfTerm);
        mWeekOfTermEditText.setText(weekOfTerm);

        if (name.isEmpty() || classroom.isEmpty() || teacher.isEmpty() || weekOfTerm.isEmpty() ||
                mDayOfWeek == 0 || mClassStart == 0 || mClassEnd == 0) {
            Toast.makeText(this, "内容不能为空", Toast.LENGTH_SHORT).show();
            return false;
        }

        int m = checkWeekOfTermFormat(weekOfTerm);
        switch (m) {
            case 0:
                Toast.makeText(this,
                        "请检查周数是否按照格式填写", Toast.LENGTH_LONG).show();
                return false;
            case -1:
                Toast.makeText(this,
                        "存在周数在区间[1,25]外", Toast.LENGTH_LONG).show();
                return false;
            case -2:
                Toast.makeText(this,
                        "周数存在重复", Toast.LENGTH_LONG).show();
                return false;
            default:
                break;
        }


        mCourse.setName(name);
        mCourse.setClassRoom(classroom);
        mCourse.setClassStart(mClassStart);
        mCourse.setDayOfWeek(mDayOfWeek);
        mCourse.setClassLength(mClassEnd - mClassStart + 1);
        mCourse.setWeekOfTerm(weekOfTerm);
        mCourse.setWeekOptions(mSpinner.getSelectedItem().toString());
        mCourse.setTeacher(teacher);
        return true;
    }

    /**
     * 去除字符串首尾的 ","
     *
     * @param text 需处理的字符串
     * @return 处理后字符串
     */
    private String weekOfTermTrim(String text) {
        int len = text.length();
        int i = len - 1;
        int j = 0;
        for (; i >= 0; i--) {
            if (text.charAt(i) != ',') {
                break;
            }

        }
        i++;
        if (i != 0) {
            for (; j < i; j++) {
                if (text.charAt(j) != ',')
                    break;
            }
        }
        return text.substring(j, i);
    }

    /**
     * @param text
     * @return int
     * 当值为1时，格式正确
     * 当值为0时，格式不正确，不详细说明
     * 当值为-1时，周数值在[1,25]外
     * 当值为-2时，周数存在覆盖
     */
    private int checkWeekOfTermFormat(String text) {
        String[] strings = text.split(",");

        if (strings.length == 0)
            return 0;
        boolean[] flags = new boolean[25];

        for (int i = 0; i < flags.length; i++)
            flags[i] = false;

        for (String s : strings) {
            if (s.isEmpty()) {
                return 0;
            } else {
                int index = s.indexOf('-');
                if (index == -1) {
                    int v = Integer.parseInt(s);
                    if (checkNum(v)) {
                        if (flags[v - 1])
                            return -2;
                        else
                            flags[v - 1] = true;
                    } else {
                        return -1;
                    }
                } else if (index == 0) {
                    return 0;
                } else {
                    int len = s.length();
                    if (index == len - 1) {
                        return 0;
                    } else {
                        String s1 = s.substring(0, index);
                        String s2 = s.substring(index + 1);

                        int a1 = Integer.parseInt(s1);
                        int a2 = Integer.parseInt(s2);

                        if (checkNum(a1) && checkNum(a2) && a1 < a2) {
                            for (int i = a1; i <= a2; i++) {
                                if (flags[i - 1])
                                    return -2;
                                else
                                    flags[i - 1] = true;
                            }
                        } else {
                            return -1;
                        }
                    }
                }
            }
        }
        return 1;
    }

    /**
     * @param v 周数
     * @return 是否在[1, 25]区间内
     */
    private boolean checkNum(int v) {
        return (v > 0 && v <= 25);
    }

    /**
     * 保存课程信息到本地文件
     */
    private void saveCourse() {
        if (setCourseFromView()) {
            if (mIndex == -1) {
                MainActivity.sCourseList.add(getInsertIndex(), mCourse);
            } else {
                MainActivity.sCourseList.set(mIndex, mCourse);
            }
            FileUtils.saveToJson(MainActivity.sCourseList, this);
            setUpdateResult();
            Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * @return 得到新增课程应该插入到List的位置
     * 按照一周中课程开始时间排序
     */
    private int getInsertIndex() {//按上课顺序插入
        List<Course> courseList = MainActivity.sCourseList;
        int dayOfWeek = mCourse.getDayOfWeek();
        int class_start = mCourse.getClassStart();
        int size = courseList.size();
        int i;
        for (i = 0; i < size; i++) {
            Course course = courseList.get(i);
            if (dayOfWeek == course.getDayOfWeek() && class_start < course.getClassStart())
                break;
        }
        return i;
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
     * 初始化ActionBar
     */
    private void setActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.course_edit);
    }

    /**
     * 设置TextView的初始值
     */
    private void setDefaultValue() {
        mTeacherEditText.setText(mCourse.getTeacher());
        mNameEditText.setText(mCourse.getName());
        String string = mCourse.getWeekOptions();

        if (string.equals("周"))
            mSpinner.setSelection(0);
        else if (string.equals("单周"))
            mSpinner.setSelection(1);
        else
            mSpinner.setSelection(2);

        mWeekOfTermEditText.setText(mCourse.getWeekOfTerm());
        mClassRoomEditText.setText(mCourse.getClassRoom());

        int class_start = mCourse.getClassStart();
        int class_end = class_start + mCourse.getClassLength() - 1;
        String week = WEEK_ARRAY[mCourse.getDayOfWeek() - 1];

        mClassNumTextView.setText(
                String.format(getString(R.string.schedule_section), week, class_start, class_end));
    }

    /**
     * 菜单栏
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();
        if (id == android.R.id.home) {
            final AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setTitle("提示")
                    .setMessage("确定要退出吗?")
                    .create();

            alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "确定",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //在退出之前结束dialog,再退出,否则退出会很慢
                            alertDialog.dismiss();
                            finish();

                        }
                    });

            alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            alertDialog.dismiss();
                        }
                    });

            alertDialog.show();

        } else if (id == R.id.menu_save) {
            //TODO 保存课表信息
            if (setCourseFromView()) {
                final AlertDialog alertDialog = initAlertDialog("提示", "是否保存内容?", "取消");
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "确定",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                saveCourse();

                            }
                        });
                alertDialog.show();
            }
        }

//        boolean Return false to allow normal menu processing to  proceed, true to consume it here.
        return super.onOptionsItemSelected(item);
    }

    private AlertDialog initAlertDialog(String title, String message, String cancleBtnText) {
        final AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .create();

        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, cancleBtnText,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        alertDialog.dismiss();
                    }
                });
        return alertDialog;
    }

    /**
     * 初始化节数选择对话框的星期，开始节数，节数节数列表
     */
    private void setData() {
        weekItems = new ArrayList<>();

        weekItems.add("周一");
        weekItems.add("周二");
        weekItems.add("周三");
        weekItems.add("周四");
        weekItems.add("周五");
        weekItems.add("周六");
        weekItems.add("周日");

        startItems = new ArrayList<>();
        endItems = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("到");
        for (int i = 1; i <= 12; i++) {
            stringBuilder.append(i);
            startItems.add(String.valueOf(i));
            endItems.add(stringBuilder.toString());
            stringBuilder.delete(1, stringBuilder.length());

        }
    }

    /**
     * 初始化选择对话框
     *
     * @param options1
     * @param options2
     * @param options3
     */

    private void initOptionsPicker(int options1, int options2, int options3) {

        mClassStart = options1 + 1;
        mClassEnd = options2 + 1;
        mDayOfWeek = options3 + 1;

        final String str = "%s %d-%d节";
        pvOptions = new OptionsPickerBuilder(this, new OnOptionsSelectListener() {

            @Override
            public void onOptionsSelect(int options1, int options2, int options3, View v) {
                //返回的分别是三个级别的选中位置
                String str = weekItems.get(options1) + " " + (options2 + 1) + "-" + (options3 + 1) + "节";
                mClassNumTextView.setText(str);
                //保存节数信息
                mDayOfWeek = options1 + 1;
                mClassStart = options2 + 1;
                mClassEnd = options3 + 1;

            }
        })
                .setOptionsSelectChangeListener(new OnOptionsSelectChangeListener() {
                    @Override
                    public void onOptionsSelectChanged(int options1, int options2, int options3) {
                        String str = weekItems.get(options1) + " " + (options2 + 1) + "-" + (options3 + 1) + "节";

                        pvOptions.setTitleText(str);
                        if (options3 < options2) {
                            pvOptions.setSelectOptions(options1, options2, options2 + 1);
                        }
                    }
                })
                .build();
        if (pvOptions != null) {
            pvOptions.setNPicker(weekItems, startItems, endItems);
            pvOptions.setSelectOptions(options1, options2, options3);
            pvOptions.setTitleText("选择上课节数");
            pvOptions.show();
        }

    }
}
