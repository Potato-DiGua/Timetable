package com.potato.timetable.ui.editcourse;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyboardShortcutGroup;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.potato.timetable.R;
import com.potato.timetable.util.Config;

import java.util.ArrayList;
import java.util.List;


public class WeekOfTermSelectDialog extends Dialog {
    private final List<Boolean> list = new ArrayList<>(Config.getMaxWeekNum());
    private final int mWeekOfTerm;
    private final Context mContext;
    private DialogAdapter dialogAdapter;
    private CheckBox selectAll;
    private CheckBox singleWeek;
    private CheckBox doubleWeek;
    private View.OnClickListener positiveListener;
    private View.OnClickListener nativeListener;


    public WeekOfTermSelectDialog(@NonNull Context context, int weekOfTerm) {
        super(context, R.style.CustomDialog);
        this.mWeekOfTerm = weekOfTerm;
        this.mContext = context;
    }

    /**
     * 设置dialog居下占满屏幕
     */
    private void changeDialogStyle() {
        Window window = getWindow();
        if (window != null) {
            WindowManager.LayoutParams attr = window.getAttributes();
            if (attr != null) {
                attr.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                attr.width = ViewGroup.LayoutParams.MATCH_PARENT;
                attr.gravity = Gravity.BOTTOM;
                window.setAttributes(attr);
            }
        }
    }

    public int getWeekOfTerm() {
        int weekOfTerm = 0;
        for (int i = 0, len = list.size(); i < len; i++) {
            if (list.get(i)) {
                //Log.d("weekofterm",String.valueOf(i));
                weekOfTerm++;
            }
            if (i != len - 1) {//最后不移动
                weekOfTerm = weekOfTerm << 1;
            }
        }
        return weekOfTerm;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_select_week_of_term);
        init();
        RecyclerView recyclerView = findViewById(R.id.rv_week_of_term);
        GridLayoutManager gridLayoutManager =
                new GridLayoutManager(mContext, 5);
        recyclerView.setLayoutManager(gridLayoutManager);

        dialogAdapter = new DialogAdapter(list);
        recyclerView.setAdapter(dialogAdapter);

        selectAll = findViewById(R.id.check_box_select_all);
        selectAll.setOnClickListener(view -> {
            boolean b = selectAll.isChecked();
            if (b) {
                singleWeek.setChecked(false);
                doubleWeek.setChecked(false);
            }
            for (int i = 0, len = list.size(); i < len; i++) {
                dialogAdapter.checkBoxList.get(i).setChecked(b);
            }
        });

        singleWeek = findViewById(R.id.check_box_single_week);
        singleWeek.setOnClickListener(view -> {
            if (singleWeek.isChecked()) {
                selectAll.setChecked(false);
                doubleWeek.setChecked(false);
                for (int i = 0, len = list.size(); i < len; i++) {
                    dialogAdapter.checkBoxList.get(i).setChecked((i + 1) % 2 == 1);
                }
            }

        });

        doubleWeek = findViewById(R.id.check_box_double_week);
        doubleWeek.setOnClickListener(view -> {
            if (doubleWeek.isChecked()) {
                singleWeek.setChecked(false);
                selectAll.setChecked(false);
                for (int i = 0, len = list.size(); i < len; i++) {
                    dialogAdapter.checkBoxList.get(i).setChecked((i + 1) % 2 == 0);
                }
            }
        });
        Button cancelBtn = findViewById(R.id.btn_cancel);
        Button yesBtn = findViewById(R.id.btn_yes);
        cancelBtn.setOnClickListener(nativeListener);
        yesBtn.setOnClickListener(positiveListener);

        changeDialogStyle();
    }

    public void setPositiveBtn(View.OnClickListener listener) {
        positiveListener = listener;
    }

    public void setNativeBtn(View.OnClickListener listener) {
        nativeListener = listener;
    }

    private void init() {
        if (mWeekOfTerm == -1) {
            for (int i = 0, len = Config.getMaxWeekNum(); i < len; i++) {
                list.add(false);
            }
        } else {
            for (int i = Config.getMaxWeekNum() - 1; i >= 0; i--) {
                list.add(((mWeekOfTerm >> i) & 0x01) == 1);
            }
        }

    }

    @Override
    public void onProvideKeyboardShortcuts(List<KeyboardShortcutGroup> data, @Nullable Menu menu, int deviceId) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    private static class DialogAdapter extends RecyclerView.Adapter<DialogAdapter.ViewHolder> {
        private final List<CheckBox> checkBoxList = new ArrayList<>();
        private final List<Boolean> resultList;

        public DialogAdapter(List<Boolean> resultList) {
            this.resultList = resultList;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.week_of_term_checkbox, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            checkBoxList.add(holder.checkBox);
            holder.checkBox.setChecked(resultList.get(position));
            holder.checkBox.setText(String.valueOf(position + 1));
            holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                    Log.d("checkbox", "发生改变");
                    resultList.set(position, b);
                }
            });
        }

        @Override
        public int getItemCount() {
            return resultList.size();
        }

        private static class ViewHolder extends RecyclerView.ViewHolder {
            public final CheckBox checkBox;

            public ViewHolder(View view) {
                super(view);
                checkBox = view.findViewById(R.id.checkBox);
            }

        }
    }
}
