package com.potato.timetable.util

import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory


class RetrofitUtils private constructor() {
    companion object {
        private const val BASE_URL = "http://192.168.50.162"

        @JvmStatic
        val retrofit: Retrofit by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(OkHttpUtils.getOkHttpClient()).addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                    .build()
        }
    }
}

fun Disposable.add(c: CompositeDisposable) {
    c.add(this)
}