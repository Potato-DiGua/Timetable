package com.potato.timetable.colleges.base;

import android.graphics.Bitmap;

import com.potato.timetable.bean.Course;

import java.util.List;

public interface College {
    /**
     * 获取学校名字
     *
     * @return
     */
    String getCollegeName();

    /**
     * 登录
     *
     * @param account
     * @param pw
     * @param RandomCode 可为NULL，根据实际情况填写
     * @return 返回是否登录成功
     */
    boolean login(String account, String pw, String RandomCode);

    /**
     * 获取课程
     *
     * @param term 课程学期
     * @return
     */
    List<Course> getCourses(String term);

    /**
     * 获取验证码的BitMap
     *
     * @param dirPath 验证码图片保存路径
     * @return
     */
    Bitmap getRandomCodeImg(String dirPath);

    /**
     * 获取课程学期选项
     *
     * @return
     */
    String[] getTermOptions();
    boolean isLogin();
}
