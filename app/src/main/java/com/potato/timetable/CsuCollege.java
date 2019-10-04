package com.potato.timetable;

import android.util.Log;

import com.potato.timetable.util.HttpUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class CsuCollege {
    public static final String COLLEGE_NAME ="中南大学";
    public String mCookie;
    private static final String BASE_URL = "http://csujwc.its.csu.edu.cn";
    private static final String SESS_URL = BASE_URL + "/Logon.do?method=logon&flag=sess";
    private static final String LOGIN_URL = BASE_URL + "/Logon.do?method=logon";
    private static final String RANDOM_CODE_URL = BASE_URL + "/verifycode.servlet";

    private static final String TIMETABLE_EXCEL_URL =BASE_URL+"/jsxsd/xskb/xskb_print.do?xnxq01id=%s&zc=";

    /**
     * 获取cookie
     * @return boolean是否成功
     */
    public boolean getCookie() {
        mCookie = HttpUtils.getCookie(BASE_URL).replaceAll("(?i)path=/", "")
                .replaceAll("; *$", "");
        return !mCookie.isEmpty();
    }

    public boolean downloadRandomCodeImg(String path) {

        return HttpUtils.download(RANDOM_CODE_URL, mCookie, path);
    }

    public boolean downloadTimetableExcel(String term,String path)
    {
        String strUrl=String.format(TIMETABLE_EXCEL_URL,term);
        BufferedInputStream bi = null;
        BufferedOutputStream bo=null;
        BufferedWriter bw=null;
        String data="xnxq01id="+term+"&zc=";
        try {
            URL url = new URL(strUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
            connection.setRequestProperty("Content-Length",String.valueOf(data.getBytes().length));
            connection.setRequestProperty("Cookie", mCookie);
            // 设置通用的请求属性
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");

            connection.setDoInput(true);
            connection.setDoOutput(true);


            if (!data.isEmpty()) {
                bw=new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
                bw.write(data);
                bw.flush();
            }
            if (connection.getResponseCode()==200) {
                bi = new BufferedInputStream(new BufferedInputStream(connection.getInputStream()));

                bo=new BufferedOutputStream(new FileOutputStream(new File(path)));
                int len;
                byte[] buffer=new byte[1024];
                while ((len = bi.read(buffer,0,1024)) != -1) {
                    bo.write(buffer,0,len);
                }
                bo.flush();
                return true;

            } else
                return false;

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bi != null)
                    bi.close();
                if(bo!=null)
                    bo.close();
                if(bw!=null)
                    bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public String login(String account, String pw, String RandomCode) {
        String encoded = encode(account, pw);

        //进行x-www-form-urlencoded编码
        try {
            encoded=URLEncoder.encode(encoded,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String data = "view=0&useDogCode=&encoded=" + encoded + "&RANDOMCODE=" + RandomCode;

        //Log.d("data", data);
        String result = postLogin(LOGIN_URL, mCookie, data);


        Document doc = Jsoup.parse(result);
        if (doc.title().equals("学生个人中心")) {
            Element e=doc.select("select[id=xnxq01id]").first();
            Elements es=e.children();
            StringBuilder stringBuilder=new StringBuilder();
            for(Element element:es)
            {
                stringBuilder.append(element.text());
                stringBuilder.append("&&");//分隔符
                //Log.d("term",element.text());
            }
            return stringBuilder.toString();
        } else {
            //Log.d("term", "error");
            return "";
        }
    }

    public static String postLogin(String strUrl, String cookie, String data) {
        BufferedReader br = null;
        BufferedWriter bw=null;
        try {
            URL url = new URL(strUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
            connection.setRequestProperty("Content-Length",String.valueOf(data.getBytes().length));
            connection.setRequestProperty("Cookie", cookie);
            // 设置通用的请求属性
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");

            connection.setDoInput(true);
            connection.setDoOutput(true);


            if (!data.isEmpty()) {
                bw=new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
                bw.write(data);
                bw.flush();
            }
            if (connection.getResponseCode()==200) {
                br = new BufferedReader(new InputStreamReader(connection.getInputStream()));


                StringBuilder stringBuilder = new StringBuilder();
                int len;
                String line;
                while ((line = br.readLine()) != null) {
                    stringBuilder.append(line);
                }
                return stringBuilder.toString();

            } else
                return "";

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null)
                    br.close();
                if(bw!=null)
                    bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    private String encode(String account, String pw) {
        String str = HttpUtils.sendPost(SESS_URL, mCookie, "");
        System.out.println(str);
        if (str.isEmpty())
            return "";
        String[] strings = str.split("#");
        String scode = strings[0];
        String sxh = strings[1];
        String code = account + "%%%" + pw;
        StringBuilder encoded = new StringBuilder();
        for (int i = 0; i < code.length(); i++) {
            if (i < 50) {
                encoded.append(code.substring(i, i + 1));
                int value = Integer.parseInt(sxh.substring(i, i + 1));
                encoded.append(scode.substring(0, value));
                scode = scode.substring(value);
            } else {
                encoded.append(code.substring(i));
                i = code.length();
            }
        }
        return encoded.toString();
    }
}
