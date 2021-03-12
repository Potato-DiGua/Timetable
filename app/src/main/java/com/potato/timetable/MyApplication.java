package com.potato.timetable;

import android.app.Application;
import android.content.Context;

public class MyApplication extends Application {
    private static Application instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    /**
     * 用于工具类实例化，UI不能使用，使用后无法被回收，造成内存泄漏
     *
     * @return
     */
    public static Context getApplication() {
        return instance;
    }
}
