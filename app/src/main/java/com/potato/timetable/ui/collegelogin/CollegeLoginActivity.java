package com.potato.timetable.ui.collegelogin;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.potato.timetable.R;
import com.potato.timetable.httpservice.CollegeService;
import com.potato.timetable.util.Config;
import com.potato.timetable.util.RetrofitUtils;
import com.potato.timetable.util.Utils;

public class CollegeLoginActivity extends AppCompatActivity implements ItemFragment.OnListFragmentInteractionListener {

    private FragmentManager fragmentManager;
    private CollegeLoginFragment collegeLoginFragment = null;
    private ItemFragment itemFragment = null;
    private final CollegeService collegeService = RetrofitUtils.getRetrofit().create(CollegeService.class);

    public static final String EXTRA_UPDATE_TIMETABLE = "update_timetable";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_college_login);

        init();

        fragmentManager = getSupportFragmentManager();//初始化管理者
        String name = Config.getCollegeName();
        if (!name.isEmpty()) {
            fragmentManager.beginTransaction()
                    .add(R.id.fragment_container, getCollegeLoginFragment())
                    .commit();

        } else {
            //选择学校
            fragmentManager.beginTransaction()
                    .add(R.id.fragment_container, getItemFragment())
                    .commit();
        }
    }

    public CollegeLoginFragment getCollegeLoginFragment() {
        if (collegeLoginFragment == null) {
            collegeLoginFragment = new CollegeLoginFragment();
        }
        return collegeLoginFragment;
    }

    public ItemFragment getItemFragment() {
        if (itemFragment == null) {
            itemFragment = ItemFragment.newInstance(1);
        }
        return itemFragment;
    }

    private void init() {
        setActionBar();
        ImageView bgIv = findViewById(R.id.iv_bg);
        Utils.setBackGround(this, bgIv);

        Config.readSelectCollege(this);

    }

    private void setActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.title_activity_login);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        } else if (id == R.id.menu_select_college) {
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, getItemFragment())
                    .commit();
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onListFragmentInteraction(String item) {
        Config.setCollegeName(item);
        Config.saveSelectCollege(this);

        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, getCollegeLoginFragment())
                .commit();

    }
}
