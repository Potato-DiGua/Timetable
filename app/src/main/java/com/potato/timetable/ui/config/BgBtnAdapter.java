package com.potato.timetable.ui.config;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.potato.timetable.R;

import java.util.ArrayList;
import java.util.List;

public class BgBtnAdapter extends BaseAdapter {
    private final LayoutInflater mLayoutInflater;

    public List<Integer> bgIdList = new ArrayList<>();

    public BgBtnAdapter(Context context) {
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return bgIdList.size();
    }

    @Override
    public Object getItem(int i) {
        return bgIdList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = mLayoutInflater.inflate(R.layout.item_iv_bg, viewGroup, false);
            ImageView imageView = view.findViewById(R.id.item_iv);
            imageView.setImageResource(bgIdList.get(i));
        }
        return view;
    }
}
