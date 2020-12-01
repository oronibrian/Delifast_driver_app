package com.zap.zapdriver;

import android.app.Application;

public class DriverApplication extends Application {


    String token_key;

    String username;
    String userid;


    @Override
    public void onCreate() {
        super.onCreate();
    }


    public DriverApplication() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getToken_key() {
        return token_key;
    }

    public void setToken_key(String token_key) {
        this.token_key = token_key;
    }
}