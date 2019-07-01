package com.cudpast.app.patientApp.Common;

import android.location.Location;

import com.cudpast.app.patientApp.Model.DoctorPerfil;
import com.cudpast.app.patientApp.Model.User;
import com.cudpast.app.patientApp.Remote.FCMClient;
import com.cudpast.app.patientApp.Remote.GoogleMapApi;
import com.cudpast.app.patientApp.Remote.IFCMService;
import com.cudpast.app.patientApp.Remote.IGoogleAPI;

public class Common {

    public static final String TB_AVAILABLE_DOCTOR = "TB_AVAILABLE_DOCTOR";
    public static final String TB_AVAILABLE_PLASMA = "TB_AVAILABLE_DOCTOR";
    public static final String TB_INFO_DOCTOR = "tb_Info_Doctor";
    public static final String TB_INFO_PLASMA = "tb_Info_Plasma";
    public static final String TB_INFO_PACIENTE = "tb_Info_Paciente";
    public static final String TB_SERVICIO_DOCTOR_PACIENTE = "TB_SERVICIO_DOCTOR_PACIENTE";
    public static final String token_tbl = "Tokens";

    public static final String AppPaciente_history= "AppPaciente_history";
    public static final String AppDoctor_history= "AppDoctor_history";
    public static final String AppDoctor_history_Comment= "AppDoctor_history_Comment";

    public static final String db_session = "db_session";

    public static Boolean doctorAcept ;

    public static User currentUser;
    public static DoctorPerfil currentDoctor;

    public static Location mLastLocation;
    public static  String token_doctor ;


    public static final String googleAPIUrl = "https://maps.googleapis.com";
    public static final String fcmURL = "https://fcm.googleapis.com/";

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
