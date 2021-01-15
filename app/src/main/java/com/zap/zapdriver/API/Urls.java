package com.zap.zapdriver.API;

public class Urls {

    public static final String Base = "https://zaplogistics.co.ke/api/";
    public static final String Base2 = "https://zaplogistics.co.ke/";


    public static final String Auth = Base + "auth/login/";

    public static final String Delivery = Base + "driverassignmet/?driver_assigned=";
    public static final String location_update = Base + "v1/location_update/";

    public static final String acceptrequest = Base2 + "acceptrequest";
    public static final String rejectrequest = Base2 + "rejectrequest";

    public static final String is_driverassigned = Base2 + "api/v1/is_assined?id=";


    public static final String Token = Base2 + "api/token/";
    public static final String FCM_URL = Base2 + "api/devices/";

    public static final String complete_delivery_request = Base2 + "deliverPackage";



}
