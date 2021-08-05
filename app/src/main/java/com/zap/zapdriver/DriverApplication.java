package com.zap.zapdriver;

import android.app.Application;

import com.google.android.gms.maps.model.LatLng;

public class DriverApplication extends Application {


    String token_key;

    String username;
    String userid;
    String package_id;
    String password;
    String fcm_device_token;
    String auttoken;
    String package_from,package_to;
    LatLng destination;
    Boolean is_cooperate;

    String firstName,last_name,email,phone_no;


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

    public String getPackage_id() {
        return package_id;
    }

    public void setPackage_id(String package_id) {
        this.package_id = package_id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFcm_device_token() {
        return fcm_device_token;
    }

    public void setFcm_device_token(String fcm_device_token) {
        this.fcm_device_token = fcm_device_token;
    }

    public String getAuttoken() {
        return auttoken;
    }

    public void setAuttoken(String auttoken) {
        this.auttoken = auttoken;
    }

    public String getPackage_from() {
        return package_from;
    }

    public void setPackage_from(String package_from) {
        this.package_from = package_from;
    }

    public String getPackage_to() {
        return package_to;
    }

    public void setPackage_to(String package_to) {
        this.package_to = package_to;
    }

    public LatLng getDestination() {
        return destination;
    }

    public void setDestination(LatLng destination) {
        this.destination = destination;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getIs_cooperate() {
        return is_cooperate;
    }

    public void setIs_cooperate(Boolean is_cooperate) {
        this.is_cooperate = is_cooperate;
    }

    public String getPhone_no() {
        return phone_no;
    }

    public void setPhone_no(String phone_no) {
        this.phone_no = phone_no;
    }
}

