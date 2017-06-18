package com.yosriz.gphotosclient.signin;


public class SignInRequiredException extends SignInException {

    SignInRequiredException() {
        super("Sign in required.");
    }

}
