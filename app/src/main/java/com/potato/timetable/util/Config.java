package com.potato.timetable.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.potato.timetable.R;

public class Config {
    private static int currentWeek = 1;//表示当前周数
    private static boolean flagCurrentWeek = false;//利用该flag,进行周一的周数更新
    private static int bgId = R.drawable.bg_rem;//保存图片id,当等于零的时候，使用自定义图片,
    private static float cardViewAlpha = 1.0f;//卡片布局的透明度 值为0.0-1.0

    private static final String KEY_WEEK_NUM = "current_week";
    private static final String KEY_FLAG_WEEK = "flag_week";
    private static final String KEY_BG_ID = "bg_id";
    private static final String KEY_CARD_VIEW_ALPHA = "card_view_alpha";

    public static float getCardViewAlpha() {
        return cardViewAlpha;
    }
    public static void setCardViewAlpha(float cardViewAlpha) {
        if (cardViewAlpha > 1.0f)
            cardViewAlpha = 1.0f;
        else if (cardViewAlpha < 0.1f)
            cardViewAlpha = 0.1f;
        Config.cardViewAlpha = cardViewAlpha;
    }

    public static int getBgId() {
        return bgId;
    }

    public static void setBgId(int bgId) {
        Config.bgId = bgId;
    }

    public static int currentWeekAdd() {
        return ++currentWeek;
    }

    public static int getCurrentWeek() {
        return currentWeek;
    }

    public static void setCurrentWeek(int currentWeek) {
        Config.currentWeek = currentWeek;
    }

    public static boolean isFlagCurrentWeek() {
        return flagCurrentWeek;
    }

    public static void setFlagCurrentWeek(boolean flagCurrentWeek) {
        Config.flagCurrentWeek = flagCurrentWeek;
    }

    public static void saveSharedPreferences(final Context context) {

        SharedPreferences sharedPreferences =
                context.getSharedPreferences("data", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean(KEY_FLAG_WEEK, flagCurrentWeek);
        editor.putInt(KEY_WEEK_NUM, currentWeek);
        editor.putInt(KEY_BG_ID, bgId);
        editor.putFloat(KEY_CARD_VIEW_ALPHA, cardViewAlpha);

        editor.apply();


    }

    public static void readFormSharedPreferences(Context context) {
        //读取当前周
        SharedPreferences sharedPreferences = context.getSharedPreferences("data", Context.MODE_PRIVATE);

        currentWeek = sharedPreferences.getInt(KEY_WEEK_NUM, 1);
        flagCurrentWeek = sharedPreferences.getBoolean(KEY_FLAG_WEEK, false);//用于更新当前周数

        bgId = sharedPreferences.getInt(KEY_BG_ID, R.drawable.bg_rem);
        //Log.d("BgId",bgId+"");

        cardViewAlpha = sharedPreferences.getFloat(KEY_CARD_VIEW_ALPHA, 0.7f);
    }
}
