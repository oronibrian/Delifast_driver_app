package com.zap.zapdriver.Model;

public class PackageModel {
    private String date;
    private String title;
    private String pickup_name;
    private String dropoff_name;
    private String distance;
    private String status;
    private String cost;
    private Boolean pay_now;



    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPickup_name() {
        return pickup_name;
    }

    public void setPickup_name(String pickup_name) {
        this.pickup_name = pickup_name;
    }

    public String getDropoff_name() {
        return dropoff_name;
    }

    public void setDropoff_name(String dropoff_name) {
        this.dropoff_name = dropoff_name;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCost() {
        return cost;
    }

    public void setCost(String cost) {
        this.cost = cost;
    }

    public Boolean getPay_now() {
        return pay_now;
    }

    public void setPay_now(Boolean pay_now) {
        this.pay_now = pay_now;
    }
}
