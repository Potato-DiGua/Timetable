package com.potato.timetable.httpservice;

import com.potato.timetable.bean.Course;
import com.potato.timetable.model.ResponseWrap;
import com.potato.timetable.model.ShareModel;

import java.util.List;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ShareService {
    @POST("/calendar/shareCalendar")
    @FormUrlEncoded
    Observable<ResponseWrap<ShareModel>> shareTimeTable(@Field("calendar") String timetableJson);

    @GET("/calendar/getSharedCalendar")
    Observable<ResponseWrap<List<Course>>> getSharedCalendar(@Query("key") String key);
}
