package com.potato.timetable.httpservice

import com.potato.timetable.model.ResponseWrap
import com.potato.timetable.model.User
import io.reactivex.rxjava3.core.Observable
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface UserService {
    @POST("/user/login")
    @FormUrlEncoded
    fun login(@Field("account") account: String,
              @Field("password") pwd: String): Call<ResponseWrap<User>>

    @POST("/user/register")
    @FormUrlEncoded
    fun register(@Field("name") name: String,
                 @Field("account") account: String,
                 @Field("password") pwd: String): Call<ResponseWrap<Any>>

    @GET("/user/isLogin")
    fun isLogin(): Observable<ResponseWrap<Any>>
}