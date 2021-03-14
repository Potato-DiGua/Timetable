package com.potato.timetable.util;

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

    private static class Inner {
        private static final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(3, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)
                .followRedirects(true)
//                .addInterceptor(chain -> {
//                    /*
//                     * 登录拦截器
//                     * */
//                    Request request = chain.request();
//                    Response response = chain.proceed(request);
//                    if (response.code() == 401) {
//                        Intent intent = new Intent(MyApplication.getApplication(), LoginActivity.class);
//                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                        MyApplication
//                                .getApplication()
//                                .startActivity(intent);
//
//                        throw new IOException("没有登录");
//                    }
//                    return response;
//                })
                .addInterceptor(chain -> {
                    Request request = chain
                            .request()
                            .newBuilder()
                            .header("cookie", "JSESSIONID=" + SharePreferenceUtil.getToken())
                            .build();
                    return chain.proceed(request);
                })
                .build();
    }

    public static OkHttpClient getOkHttpClient() {
        return Inner.okHttpClient;
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
        return downloadToLocal(createRequest(url), path, name);
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
            if (response.code() == 200) {
                File file = new File(path);
                if (!file.exists()) {
                    if (!file.mkdirs()) {
                        return false;
                    }
                } else {
                    if (!file.isDirectory()) {
                        return false;
                    }
                }

                bos = new BufferedOutputStream(
                        new FileOutputStream(path + File.separator + name));
                bis = new BufferedInputStream(response.body().byteStream());

                byte[] buffer = new byte[1024];
                int len;
                while ((len = bis.read(buffer, 0, 1024)) != -1) {
                    bos.write(buffer, 0, len);
                }
                bos.flush();

                return true;
            }
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
     * 下载文本内容
     *
     * @param url
     * @return
     */
    public static String downloadText(String url) {
        return downloadText(createRequest(url));
    }

    /**
     * 下载文本内容
     *
     * @param request
     * @return
     */
    public static String downloadText(Request request) {
        return downloadText(request, "UTF-8");
    }

    /**
     * 下载文本内容
     *
     * @param url
     * @return
     */
    public static String downloadText(String url, String encoding) {
        return downloadText(createRequest(url), encoding);
    }

    /**
     * 下载文本内容
     *
     * @param request
     * @param encoding
     * @return
     */
    public static String downloadText(Request request, String encoding) {
        try {
            return new String(downloadRaw(request), encoding);
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
     * @return 返回下载内容
     */
    public static byte[] downloadRaw(Request request) {
        try {
            Response response = getOkHttpClient().newCall(request).execute();
            if (response.code() == 200) {
                InputStream is = response.body().byteStream();
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len;
                while ((len = is.read(buffer, 0, 1024)) != -1) {
                    bos.write(buffer, 0, len);
                }
                bos.flush();
                return bos.toByteArray();
            }
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
        return EMPTY_BYTES;
    }

    public static Request createRequest(String url) {
        return new Request.Builder()
                .url(url)
                .build();
    }
}
