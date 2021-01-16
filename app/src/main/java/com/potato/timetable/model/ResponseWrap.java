package com.potato.timetable.model;

import com.google.gson.Gson;

public class ResponseWrap<T> {
    // 0-成功 1-失败
    private int status;
    // 提示信息
    private String msg;
    // 数据
    private T data;

    /**
     *
     * @param status 0-成功 1-失败
     * @return
     */
    public ResponseWrap<T> setStatus(int status) {
        this.status = status;
        return this;
    }

    public ResponseWrap<T> setMsg(String msg) {
        this.msg = msg;
        return this;
    }

    public ResponseWrap<T> setData(T data) {
        this.data = data;
        return this;
    }

    public int getStatus() {
        return status;
    }


    public String getMsg() {
        return msg;
    }


    public T getData() {
        return data;
    }


    public String toJson() {
        return new Gson().toJson(this);
    }
}
