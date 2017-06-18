package com.yosriz.gphotosclient.signin;


import android.accounts.Account;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;

import java.io.IOException;

import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;

abstract class GoogleSignInOnSubscribeBase implements SingleOnSubscribe<GoogleSignIn.SignInAccount> {

    private static final String SCOPE_PICASA = "https://picasaweb.google.com/data/";

    protected GoogleApiClient googleApiClient;
    protected SingleEmitter<GoogleSignIn.SignInAccount> emitter;
    protected Context context;

    GoogleSignInOnSubscribeBase(Context context) {
        this.context = context;
    }

    @Override
    public void subscribe(SingleEmitter<GoogleSignIn.SignInAccount> emitter) throws Exception {
        this.emitter = emitter;
        initGoogleApiClient();

        emitter.setCancellable(() -> {
            disconnect();
            context = null;
        });

        act();
    }

    abstract protected void act();

    private void disconnect() {
        if (googleApiClient != null) {
            if (googleApiClient.isConnected()) {
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
        googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    protected void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            if (result.getSignInAccount() != null && result.getSignInAccount().getAccount() != null) {
                Account account = result.getSignInAccount().getAccount();
                try {
                    String token = GoogleAuthUtil.getToken(context, account, "oauth2:" + SCOPE_PICASA);
                    emitter.onSuccess(new GoogleSignIn.SignInAccount(token, result.getSignInAccount()));
                } catch (IOException | GoogleAuthException e) {
                    emitter.onError(new SignInException("SignIn", e));
                }
            } else {
                emitter.onError(new SignInException("SignIn", "getSignInAccount is null!", 0));
            }

        } else {
            if (result.getStatus().getStatusCode() == CommonStatusCodes.SIGN_IN_REQUIRED) {
                emitter.onError(new SignInRequiredException());
            } else {
                emitter.onError(new SignInException("SignIn", result.getStatus().getStatusMessage(), result.getStatus().getStatusCode()));
            }
        }
    }
}