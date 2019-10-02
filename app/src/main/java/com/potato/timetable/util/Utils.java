package com.potato.timetable.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;

import com.alibaba.fastjson.JSON;
import com.potato.timetable.bean.Version;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 工具类：
 * 设置背景
 *
 */
public class Utils {

    private static String PATH;
    private static final String BG_NAME="bg.jpg";
    private static final String UPDATE_URL=
            "https://raw.githubusercontent.com/Potato-DiGua/Timetable/master/app/release/version.json";

    private static final String BASE_URL="https://raw.githubusercontent.com/Potato-DiGua/Timetable/master/app/release/";
    public static void setPATH(String PATH) {
        Utils.PATH = PATH;
    }

    public static void setBackGround(ImageView imageView)
    {
        setBackGround(imageView, Config.getBgId());
    }
    public static void setBackGround(ImageView imageView, int id)
    {
        if(id==0)
        {
            File file=new File(PATH,BG_NAME);
            if(file.exists())
            {
                Bitmap bitmap= BitmapFactory.decodeFile(file.getAbsolutePath());
                imageView.setImageBitmap(bitmap);
            }
        }
        else
        {
            imageView.setImageResource(id);
        }
    }
    public static long getLocalVersionCode(Context context)
    {
        long localVersion = 0;
        try {
            PackageInfo packageInfo = context.getApplicationContext()
                    .getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            localVersion = packageInfo.versionCode;
            Log.d("TAG", "当前版本号：" + localVersion);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return localVersion;
    }
    public static String checkUpdate(long versionCode)
    {
        BufferedReader bis=null;
        try {
            URL url=new URL(UPDATE_URL);
            HttpURLConnection httpURLConnection=(HttpURLConnection)url.openConnection();

            bis=new BufferedReader(
                    new InputStreamReader(httpURLConnection.getInputStream()));
            StringBuilder stringBuilder=new StringBuilder();
            String line;
            while ((line=bis.readLine())!=null)
            {
                stringBuilder.append(line);
            }
            Version version=JSON.parseObject(stringBuilder.toString(),Version.class);
            Log.d("version",version.getVersionCode()+"");

            if(version.getVersionCode()>versionCode)
            {
                Log.d("update",version.getVersionCode()+"|"+versionCode);
                return BASE_URL+version.getReleaseName();
            }
            else
            {
                Log.d("update","最新版");
                return "";
            }

        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if(bis!=null)
                    bis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return "";
    }
}
