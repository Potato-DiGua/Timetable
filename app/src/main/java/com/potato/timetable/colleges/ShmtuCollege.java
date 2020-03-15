package com.potato.timetable.colleges;

import android.app.DownloadManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.service.autofill.FieldClassification;
import android.text.TextUtils;
import android.util.Log;

import com.potato.timetable.bean.Course;
import com.potato.timetable.colleges.base.College;
import com.potato.timetable.util.Config;
import com.potato.timetable.util.OkHttpUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.OutputKeys;

import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.Response;

public class ShmtuCollege implements College {
    private static final String BASE_URL = "https://cas.shmtu.edu.cn";
    private static final String LOGIN_URL = BASE_URL + "/cas/login";
    private static final String RANDOM_IMG_URL = BASE_URL + "/cas/captcha";
    private Map<String, String> termMap = new HashMap<>();
    private Context mContext;
    private boolean isLogin = false;

    public ShmtuCollege(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public String getCollegeName() {
        return "上海海事大学";
    }

    @Override
    public boolean login(String account, String pw, String RandomCode) {
        if(isLogin())
            return true;
        String result = OkHttpUtils.toString(OkHttpUtils.downloadRaw(LOGIN_URL));
        Document doc = Jsoup.parse(result);
        Element e = doc.getElementById("fm1");
        String execution="";
        if(e!=null&&e.children().size()>6){
            execution = e.child(5).attr("value");
            if(TextUtils.isEmpty(execution))
                return false;
        }

        FormBody formBody = new FormBody.Builder()
                .add("username", account)//账号
                .add("password", pw)//密码
                .add("validateCode", RandomCode)//验证码
                .add("execution", execution)
                .add("_eventId", "submit")
                .add("geolocation", "")
                .build();

        Request request = new Request.Builder()
                .url(LOGIN_URL)
                .addHeader("Accept-Language", "zh-CN,zh;q=0.9")
                .post(formBody)
                .build();

        result = OkHttpUtils.toString(OkHttpUtils.downloadRaw(request));
        doc = Jsoup.parse(result);
        Elements elements = doc.getElementsByClass("alert-success");
        if (elements.size() > 0) {
            Element div = elements.first();
            if (div.children().size() > 0) {
                if (div.child(0).text().equals("登录成功")) {
                    String url = "https://portal.shmtu.edu.cn/";
                    redirect(new Request.Builder().url(url).build());
                    isLogin = true;
                    return true;
                }
            }
        }
        return false;
    }

    private void redirect(Request request) {
        try {
            Response response = OkHttpUtils.getOkHttpClient()
                    .newCall(request)
                    .execute();
            //手动重定向，自动重定向cookie保存会出现问题
            while (response.code() == 302) {
                String location = response.header("Location");
                //Log.d("location",location);
                response = OkHttpUtils.getOkHttpClient()
                        .newCall(new Request.Builder().url(location).build())
                        .execute();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Course> getCourses(String term) {
        String id = termMap.get(term);
        if (TextUtils.isEmpty(id))
            return null;
        Log.d("courses", id);
        FormBody form = new FormBody.Builder()
                .add("ignoreHead", "1")
                .add("setting.kind", "std")
                .add("startWeek", "1")
                .add("semester.id", id)
                .add("ids", "380336")
                .build();
        Request request = new Request.Builder()
                .url("https://jwxt.shmtu.edu.cn/shmtu/courseTableForStd!courseTable.action")
                .post(form)
                .build();
        String result = OkHttpUtils.toString(OkHttpUtils.downloadRaw(request));
        Document doc = Jsoup.parse(result);
        Element e = doc.getElementsByTag("script").last();
        String script = e.html();
        String info = script.split("var fakeCourses = \\[\\];")[1]
                .split("function containFakeCourse")[0]
                .replaceAll("[\\s\\u00A0]", "");//去除&nbsp;和空格

        List<Course> courseList = new ArrayList<>();

        boolean flag = false;//用于标记是否新建课程
        for (String s : info.split(";")) {
            Log.d("courses", s);
            if (s.startsWith("activity")) {
                //需要的解析字符串
                //activity = new TaskActivity("9905","戴峰","1679(XX210270_001)","微波技术与天线(XX210270_001)","5362","教学3B503","00000000011111111000000000000000000000000000000000000");
                Course course = new Course();
                String[] courseInfo = s.split("\",\"");
                course.setTeacher(courseInfo[1]);
                course.setName(courseInfo[3].substring(0, courseInfo[3].indexOf('(')));
                course.setClassRoom(courseInfo[5]);
                course.setWeekOfTerm(Integer.parseInt(courseInfo[6].substring(1, Config.getMaxWeekNum()+1), 2));//二进制转十进制
                flag = true;
                Log.d("courses", course.getTeacher() + courseInfo[6]);
                courseList.add(course);
            } else if (s.startsWith("index")) {
                Course course = courseList.get(courseList.size() - 1);
                if (flag) {//设置周几和开始上课的节数
                    Matcher m = Pattern.compile("index=(\\d+)\\*unitCount\\+(\\d+)").matcher(s);
                    if (m.find()) {
                        String str = m.group(1);
                        if (!TextUtils.isEmpty(str)) {
                            course.setDayOfWeek(Integer.parseInt(str) + 1);
                        }
                        str = m.group(2);
                        if (!TextUtils.isEmpty(str)) {
                            int classStart = Integer.parseInt(str) + 1;
                            course.setClassStart(classStart >= 7 ? classStart - 2 : classStart);
                        }
                        course.setClassLength(1);
                        flag = false;
                    }
                } else {
                    course.setClassLength(course.getClassLength() + 1);
                }
            }
        }
        for (Course course : courseList) {
            Log.d("course", course.getName() + ",周" + course.getDayOfWeek() + course.getClassStart() + "-" + course.getClassLength());
        }
        return courseList;
    }

    @Override
    public Bitmap getRandomCodeImg(String dirPath) {
        String name = "random.png";
        if (OkHttpUtils.downloadToLocal(RANDOM_IMG_URL, dirPath, name)) {
            return BitmapFactory.decodeFile(dirPath + File.separator + name);
        } else {
            return null;
        }
    }

    @Override
    public String[] getTermOptions() {
        if (termMap.size() > 0) {
            String[] strings = new String[termMap.size()];
            int i = 0;
            for (String key : termMap.keySet()) {
                strings[i++] = key;
            }
            return strings;
        }

        redirect(new Request.Builder().url("https://jwxt.shmtu.edu.cn/shmtu/home.action").build());
        String url = "https://jwxt.shmtu.edu.cn/shmtu/dataQuery.action";
        FormBody form = new FormBody.Builder()
                .add("tagId", "semesterBar1001572775Semester")
                .add("dataType", "semesterCalendar")
                .add("value", "215")
                .add("empty", "false")
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(form)
                .build();
        String result = OkHttpUtils.toString(OkHttpUtils.downloadRaw(request));
        Matcher matcher = Pattern.compile("\\{id:(\\d+),schoolYear:\"([0-9\\-]+)\",name:\"([12])\"\\}").matcher(result);
        List<String> list = new LinkedList<>();
        while (matcher.find()) {
            Log.d("term", matcher.group(0));
            String term = matcher.group(2) + "-" + matcher.group(3);
            termMap.put(term, matcher.group(1));
            list.add(term);
        }
        Collections.reverse(list);//反序
        return list.toArray(new String[0]);
    }

    @Override
    public boolean isLogin() {
        Request request = new Request.Builder()
                .url(LOGIN_URL)
                .addHeader("Accept-Language", "zh-CN,zh;q=0.9")
                .build();
        String result = OkHttpUtils.toString(OkHttpUtils.downloadRaw(request));
        Document doc = Jsoup.parse(result);
        Elements elements = doc.getElementsByClass("alert-success");
        if (elements.size() > 0) {
            Element div = elements.first();
            if (div.children().size() > 0) {
                if (div.child(0).text().equals("登录成功")) {
                    isLogin=true;
                    return true;
                }
            }
        }
        return false;
    }
}
