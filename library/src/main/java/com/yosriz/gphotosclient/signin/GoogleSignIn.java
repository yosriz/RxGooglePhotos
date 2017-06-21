package com.yosriz.gphotosclient.signin;


import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Completable;
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

    public Single<SignInAccount> getToken(final FragmentActivity activity) {
        GoogleSignInOnSubscribe subscriber = new GoogleSignInOnSubscribe(activity);
        activityResultListeners.add(subscriber);
        return Single.create(subscriber)
                .doOnDispose(() -> activityResultListeners.remove(subscriber))
                .doAfterTerminate(() -> activityResultListeners.remove(subscriber));
    }

    public Single<SignInAccount> getTokenSilently(final FragmentActivity activity) {
        return Single.create(new GoogleSilentSignInOnSubscribe(activity));
    }

    public Completable signOut(final FragmentActivity activity) {
        return Completable.create(new GoogleSignOutOnSubscribe(activity));
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        for (OnActivityResultListener listener : activityResultListeners) {
            listener.onActivityResult(requestCode, resultCode, data);
        }
    }

}
