package com.yosriz.gphotosclient.signin;


import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Single;

public class GoogleSignIn {

    interface OnActivityResultListener {

        void onActivityResult(int requestCode, int resultCode, Intent data);
    }

    public static class SignInAccount {

        private final String token;
        private final GoogleSignInAccount account;

        SignInAccount(String token, GoogleSignInAccount account) {
            this.token = token;
            this.account = account;
        }

        public GoogleSignInAccount getAccount() {
            return account;
        }

        public String getToken() {
            return token;
        }

    }

    private List<OnActivityResultListener> activityResultListeners = new ArrayList<>();

    public Single<SignInAccount> getToken(final Activity activity) {
        GoogleSignInOnSubscribe subscriber = new GoogleSignInOnSubscribe(activity);
        activityResultListeners.add(subscriber);
        return Single.create(subscriber)
                .doOnDispose(() -> activityResultListeners.remove(subscriber))
                .doAfterTerminate(() -> activityResultListeners.remove(subscriber));
    }

    public Single<SignInAccount> getTokenSilently(final Context context) {
        return Single.create(new GoogleSilentSignInOnSubscribe(context));
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        for (OnActivityResultListener listener : activityResultListeners) {
            listener.onActivityResult(requestCode, resultCode, data);
        }
    }

}
