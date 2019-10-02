package com.potato.timetable.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.potato.timetable.R;
import com.potato.timetable.bean.Course;
import com.potato.timetable.util.Config;
import com.potato.timetable.util.FileUtils;
import com.potato.timetable.util.Utils;

public class CourseDetailsActivity extends AppCompatActivity {
    public static final String KEY_COURSE_INDEX="course_index";
    private static final String[] aStrWeek =new String[]{
            "周一","周二","周三","周四","周五","周六","周日"
    };
    public static final int EDIT_ID=0;
    private int index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_details);
        Button button=findViewById(R.id.btn_edit);

        setActionBar();

        setCourseTextView();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(CourseDetailsActivity.this,EditActivity.class);
                intent.putExtra(EditActivity.KEY_COURSE_INDEX,index);
                startActivityForResult(intent,EDIT_ID);
            }
        });

        ImageView imageView=findViewById(R.id.iv_bg);
        setCardViewAlpha();

        Utils.setBackGround(imageView);

    }
    private void setCardViewAlpha()
    {
        float alpha= Config.getCardViewAlpha();
        CardView cardView=findViewById(R.id.cv_course_details);
        cardView.setAlpha(alpha);
    }

    private void setUpdateResult()
    {
        Intent intent=new Intent();
        intent.putExtra(EditActivity.EXTRA_UPDATE_TIMETABLE,true);
        setResult(RESULT_OK,intent);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_course_details,menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==EDIT_ID)
        {
            if(data!=null)
            {
                setResult(RESULT_OK,data);//回传给MainActivity,让其更新课表
            }

        }
    }

    private void setCourseTextView()
    {
        index=getIntent().getIntExtra(KEY_COURSE_INDEX,0);

        Course course =MainActivity.sCourseList.get(index);
        TextView textView=findViewById(R.id.tv_class_name);
        textView.setText(course.getName());

        textView=findViewById(R.id.tv_class_room);

        textView.setText(course.getClassRoom());

        textView=findViewById(R.id.tv_class_num);

        int class_start= course.getClassStart();
        int class_num= course.getClassLength();
        textView.setText(String.format(getString(R.string.schedule_section),
                aStrWeek[course.getDayOfWeek()-1],class_start,(class_start+class_num-1)));

        textView=findViewById(R.id.tv_week_of_term);
        textView.setText(String.format(getString(R.string.week_num_format),
                course.getWeekOfTerm(), course.getWeekOptions()));

        textView=findViewById(R.id.tv_teacher);
        textView.setText(course.getTeacher());
    }
    private void setActionBar()
    {
        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.course_details);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case android.R.id.home:
                finish();
                break;
            case R.id.menu_delete:
                final AlertDialog alertDialog=new AlertDialog.Builder(this)
                        .setTitle("提示")
                        .setMessage("您确定要删除该课程吗?")
                        .create();
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        MainActivity.sCourseList.remove(index);
                        FileUtils.saveToJson(MainActivity.sCourseList,
                                CourseDetailsActivity.this);
                        Toast.makeText(CourseDetailsActivity.this,"成功删除",Toast.LENGTH_SHORT).show();
                        setUpdateResult();
                        alertDialog.dismiss();
                        finish();

                    }
                });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                            alertDialog.dismiss();
                    }
                });
                alertDialog.show();
                default:
                    break;
        }
        return super.onOptionsItemSelected(item);
    }
}
