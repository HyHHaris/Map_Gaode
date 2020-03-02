package com.a01tech.map_gaode.retrofit;

import com.a01tech.map_gaode.BuildConfig;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.util.concurrent.TimeUnit;

/**
 * Medical   com.a01.medical.utils
 * Created by Zou on 2017/11/22 0022.11:50
 */

public class RetrofitUtil {

    private static Retrofit retrofit;
    private static MapService service;

    public static MapService getService() {
        if (retrofit == null) {
            OkHttpClient.Builder httpBuilder = new OkHttpClient.Builder();
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            if (BuildConfig.DEBUG) {
                logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            }
            httpBuilder.addInterceptor(logging);
            OkHttpClient client = httpBuilder.readTimeout(240, TimeUnit.SECONDS)
                    .connectTimeout(20, TimeUnit.SECONDS).writeTimeout(2, TimeUnit.MINUTES) //设置超时
                    .build();

            synchronized (MapService.class) {
                retrofit = new Retrofit.Builder()
                        .baseUrl(MapService.Companion.baseUrl)
                        .client(client)
                        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                        .addConverterFactory(CustomGsonConverterFactory.create())
                        .build();
                service = retrofit.create(MapService.class);
            }
        } else if (service == null) {
            synchronized (MapService.class) {
                service = retrofit.create(MapService.class);
            }
        }

        return service;
    }
}
