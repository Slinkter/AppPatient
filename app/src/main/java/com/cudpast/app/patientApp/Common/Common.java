package com.cudpast.app.patientApp.Common;

import com.cudpast.app.patientApp.Remote.FCMClient;
import com.cudpast.app.patientApp.Remote.GoogleMapApi;
import com.cudpast.app.patientApp.Remote.IFCMService;
import com.cudpast.app.patientApp.Remote.IGoogleAPI;

public class Common {

    public static final String driver_tbl = "Drivers";
    public static final String user_driver_tbl = "DriversInformation";
    public static final String user_rider_tbl = "RidersInformation";
    public static final String pickup_request_tbl = "PickupRequest";
    public static final String token_tbl = "Tokens";


    public static final String fcmURL = "https://fcm.googleapis.com/";

    public static final String googleAPIUrl = "https://maps.googleapis.com";

    private static double base_fare = 2.55;
    private static double time_rate = 2.55;
    private static double distance_rate = 2.55;

    public static double getPrice(double km, int min) {
        return (base_fare + (time_rate * min) + (distance_rate * km));
    }

    public static IFCMService getIFCMService() {
        return FCMClient.getClient(fcmURL).create(IFCMService.class);
    }

    public static IGoogleAPI getGoogleService() {

        return GoogleMapApi.getClient(googleAPIUrl).create(IGoogleAPI.class);
    }

}
