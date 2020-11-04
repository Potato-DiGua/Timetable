package com.potato.timetable.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;

import androidx.cardview.widget.CardView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

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
            "https://api.github.com/repos/Potato-DiGua/Timetable/releases/latest";

    private static final String BASE_URL = "https://raw.githubusercontent.com/Potato-DiGua/Timetable/master/app/release/";
    private static Bitmap bgBitmap = null;

    public static final int SINGLE_DOUBLE_WEEK = 0;
    public static final int SINGLE_WEEK = 1;
    public static final int DOUBLE_WEEK = 2;
    public static final String[] WEEK_OPTIONS = new String[]{"周", "单周", "双周"};


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

    public static String getDate() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmm", Locale.CHINA);
        return simpleDateFormat.format(new Date());
    }

    /**
     * 计算1970年1月4号0时0分0秒(周一)至今有多少周
     * 用于更新周数，用一年周数的话跨年会产生问题
     *
     * @return
     */
    public static long getWeekNum() {
        //System.currentTimeMillis()返回的是1970年1月4号0时0分0秒距今多少毫秒
        long day = System.currentTimeMillis() / (1000 * 60 * 60 * 24) - 4;//减四为1月4日距今多少天
        //Log.d("weeknum",String.valueOf(day/7+1));
        return day / 7 + 1;
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
     * 获取app版本名称以供更新
     *
     * @param context
     * @return app版本名称（1.0.3）
     */
    public static String getLocalVersionCode(Context context) {
        String versionName = "";
        try {
            PackageInfo packageInfo = context.getApplicationContext()
                    .getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);

            versionName = packageInfo.versionName;

            Log.d("TAG", "当前版本号：" + versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }

    /**
     * 检查是否有新版本
     *
     * @param versionName 版本名称
     * @return 新版本的下载地址
     */
    public static String checkUpdate(String versionName) {
        String json = OkHttpUtils.downloadText(UPDATE_URL);
        if (!json.isEmpty()) {
            try {
                JsonElement jsonElement = new JsonParser().parse(json);
                if (jsonElement != null) {
                    jsonElement = jsonElement.getAsJsonObject().get("assets");
                    if (jsonElement != null) {
                        JsonArray jsonArray = jsonElement.getAsJsonArray();
                        if (jsonArray.size() > 0) {
                            jsonElement = jsonArray.get(0).getAsJsonObject().get("browser_download_url");
                            if (jsonElement != null) {
                                String url = jsonElement.getAsString();
                                String remoteVersionName = getVersionNameFromUrl(url);
                                Log.d("TAG", "远程仓库版本号：" + remoteVersionName);
                                if (compareVersionName(remoteVersionName, versionName) > 0) {
                                    return url;
                                }
                            }
                        }
                    }
                }
            } catch (JsonSyntaxException | IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
        return "";

    }

    /**
     * 获取版本名称(例如1.0.3)
     * 从https://github.com/Potato-DiGua/Timetable/releases/download/v1.0.3/LightTimetable-v1.0.3.apk中
     * 提取 1.0.3
     *
     * @param url 网址
     * @return 版本名称
     */
    public static String getVersionNameFromUrl(String url) {
        if (!url.isEmpty()) {
            //
            String anchor = "LightTimetable-v";
            int index = url.indexOf(anchor);
            int apkIndex = url.indexOf(".apk");
            if (index != -1 && apkIndex != -1) {
                return url.substring(index + anchor.length(), apkIndex);
            }
        }
        return "";
    }

    /**
     * 比较版本号
     *
     * @param a
     * @param b
     * @return
     * @throws IllegalArgumentException
     */
    public static int compareVersionName(String a, String b) throws IllegalArgumentException {
        String[] strings1 = a.split("\\.");
        String[] strings2 = b.split("\\.");
        if (strings1.length == 3 && strings2.length == 3) {
            try {
                for (int i = 0; i < 3; i++) {
                    int compare = Integer.parseInt(strings1[i]) - Integer.parseInt(strings2[i]);
                    if (compare != 0) {
                        return compare;
                    }
                }
                return 0;
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("无效的版本名称参数");
            }
        } else {
            throw new IllegalArgumentException("无效的版本名称参数");
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
     *
     * @param weekOfTerm
     * @return
     */
    public static int getWeekOptionFromWeekOfTerm(int weekOfTerm) {
        int singleWeek = 0x55555555;//二进制:0101,0101,0101,0101,0101,0101,0101,0101
        int doubleWeek = 0xaaaaaaaa;//二进制:1010,1010,1010,1010,1010,1010,1010,1010

        //如果总周数是偶数则互换，保证算法的正确性
        if (Config.getMaxWeekNum() % 2 == 0) {
            int temp = singleWeek;
            singleWeek = doubleWeek;
            doubleWeek = temp;
        }
        //快速判断是否有单周或者双周
        boolean hasSingleWeek = ((singleWeek & weekOfTerm) != 0);
        boolean hasDoubleWeek = ((doubleWeek & weekOfTerm) != 0);
        if (hasSingleWeek && hasDoubleWeek) {
            return SINGLE_DOUBLE_WEEK;
        } else if (hasSingleWeek) {
            return SINGLE_WEEK;
        } else if (hasDoubleWeek) {
            return DOUBLE_WEEK;
        } else {
            return -1;
        }

    }

    /**
     * @param weekOfTerm
     * @return 获取格式为"1-9,19,20-25 [周]"的周数
     */
    public static String getFormatStringFromWeekOfTerm(int weekOfTerm) {
        return getStringFromWeekOfTerm(weekOfTerm) +
                " [" +
                WEEK_OPTIONS[getWeekOptionFromWeekOfTerm(weekOfTerm)] +
                "]";
    }

    /**
     * 生成1-18,19,25格式的周数
     *
     * @param weekOfTerm
     * @return
     */
    public static String getStringFromWeekOfTerm(int weekOfTerm) {
        if (weekOfTerm == 0)
            return "";
        StringBuilder stringBuilder = new StringBuilder();

        int weekOptions = getWeekOptionFromWeekOfTerm(weekOfTerm);

        boolean week[] = new boolean[Config.getMaxWeekNum()];

        for (int i = Config.getMaxWeekNum() - 1; i >= 0; i--) {
            week[i] = ((weekOfTerm & 0x01) == 1);
            weekOfTerm = weekOfTerm >> 1;
        }
        String weekOptionsStr = "";

        int start = 1;
        int space = 2;

        switch (weekOptions) {
            case SINGLE_DOUBLE_WEEK:
                space = 1;
                break;
            case SINGLE_WEEK:
                break;
            case DOUBLE_WEEK:
                start = 2;
                break;
            default:
                return "error";

        }
        int count = 0;
        for (int i = start; i <= Config.getMaxWeekNum(); i += space) {
            if (week[i - 1]) {
                if (count == 0) {
                    stringBuilder.append(i);
                }
                count += 1;
            } else {
                if (count == 1) {
                    stringBuilder.append(',');
                } else if (count > 1) {
                    stringBuilder.append('-');
                    stringBuilder.append(i - space);
                    stringBuilder.append(',');
                }
                count = 0;
            }
        }
        if (count > 1) {
            stringBuilder.append('-');
            int max = Config.getMaxWeekNum();
            if (start == 1 && max % 2 == 0) {//单周
                max--;
            } else if (start == 2 && max % 2 == 1) {//双周
                max--;
            }
            stringBuilder.append(max);
        }
        int len = stringBuilder.length() - 1;
        if (stringBuilder.charAt(len) == ',')
            stringBuilder.deleteCharAt(len);

        return stringBuilder.toString();
    }

    public static String formatTime(int time) {
        return time < 10 ? "0" + time : String.valueOf(time);
    }

    /**
     * 获取星期
     *
     * @return 返回1-7代表周几 周日为1
     */
    public static int getWeekOfDay() {

        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.DAY_OF_WEEK);
    }
}
