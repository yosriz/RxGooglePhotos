package com.yosriz.gphotosclient.signin;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;

import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class GoogleSignOutOnSubscribe implements CompletableOnSubscribe {

    private static final String SCOPE_PICASA = "https://picasaweb.google.com/data/";
    private FragmentActivity activity;
    private GoogleApiClient googleApiClient;
    private CompletableEmitter emitter;

    GoogleSignOutOnSubscribe(FragmentActivity activity) {
        this.activity = activity;
    }


    @Override
    public void subscribe(CompletableEmitter emitter) throws Exception {
        initGoogleApiClient();
        this.emitter = emitter;
        emitter.setCancellable(() -> {
            AndroidSchedulers.mainThread().scheduleDirect(() -> {
                disconnect();
                activity = null;
            });
        });
    }

    private void disconnect() {
        if (googleApiClient != null) {
            if (googleApiClient.isConnected()) {
                googleApiClient.stopAutoManage(activity);
                googleApiClient.disconnect();
            }
        }
    }

    private void initGoogleApiClient() {
        final GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .requestScopes(new Scope(SCOPE_PICASA))
                .build();
        googleApiClient = new GoogleApiClient.Builder(activity)
                .enableAutoManage(activity,
                        connectionResult -> emitter.onError(new SignInException("Connecting", connectionResult.getErrorMessage(), connectionResult.getErrorCode())))
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        actWhenConnected();
                    }

                    @Override
                    public void onConnectionSuspended(int i) {

                    }
                })
                .build();
    }

    private void actWhenConnected() {
        Auth.GoogleSignInApi.signOut(googleApiClient)
                .setResultCallback(status -> {
                    if (status.isSuccess()) {
                        emitter.onComplete();
                    } else {
                        emitter.onError(new SignInException("signOut", status.getStatus().getStatusMessage(), status.getStatus().getStatusCode()));
                    }
                });
    }
}
