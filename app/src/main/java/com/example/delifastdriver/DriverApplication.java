package com.example.delifastdriver;

import android.app.Application;

public class DriverApplication extends Application {


    String token_key;


    @Override
    public void onCreate() {
        super.onCreate();
    }


    public DriverApplication() {
    }


    public String getToken_key() {
        return token_key;
    }

    public void setToken_key(String token_key) {
        this.token_key = token_key;
    }
}
