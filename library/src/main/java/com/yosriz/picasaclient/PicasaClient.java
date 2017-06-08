package com.yosriz.picasaclient;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.GoogleApiAvailability;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.yosriz.picasaclient.model.AlbumFeed;
import com.yosriz.picasaclient.model.AlbumFeedResponse;
import com.yosriz.picasaclient.model.UserFeed;
import com.yosriz.picasaclient.model.UserFeedResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Completable;
import rx.Single;
import rx.SingleSubscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class PicasaClient {

    public static final String ACCOUNT_TYPE_GOOGLE = "com.google";
    private static boolean DEBUG = BuildConfig.DEBUG || Log.isLoggable("yosriz.picasaclient", Log.DEBUG);

    private static final String SCOPE_PICASA = "https://picasaweb.google.com/data/";
    private static final String BASE_API_URL = "https://picasaweb.google.com/data/feed/api/user/";

    private static final int TIMEOUT_SEC = 30;
    private static final int REQUEST_ACCOUNT_PICKER = 1000;
    private static final int REQUEST_RECOVER_PLAY_SERVICES_ERROR = 1024;

    private static PicasaClient picasaClient;
    private Activity mActivity;
    private Fragment mFragment;
    private Account mAccount;
    private String mOAuthToken;
    private PicasaService mPicasaService;

    private PicasaClient() {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public okhttp3.Response intercept(Chain chain) throws IOException {
                        Request originalRequest = chain.request();
                        if (originalRequest.body() != null || originalRequest.header("Authorization") != null) {
                            return chain.proceed(originalRequest);
                        }

                        HttpUrl jsonUrl = originalRequest.url().newBuilder()
                                .addQueryParameter("alt", "json")
                                .build();

                        Request authorizedRequest = originalRequest.newBuilder()
                                .url(jsonUrl)
                                .header("Authorization", "Bearer " + mOAuthToken)
                                .header("Gdata-version", "2")
                                .build();
                        return chain.proceed(authorizedRequest);
                    }
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
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(clientBuilder.build())
                .build();

        mPicasaService = retrofit.create(PicasaService.class);
    }

    public static PicasaClient get() {
        if (picasaClient == null) {
            picasaClient = new PicasaClient();
        }
        return picasaClient;
    }

    public void attachActivity(Activity activity) {
        mActivity = activity;
    }

    public void attachFragment(Activity activity, Fragment fragment) {
        mActivity = activity;
        mFragment = fragment;
    }

    public void detach() {
        mActivity = null;
        mFragment = null;
    }

    /**
     * Completed when the Picasa service is initialized.
     */
    public Completable setAccount(Account account, String... additionalScopes) {
        if (account.type.equals(ACCOUNT_TYPE_GOOGLE)) {
            mAccount = account;

            return retrieveTokenInitService(additionalScopes);
        } else {
            return Completable.error(new RuntimeException("You may only set a Google account"));
        }
    }


    /**
     * onActivityResult will be called either in the Activity/Fragment depending on whether a
     * Fragment is attached.
     */
    public void pickAccount() {
        String[] accountTypes = new String[]{ACCOUNT_TYPE_GOOGLE};
        Intent intent = AccountPicker.newChooseAccountIntent(null, null, accountTypes, false, null, null, null, null);
        if (mFragment != null) {
            mFragment.startActivityForResult(intent, REQUEST_ACCOUNT_PICKER);
        } else {
            mActivity.startActivityForResult(intent, REQUEST_ACCOUNT_PICKER);
        }
    }


    /**
     * Processes account picker or error result. Completed when the Picasa service is initialized.
     */
    public Completable onActivityResult(int requestCode, int resultCode, Intent data,
                                        String... additionalScopes) {
        if (requestCode == REQUEST_ACCOUNT_PICKER) {
            if (resultCode == Activity.RESULT_OK) {
                String accountEmail = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                mAccount = new Account(accountEmail, ACCOUNT_TYPE_GOOGLE);

                return retrieveTokenInitService(additionalScopes);
            } else if (resultCode == Activity.RESULT_CANCELED) {
                //Select an account to add
                return Completable.error(new Exception("User canceled account picker dialog"));
            }
        } else if (requestCode == REQUEST_RECOVER_PLAY_SERVICES_ERROR && resultCode == Activity.RESULT_OK) {
            // Receiving a result that follows a GoogleAuthException, try auth again
            return retrieveTokenInitService(additionalScopes);
        }

        return Completable.never();
    }

    private boolean isDeviceOnline() {
        ConnectivityManager connMgr = (ConnectivityManager) mActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    private Completable retrieveTokenInitService(final String... additionalScopes) {
        if (isDeviceOnline()) {
            return Single.create(new Single.OnSubscribe<String>() {
                @Override
                public void call(SingleSubscriber<? super String> subscriber) {
                    try {
                        List<String> scopes = new ArrayList<>();
                        scopes.add(SCOPE_PICASA);
                        scopes.addAll(Arrays.asList(additionalScopes));
                        subscriber.onSuccess(GoogleAuthUtil.getToken(mActivity, mAccount, "oauth2:"
                                + TextUtils.join(" ", scopes)));
                    } catch (IOException | GoogleAuthException e) {
                        subscriber.onError(e);
                    }
                }
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnError(new Action1<Throwable>() {
                        @Override
                        public void call(Throwable error) {
                            if (error instanceof GooglePlayServicesAvailabilityException) {
                                int statusCode = ((GooglePlayServicesAvailabilityException) error)
                                        .getConnectionStatusCode();
                                Dialog dialog = GoogleApiAvailability.getInstance()
                                        .getErrorDialog(mActivity, statusCode, REQUEST_RECOVER_PLAY_SERVICES_ERROR);
                                dialog.show();
                            } else if (error instanceof UserRecoverableAuthException) {
                                Intent intent = ((UserRecoverableAuthException) error).getIntent();
                                mActivity.startActivityForResult(intent, REQUEST_RECOVER_PLAY_SERVICES_ERROR);
                            }
                        }
                    })
                    .doOnSuccess(new Action1<String>() {
                        @Override
                        public void call(String s) {
                            mOAuthToken = s;
                        }
                    })
                    .toObservable()
                    .toCompletable();
        } else {
            return Completable.error(new Exception("Device not online."));
        }
    }

    private void checkTokenInitialized() {
        if (!isInitialized()) {
            throw new RuntimeException("Service not initialized");
        }
    }

    public boolean isInitialized() {
        return mOAuthToken != null;
    }

    @Nullable
    public Account getAccount() {
        return mAccount;
    }

    public PicasaService getService() {
        checkTokenInitialized();
        return mPicasaService;
    }

    public Single<UserFeed> getUserFeed() {
        checkTokenInitialized();
        return mPicasaService.getUserFeedResponse()
                .map(new Func1<UserFeedResponse, UserFeed>() {
                    @Override
                    public UserFeed call(UserFeedResponse response) {
                        return response.getFeed();
                    }
                })
                .subscribeOn(Schedulers.io())
                .toSingle();
    }

    public Single<AlbumFeed> getAlbumFeed(long albumId) {
        checkTokenInitialized();
        return mPicasaService.getAlbumFeedResponse(albumId)
                .map(new Func1<AlbumFeedResponse, AlbumFeed>() {
                    @Override
                    public AlbumFeed call(AlbumFeedResponse response) {
                        return response.getFeed();
                    }
                })
                .subscribeOn(Schedulers.io())
                .toSingle();
    }

    public Single<AlbumFeed> getAlbumFeed(long albumId, int startIndex, int maxResults) {
        checkTokenInitialized();
        if (startIndex < 1) {
            throw new IllegalArgumentException("Illegal start index, must be above 0");
        }
        return mPicasaService.getAlbumFeedResponse(albumId, startIndex, maxResults)
                .map(new Func1<AlbumFeedResponse, AlbumFeed>() {
                    @Override
                    public AlbumFeed call(AlbumFeedResponse response) {
                        return response.getFeed();
                    }
                })
                .subscribeOn(Schedulers.io())
                .toSingle();
    }

}
