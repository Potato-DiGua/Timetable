package com.potato.timetable.util;


public class HttpUtils {
    /**
     * ping 判断网络连通
     *
     * @return
     */
    public static boolean isNetworkConnected() {
        try {
            //代表ping 3 次 超时时间为10秒
            Process p = Runtime.getRuntime().exec("ping -c 3 -w 10 www.baidu.com");//ping3次
            int status = p.waitFor();
            if (status == 0) {
                //代表成功
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

}
