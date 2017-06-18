package com.yosriz.gphotosclient.signin;


import android.annotation.SuppressLint;

public class SignInException extends Exception {

    @SuppressLint("DefaultLocale")
    SignInException(String operation, String errorMsg, int statusCode) {
        super(String.format("%s failed: msg = %s, code = %d", operation, errorMsg, statusCode));
    }

    SignInException(String signIn, Exception e) {
        super(String.format("%s failed with exception: %s", signIn, e));
    }

    protected SignInException(String s) {
        super(s);
    }
}
