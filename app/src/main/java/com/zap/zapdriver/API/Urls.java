package com.zap.zapdriver.API;

public class Urls {

    public static final String Base = "https://zaplogistics.co.ke/api/";
    public static final String Base2 = "https://zaplogistics.co.ke/";


    public static final String Auth = Base + "token/";

    public static final String Delivery = Base + "driverassignmet/?driver_assigned=";
    public static final String location_update = Base + "v1/location_update/";

    public static final String acceptrequest = Base2 + "acceptrequest";
    public static final String rejectrequest = Base2 + "rejectrequest";
    public static final String pickrequest = Base2 + "pickpackage";

    public static final String returnrequest = Base2 + "return";


    public static final String onlineRequest = Base2 + "driveronlinelinerequest";
    public static final String offlineRequest = Base2 + "driverofflinerequest";


    public static final String generate_receiver_code = Base + "v1/generate_receiver_code";


    public static final String is_driverassigned = Base2 + "api/v1/is_assined?id=";

    public static final String search_by_qr = Base2 + "api/v1/mypackages/?package_qr_code=";

    public static final String Token = Base2 + "api/token/";
    public static final String FCM_URL = Base2 + "api/devices/";

    public static final String register = Base2 + "api/users/";



    public static final String complete_delivery_request = Base2 + "deliverPackage";

    public static final String driverassignedpackages = Base2 + "api/v1/driverassignedpackages/?driver_assigned=";

    public static final String rider_location = Base2 + "api/v1/live_track?id=";

    public static final String checkPaid = Base2 + "ispackagepaid/";

    public static final String onlinepayment = Base2 + "mpesa/";


    public static final String finace = Base + "v1/finance/?rider=";

    public static final String location_path = Base2 + "api/v1/location_path";


    public static String mpesacallback = "https://zaplogistics.co.ke/c2b/callback";
}
