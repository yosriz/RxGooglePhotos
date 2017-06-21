package com.yosriz.gphotosclient.signin;


import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.OptionalPendingResult;

import android.support.v4.app.FragmentActivity;

class GoogleSilentSignInOnSubscribe extends GoogleSignInOnSubscribeBase {

    GoogleSilentSignInOnSubscribe(FragmentActivity activity) {
        super(activity);
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
