package com.yosriz.gphotosclient.signin;


import android.accounts.Account;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.Scope;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.schedulers.Schedulers;

public class GoogleSignIn {

    private interface OnActivityResultListener {
        void onActivityResult(int requestCode, int resultCode, Intent data);
    }

    private static final String SCOPE_PICASA = "https://picasaweb.google.com/data/";
    private static final int RC_SIGN_IN = 42;
    private List<OnActivityResultListener> activityResultListeners = new ArrayList<>();

    public Single<String> getToken(final AppCompatActivity activity) {
        GoogleSignInOnSubscriber subscriber = new GoogleSignInOnSubscriber(activity);
        activityResultListeners.add(subscriber);
        return Single.create(subscriber)
                .doOnDispose(() -> activityResultListeners.remove(subscriber))
                .doAfterTerminate(() -> activityResultListeners.remove(subscriber));
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        for (OnActivityResultListener listener : activityResultListeners) {
            listener.onActivityResult(requestCode, resultCode, data);
        }
    }

    private static class GoogleSignInOnSubscriber implements SingleOnSubscribe<String>, OnActivityResultListener {

        private GoogleApiClient googleApiClient;
        private SingleEmitter<String> emitter;
        private AppCompatActivity activity;

        private GoogleSignInOnSubscriber(AppCompatActivity activity) {
            this.activity = activity;
        }

        @Override
        public void subscribe(SingleEmitter<String> emitter) throws Exception {
            this.emitter = emitter;
            initGoogleApiClient(emitter);

            emitter.setCancellable(() -> {
                disconnect();
                activity = null;
            });

            OptionalPendingResult<GoogleSignInResult> result = Auth.GoogleSignInApi.silentSignIn(googleApiClient);
            if (result.isDone()) {
                handleSignInResult(result.get());
            } else {
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
                activity.startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        }

        private void disconnect() {
            if (googleApiClient != null) {
                googleApiClient.stopAutoManage(activity);
                if (googleApiClient.isConnected()) {
                    googleApiClient.disconnect();
                }
            }
        }

        private void initGoogleApiClient(SingleEmitter<String> emitter) {
            final GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .requestScopes(new Scope(SCOPE_PICASA))
                    .build();
            googleApiClient = new GoogleApiClient.Builder(activity)
                    .enableAutoManage(activity,
                            connectionResult -> emitter.onError(new SignInException("SignIn", connectionResult.getErrorMessage(), connectionResult.getErrorCode())
                            )
                    )
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .build();
        }

        private void handleSignInResult(GoogleSignInResult result) {
            Schedulers.newThread().createWorker().schedule(() -> {
                if (result.isSuccess()) {
                    if (result.getSignInAccount() != null && result.getSignInAccount().getAccount() != null) {
                        Account account = result.getSignInAccount().getAccount();
                        try {
                            String token = GoogleAuthUtil.getToken(activity, account, "oauth2:" + SCOPE_PICASA);
                            emitter.onSuccess(token);
                        } catch (IOException | GoogleAuthException e) {
                            emitter.onError(new SignInException("SignIn", e));
                        }
                    } else {
                        emitter.onError(new SignInException("SignIn", "getSignInAccount is null!", 0));
                    }

                } else {
                    emitter.onError(new SignInException("SignIn", result.getStatus().getStatusMessage(), result.getStatus().getStatusCode()));
                }
            });
        }

        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (requestCode == RC_SIGN_IN) {
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                handleSignInResult(result);
            }
        }
    }

    public static class SignInException extends Exception {

        @SuppressLint("DefaultLocale")
        SignInException(String operation, String errorMsg, int statusCode) {
            super(String.format("%s failed: msg = %s, code = %d", operation, errorMsg, statusCode));
        }

        SignInException(String signIn, Exception e) {
            super(String.format("%s failed with exception: %s", signIn, e));
        }
    }
}
