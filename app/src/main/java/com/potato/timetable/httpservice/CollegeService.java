package com.potato.timetable.httpservice;

import com.potato.timetable.bean.Course;
import com.potato.timetable.model.ResponseWrap;
import com.potato.timetable.model.college.RandomImg;

import java.util.List;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface CollegeService {
    @GET("/collegeList")
    Observable<ResponseWrap<List<String>>> getCollegeNameList();

    @GET("/college/random-code-length")
    Observable<ResponseWrap<Integer>> getRandomCodeLength(@Query("collegeName") String collegeName);

    @GET("/college/random-img-base64")
    Observable<ResponseWrap<RandomImg>> getRandomImg();

    @POST("/college/login")
    @FormUrlEncoded
    Observable<ResponseWrap<Boolean>> login(@Field("account") String account,
                                            @Field("password") String pwd,
                                            @Field("randomCode") String randomCode);

    @GET("/college/term-options")
    Observable<ResponseWrap<List<String>>> getTermOptions();

    @GET("/college/is-login")
    Observable<ResponseWrap<Boolean>> isLogin();

    @GET("/college/timetable")
    Observable<ResponseWrap<List<Course>>> getCourses(@Query("term") String term);
}
