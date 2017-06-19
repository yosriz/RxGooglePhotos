package com.yosriz.gphotosclient.signin;


import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.OptionalPendingResult;

import android.content.Context;

class GoogleSilentSignInOnSubscribe extends GoogleSignInOnSubscribeBase {

    GoogleSilentSignInOnSubscribe(Context context) {
        super(context);
    }

    @Override
    protected void act() {
        OptionalPendingResult<GoogleSignInResult> result = Auth.GoogleSignInApi.silentSignIn(googleApiClient);
        if (result.isDone()) {
            handleSignInResult(result.get());
        } else {
            result.setResultCallback(this::handleSignInResult);
        }
    }
}
