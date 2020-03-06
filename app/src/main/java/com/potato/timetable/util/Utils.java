package com.potato.timetable.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.widget.ImageView;

import androidx.cardview.widget.CardView;

import com.google.gson.Gson;
import com.potato.timetable.bean.Version;

import java.io.File;

/**
 * 工具类：
 * 设置背景
 * <p>
 * 获取更新
 */
public class Utils {

    private static String PATH;
    private static final String BG_NAME = "bg.jpg";
    private static final String UPDATE_URL =
            "https://raw.githubusercontent.com/Potato-DiGua/Timetable/master/app/release/version.json";

    private static final String BASE_URL = "https://raw.githubusercontent.com/Potato-DiGua/Timetable/master/app/release/";
    private static Bitmap bgBitmap = null;

    public static final int SINGLE_DOUBLE_WEEK=0;
    public static final int SINGLE_WEEK=1;
    public static final int DOUBLE_WEEK=2;


    public static void setPATH(String PATH) {
        Utils.PATH = PATH;
    }

    /**
     * 设置背景图片
     *
     * @param context
     * @param imageView
     */
    public static void setBackGround(Context context, ImageView imageView) {
        setBackGround(context, imageView, Config.getBgId());
    }

    public static void setBackGround(Context context, ImageView imageView, int id) {

        if (bgBitmap == null) {
            refreshBg(context, id);
        }
        imageView.setImageBitmap(bgBitmap);

    }

    /**
     * 刷新背景
     *
     * @param context
     * @param id
     */
    public static void refreshBg(Context context, int id) {

        if (id == 0) {
            File file = new File(PATH, BG_NAME);
            if (file.exists()) {
                bgBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            }
        } else {
            bgBitmap = BitmapFactory.decodeResource(context.getResources(), id);
        }
    }


    /**
     * 获取app版本号以供更新
     *
     * @param context
     * @return
     */
    public static long getLocalVersionCode(Context context) {
        long localVersion = 0;
        try {
            PackageInfo packageInfo = context.getApplicationContext()
                    .getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                localVersion = packageInfo.getLongVersionCode();
            } else {
                localVersion = packageInfo.versionCode;
            }
            //Log.d("TAG", "当前版本号：" + localVersion);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return localVersion;
    }

    /**
     * 检查是否有新版本
     *
     * @param versionCode
     * @return
     */
    public static String checkUpdate(long versionCode) {
        Version version = new Gson().fromJson(HttpUtils.sendGet(UPDATE_URL), Version.class);
        //Log.d("update","最新版本号"+version.getVersionCode());

        if (version.getVersionCode() > versionCode) {
            return BASE_URL + version.getReleaseName();
        } else {
            //Log.d("update","最新版");
            return "";
        }

    }

    /**
     * 设置CardView透明度
     *
     * @param cardView
     */
    public static void setCardViewAlpha(CardView cardView) {
        cardView.setAlpha(Config.getCardViewAlpha());
    }

    /**
     * 判断是单周、双周、还是周
     * @param weekOfTerm
     * @return
     */
    public static int getWeekOptionFromWeekOfTerm(int weekOfTerm) {
        int singleWeek = 0x55555555;//二进制:0101,0101,0101,0101,0101,0101,0101,0101
        int doubleWeek = 0xaaaaaaaa;//二进制:1010,1010,1010,1010,1010,1010,1010,1010

        //如果总周数是偶数则互换，保证算法的正确性
        if (Config.getMaxWeekNum() % 2 == 0){
            int temp=singleWeek;
            singleWeek=doubleWeek;
            doubleWeek=temp;
        }
        //快速判断是否有单周或者双周
        boolean hasSingleWeek = ((singleWeek & weekOfTerm) != 0);
        boolean hasDoubleWeek = ((doubleWeek & weekOfTerm) != 0);
        if(hasSingleWeek&&hasDoubleWeek)
        {
            return SINGLE_DOUBLE_WEEK;
        }else if(hasSingleWeek){
            return SINGLE_WEEK;
        }else if(hasDoubleWeek){
            return DOUBLE_WEEK;
        }
        return -1;
    }

    /**
     * 生成1-18,19,25格式的周数
     * @param weekOfTerm
     * @return
     */
    public static String getStringFromWeekOfTerm(int weekOfTerm) {
        StringBuilder stringBuilder = new StringBuilder();

        int weekOptions= getWeekOptionFromWeekOfTerm(weekOfTerm);

        boolean week[] = new boolean[Config.getMaxWeekNum()];

        for (int i = Config.getMaxWeekNum() - 1; i >= 0; i--) {
            week[i] = ((weekOfTerm & 0x01) == 1);
            weekOfTerm = weekOfTerm >> 1;
        }
        String weekOptionsStr="";

        int start=0;
        int space=2;

        switch (weekOptions){
            case SINGLE_DOUBLE_WEEK:
                space=1;
                break;
            case SINGLE_WEEK:
                break;
            case DOUBLE_WEEK:
                start=1;
                break;
            default:
                return "error";

        }
        int count = 0;
        for (int i = start; i < Config.getMaxWeekNum(); i+=space) {
            if (week[i]) {
                if (count == 0) {
                    stringBuilder.append(i+1);
                }
                count += 1;
            } else {
                if(count==1) {
                    stringBuilder.append(',');
                }else if(count>1){
                    stringBuilder.append('-');
                    stringBuilder.append(i);
                    stringBuilder.append(',');
                }
                count=0;
            }
        }
        if(count>1)
        {
            stringBuilder.append('-');
            stringBuilder.append(Config.getMaxWeekNum());
        }
        int len=stringBuilder.length()-1;
        if(stringBuilder.charAt(len)==',')
            stringBuilder.deleteCharAt(len);

        return stringBuilder.toString();
    }
}
