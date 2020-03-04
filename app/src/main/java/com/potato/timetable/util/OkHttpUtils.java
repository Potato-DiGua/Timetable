package com.potato.timetable.util;

import android.content.Context;

import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OkHttpUtils {
    private static final byte[] EMPTY_BYTES = new byte[0];

    /**
     * 自动存储保存cookies

    private static class MyCookieJar implements CookieJar {
        private Map<String, List<Cookie>> cookieStore = new HashMap<>();

        @NotNull
        @Override
        public List<Cookie> loadForRequest(@NotNull HttpUrl httpUrl) {
            List<Cookie> cookies = cookieStore.get(httpUrl.host());
            return cookies != null ? cookies : new ArrayList<Cookie>();
        }

        @Override
        public void saveFromResponse(@NotNull HttpUrl httpUrl, @NotNull List<Cookie> list) {
            cookieStore.put(httpUrl.host(), list);
        }
    }

    private static MyCookieJar cookieJar = new MyCookieJar();*/
    private static PersistentCookieJar cookieJar;
    private static OkHttpClient okHttpClient;

    public static OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

    /**
     * 初始化okhttp，并自动管理cookie
     * @param context
     */
    public static void init(Context context)
    {
        cookieJar = new PersistentCookieJar(
                new SetCookieCache(),
                new SharedPrefsCookiePersistor(
                        context.getApplicationContext()));
        okHttpClient = new OkHttpClient()
                .newBuilder()
                .cookieJar(cookieJar)
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)
                .build();
    }
    /**
     * 下载文件到本地
     *
     * @param url
     * @param path 文件夹地址
     * @param name 文件名
     * @return
     */
    public static boolean downloadToLocal(String url, String path, String name) {
        Request request = new Request.Builder()
                .url(url)
                .build();
        return downloadToLocal(request, path, name);
    }

    /**
     * 下载文件到本地
     *
     * @param path 文件夹地址
     * @param name 文件名
     * @return
     */
    public static boolean downloadToLocal(Request request, String path, String name) {
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        try {


            Response response = getOkHttpClient().newCall(request).execute();
            File file = new File(path);
            if (!file.exists()) {
                file.mkdirs();
            } else {
                if (!file.isDirectory())
                    return false;
            }

            bos = new BufferedOutputStream(
                    new FileOutputStream(path + File.separator + name));
            bis = new BufferedInputStream(response.body().byteStream());

            byte[] buffer = new byte[1024];
            int len;
            while ((len=bis.read(buffer, 0, 1024)) != -1) {
                bos.write(buffer,0,len);
            }
            bos.flush();

            return true;
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bis != null) {
                    bis.close();
                }
                if (bos != null) {
                    bos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return false;
    }


    /**
     * 将字节数组转为字符串，默认utf-8编码
     *
     * @param data
     * @return
     */
    public static String toString(byte[] data) {
        return toString(data, "UTF-8");
    }

    /**
     * 将字节数组转为特定编码的字符串
     *
     * @param data
     * @param charsetName
     * @return
     */
    public static String toString(byte[] data, String charsetName) {
        try {
            return new String(data, charsetName);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 下载字节码
     *
     * @param url
     * @return
     */
    public static byte[] downloadRaw(String url) {
        Request request = new Request.Builder()
                .url(url)
                .build();
        return downloadRaw(request);
    }

    /**
     * 下载字节码
     *
     * @param request
     * @return
     */
    public static byte[] downloadRaw(Request request) {
        try {
            Response response = getOkHttpClient().newCall(request).execute();
            InputStream is = response.body().byteStream();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len=is.read(buffer, 0, 1024)) != -1) {
                bos.write(buffer,0,len);
            }
            bos.flush();
            return bos.toByteArray();

        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            return EMPTY_BYTES;
        }
    }
}
