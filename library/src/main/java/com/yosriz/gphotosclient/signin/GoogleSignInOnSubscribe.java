package com.yosriz.gphotosclient.signin;


import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;

class GoogleSignInOnSubscribe extends GoogleSignInOnSubscribeBase implements GoogleSignIn.OnActivityResultListener {

    private static final int RC_SIGN_IN = 4200;
    private final Activity activity;

    GoogleSignInOnSubscribe(FragmentActivity activity) {
        super(activity);
        this.activity = activity;
    }

    @Override
    protected void act() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        activity.startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }
}
