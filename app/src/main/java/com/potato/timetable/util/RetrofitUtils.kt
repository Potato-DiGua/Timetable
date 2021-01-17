package com.potato.timetable.util

import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import com.potato.timetable.MyApplication
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


class RetrofitUtils private constructor() {
    companion object {
        private const val BASE_URL = "http://192.168.50.162"

        @JvmStatic
        val retrofit: Retrofit by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(OkHttpClient.Builder()
                            .connectTimeout(5, TimeUnit.SECONDS)
                            .readTimeout(5, TimeUnit.SECONDS)
                            .writeTimeout(5, TimeUnit.SECONDS)
                            .cookieJar(PersistentCookieJar(
                                    SetCookieCache(),
                                    SharedPrefsCookiePersistor(MyApplication.getApplication())))
                            .build()
                    ).addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                    .build()
        }
    }
}

fun Disposable.add(c: CompositeDisposable) {
    c.add(this)
}