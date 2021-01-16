package com.potato.timetable.httpservice;

import com.potato.timetable.bean.Course;
import com.potato.timetable.model.ResponseWrap;
import com.potato.timetable.model.college.RandomImg;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface CollegeService {
    @GET("/collegeList")
    Observable<ResponseWrap<List<String>>> getCollegeNameList();

    @GET("/college/randomImg")
    Observable<ResponseWrap<RandomImg>> getRandomImg();

    @POST("/college/login")
    @FormUrlEncoded
    Observable<ResponseWrap<Boolean>> login(@Field("account") String account,
                                            @Field("password") String pwd,
                                            @Field("randomCode") String randomCode);

    @GET("/college/termOptions")
    Observable<ResponseWrap<List<String>>> getTermOptions();

    @GET("/college/isLogin")
    Observable<ResponseWrap<Boolean>> isLogin();

    @GET("/college/courses")
    Observable<ResponseWrap<List<Course>>> getCourses(@Query("term") String term);
}
