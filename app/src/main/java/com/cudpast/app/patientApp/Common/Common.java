package com.cudpast.app.patientApp.Common;

import android.location.Location;

import com.cudpast.app.patientApp.Model.User;
import com.cudpast.app.patientApp.Remote.FCMClient;
import com.cudpast.app.patientApp.Remote.GoogleMapApi;
import com.cudpast.app.patientApp.Remote.IFCMService;
import com.cudpast.app.patientApp.Remote.IGoogleAPI;

public class Common {

    public static final String tb_Business_Doctor = "tb_Business_Doctor";
    public static final String tb_Info_Doctor = "tb_Info_Doctor";
    public static final String tb_Info_Paciente = "tb_Info_Paciente";
    public static final String pickup_request_tbl = "pickup_request_tbl";
    public static final String token_tbl = "Tokens";

    public static User currentUser;

    public static Location mLastLocation;
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
