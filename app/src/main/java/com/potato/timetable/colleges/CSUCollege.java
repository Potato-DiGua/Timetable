package com.potato.timetable.colleges;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.potato.timetable.bean.Course;
import com.potato.timetable.colleges.base.College;
import com.potato.timetable.util.OkHttpUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import okhttp3.FormBody;
import okhttp3.Request;


public class CSUCollege implements College {
    public static final String NAME = "中南大学";

    private static final String BASE_URL = "http://csujwc.its.csu.edu.cn";
    private static final String SESS_URL = BASE_URL + "/Logon.do?method=logon&flag=sess";
    private static final String LOGIN_URL = BASE_URL + "/Logon.do?method=logon";
    private static final String RANDOM_CODE_URL = BASE_URL + "/verifycode.servlet";
    private static final String INDEX_URL = BASE_URL + "/jsxsd/framework/xsMain.jsp";
    // 课程表json
    private static final String TIMETABLE_JSON_URL = BASE_URL + "/jsxsd/kbxx/getKbxx.do";
    // 我的课表
    private static final String TERMS_URL = BASE_URL + "/jsxsd/xskb/xskb_list.do?Ves632DSdyV=NEW_XSD_WDKB";


    @Override
    public String getCollegeName() {
        return NAME;
    }

    @Override
    public boolean login(String account, String pw, String RandomCode) {
        String encoded = encode(account, pw);
        //String data = "view=0&useDogCode=&encoded=" + encoded + "&RANDOMCODE=" + RandomCode;

        FormBody form = new FormBody.Builder()
                .add("view", "0")
                .add("useDogCode", "")
                .add("encoded", encoded)
                .add("RANDOMCODE", RandomCode)
                .build();
        Request request = new Request.Builder()
                .url(LOGIN_URL)
                .post(form)
                .build();
        try {
            String result = OkHttpUtils.downloadText(request);
            if (!TextUtils.isEmpty(result)) {
                Document doc = Jsoup.parse(result);
                return doc.title().equals("学生个人中心");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean isLogin() {

        String result = OkHttpUtils.downloadText(INDEX_URL);
        if (!TextUtils.isEmpty(result)) {
            Document doc = Jsoup.parse(result);
            return doc.title().equals("学生个人中心");
        }
        return false;
    }

    private String encode(String account, String pw) {
        String result = OkHttpUtils.downloadText(SESS_URL);

        if (TextUtils.isEmpty(result))
            return "";
        String[] strings = result.split("#");
        String scode = strings[0];
        String sxh = strings[1];
        String code = account + "%%%" + pw;
        StringBuilder encoded = new StringBuilder();
        for (int i = 0; i < code.length(); i++) {
            if (i < 50) {
                encoded.append(code.charAt(i));
                int value = sxh.charAt(i) - '0';
                encoded.append(scode.substring(0, value));
                scode = scode.substring(value);
            } else {
                encoded.append(code.substring(i));
                i = code.length();
            }
        }
        return encoded.toString();
    }


    @Override
    public Bitmap getRandomCodeImg(String dirPath) {

        String name = "random.jpg";
        if (OkHttpUtils.downloadToLocal(RANDOM_CODE_URL, dirPath, name)) {
            return BitmapFactory.decodeFile(dirPath + File.separator + name);
        } else {
            return null;
        }
    }

    @Override
    public List<Course> getCourses(String term) {
        FormBody form = new FormBody.Builder()
                .add("xnxq01id", term)
                .add("zc", "")
                .build();
        Request request = new Request.Builder()
                .url(TIMETABLE_JSON_URL)
                .post(form)
                .build();
        String json = OkHttpUtils.downloadText(request);
        List<TimetableItem> list = new Gson().fromJson(json, new TypeToken<List<TimetableItem>>() {
        }.getType());
        List<Course> courseList = new ArrayList<>(list.size());
        for (TimetableItem item : list) {
            if (item.lesson > 6) {
                continue;
            }
            for (String content : item.title.split("\n\n")) {
                //课程名称：大学英语（一）\n上课教师：黄莹讲师（高校）\n周次：5-19(周)\n星期：星期一\n节次：0102节\n上课地点：外语网络楼449\n
                String[] lines = content.split("\n");
                if (lines.length != 6) {
                    continue;
                }
                Course course = new Course();
                course.setName(getValue(lines[0]));
                course.setTeacher(getValue(lines[1]));
                course.setClassRoom(getValue(lines[5]));
                course.setClassStart((item.lesson - 1) * 2 + 1);
                String str = getValue(lines[4]);// 0102节
                str = str.substring(0, str.length() - 1);//0102
                try {
                    int min = Integer.parseInt(str.substring(0, 2));
                    int max = Integer.parseInt(str.substring(str.length() - 2));
                    course.setClassLength(max - min + 1);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    course.setClassLength(2);
                }

                int week = item.DayOfWeek - 1;
                if (week == 0) {
                    week = 7;
                }
                course.setDayOfWeek(week);

                course.setWeekOfTerm(getWeekOfTermFromString(getValue(lines[2])));
                courseList.add(course);
            }
        }
        return courseList;

    }

    private int getWeekOfTermFromString(String text) {
        //Log.d("excel",text);
        String[] s1 = text.trim().split("\\(");
        String[] s11 = s1[0].split(",");

        int weekOfTerm = 0;
        for (String s : s11) {
            if (s == null || s.isEmpty())
                continue;
            if (s.contains("-")) {
                int space = 2;
                if (text.contains("(周)")) {
                    space = 1;
                }
                String[] s2 = s.split("-");
                if (s2.length != 2) {
                    return 0;
                }
                int min = Integer.parseInt(s2[0]);
                int max = Integer.parseInt(s2[1]);
                if (text.contains("单") && min % 2 == 0) {
                    min++;
                } else if (text.contains("双") && min % 2 == 1) {
                    min++;
                }

                for (int n = min; n <= max; n += space) {
                    weekOfTerm += 1 << (25 - n);
                }
            } else {
                weekOfTerm += 1 << (25 - Integer.parseInt(s));
            }
        }
        return weekOfTerm;
    }

    private String getValue(String line) {
        return line.substring(line.indexOf('：') + 1).trim();
    }

    /*        "jc": 1,
     "title": "课程名称：大学英语（一）\n上课教师：黄莹讲师（高校）\n周次：5-19(周)\n星期：星期一\n节次：0102节\n上课地点：外语网络楼449\n",
     "xq": 2,
     "kcmc": "大学英语（..."

     */
    private static class TimetableItem {
        /**
         * 第几节课
         * 值[1,7]
         * 7表示备注
         */
        @SerializedName("jc")
        private int lesson;
        /**
         * 星期几
         * 数值1-7
         * 1表示周日，依次类推
         */
        @SerializedName("xq")
        private int DayOfWeek;
        private String title;
        private String kcmc;

        public int getLesson() {
            return lesson;
        }

        public TimetableItem setLesson(int lesson) {
            this.lesson = lesson;
            return this;
        }

        public int getDayOfWeek() {
            return DayOfWeek;
        }

        public TimetableItem setDayOfWeek(int dayOfWeek) {
            DayOfWeek = dayOfWeek;
            return this;
        }

        public String getTitle() {
            return title;
        }

        public TimetableItem setTitle(String title) {
            this.title = title;
            return this;
        }

        public String getKcmc() {
            return kcmc;
        }

        public TimetableItem setKcmc(String kcmc) {
            this.kcmc = kcmc;
            return this;
        }
    }

    @Override
    public String[] getTermOptions() {
        List<String> termOptions = new LinkedList<>();
        try {
            String result = OkHttpUtils.downloadText(TERMS_URL);
            if (!TextUtils.isEmpty(result)) {
                Document doc = Jsoup.parse(result);
                Element e = doc.select("#xnxq01id").first();
                Elements es = e.children();
                int i = 0;
                for (Element element : es) {
                    termOptions.add(element.text().trim());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return termOptions.toArray(new String[0]);
    }

    @Override
    public boolean getFollowRedirects() {
        return true;
    }

    @Override
    public int getRandomCodeMaxLength() {
        return 4;
    }
}
