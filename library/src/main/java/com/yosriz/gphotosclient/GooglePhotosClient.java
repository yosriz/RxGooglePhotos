package com.yosriz.gphotosclient;


import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.yosriz.gphotosclient.signin.GoogleSignIn;

import java.util.concurrent.TimeUnit;

import io.reactivex.Single;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class GooglePhotosClient {

    private static boolean DEBUG = BuildConfig.DEBUG || Log.isLoggable("gphotosclient", Log.DEBUG);
    private static final String BASE_API_URL = "https://picasaweb.google.com/data/feed/api/user/";
    private static final int TIMEOUT_SEC = 30;

    private final GoogleSignIn googleSignIn;

    public GooglePhotosClient() {
        googleSignIn = new GoogleSignIn();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        googleSignIn.onActivityResult(requestCode, resultCode, data);
    }

    public Single<GooglePhotosService> createGooglePhotosService(AppCompatActivity activity) {
        return googleSignIn.getToken(activity)
                .map(signInAccount -> new GooglePhotosService(createRetrofit(signInAccount.getToken()), signInAccount.getAccount()));
    }

    private PicasaApi createRetrofit(String authToken) {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request originalRequest = chain.request();
                    if (originalRequest.body() != null || originalRequest.header("Authorization") != null) {
                        return chain.proceed(originalRequest);
                    }

                    HttpUrl jsonUrl = originalRequest.url().newBuilder()
                            .addQueryParameter("alt", "json")
                            .build();

                    Request authorizedRequest = originalRequest.newBuilder()
                            .url(jsonUrl)
                            .header("Authorization", "Bearer " + authToken)
                            .header("Gdata-version", "3")
                            .build();
                    return chain.proceed(authorizedRequest);
                });
        if (DEBUG) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);
            clientBuilder.addInterceptor(loggingInterceptor);
        }
        clientBuilder.connectTimeout(TIMEOUT_SEC, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT_SEC, TimeUnit.SECONDS);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(clientBuilder.build())
                .build();

        return retrofit.create(PicasaApi.class);
    }

}
