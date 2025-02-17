package com.cudpast.app.patientApp.Service;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.cudpast.app.patientApp.Business.DoctorCancel;
import com.cudpast.app.patientApp.Business.DoctorEnd;
import com.cudpast.app.patientApp.Business.DoctorRoad;
import com.cudpast.app.patientApp.Common.Common;
import com.cudpast.app.patientApp.helper.Token;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessaging extends FirebaseMessagingService {

    private static String TAG = MyFirebaseMessaging.class.getSimpleName();
    public static final String APP_CHANNEL_ID = "Default";
    public static final String APP_CHANNEL_NAME = "App Channel";

    @Override
    public void onMessageReceived(final RemoteMessage remoteMessage) {

        if (remoteMessage.getData() != null) {
            Log.e(TAG, "==========================================");
            Log.e(TAG, "          MyFirebaseMessaging             ");

            String caso_1 = "Acepta";
            String caso_2 = "rechaza";
            String caso_3 = "Arrived";
            String caso_4 = "Cancel";

            String body = remoteMessage.getData().get("body");

            if (body != null) {
                if (body.equalsIgnoreCase(caso_1)) {
                    showAceptedBooking(remoteMessage);
                } else if (body.equalsIgnoreCase(caso_2)) {
                    showNotAceptBooking(remoteMessage);
                } else if (body.equalsIgnoreCase(caso_3)) {
                    showDoctorArrived(remoteMessage);
                } else if (body.equalsIgnoreCase(caso_4)) {
                    showDoctorCancelOnRoad(remoteMessage);
                }
            }
        }
    }

    // Caso 1 : El Docto Acepta
    private void showAceptedBooking(RemoteMessage message) {
        Log.e(TAG, "===============================================");
        Log.e(TAG, "Caso 1 : El doctor Acepta  ");
        mostrarMensaje(message);
        Common.doctorAcept = true;
        String firebaseDoctorUID = message.getData().get("dToken");// no es el token es el UID del doctor usuario
        Intent intent = new Intent(MyFirebaseMessaging.this, DoctorRoad.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("firebaseDoctorUID", firebaseDoctorUID);
        startActivity(intent);
        Log.e(TAG, "========================================================");
    }

    // Caso 2 : El Docto rechaza
    private void showNotAceptBooking(RemoteMessage message) {

        Log.e(TAG, "Caso 2 : showNotAceptBooking             ");
        mostrarMensaje(message);
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MyFirebaseMessaging.this, "Cancelado", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), DoctorCancel.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

            }
        });
    }

    // Caso 3 : El Doctor Llega al domicilio del paciente
    private void showDoctorArrived(RemoteMessage message) {
        Log.e(TAG, "Caso 3 : El doctor ha llegado a tu domicilio             ");
        mostrarMensaje(message);
        String firebaseDoctorUID = message.getData().get("dToken");// no es el token es el UID del doctor usuario
        Intent intent = new Intent(MyFirebaseMessaging.this, DoctorEnd.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("firebaseDoctorUID", firebaseDoctorUID);
        startActivity(intent);
    }

    // Caso 4 : El Doctor cancela en pleno servicio
    private void showDoctorCancelOnRoad(RemoteMessage message) {
        Log.e(TAG, "========================================================");
        Log.e(TAG, "Caso 4 : El doctor ha cancelado en pleno viaje ");
        Intent intent = new Intent(MyFirebaseMessaging.this, DoctorCancel.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        mostrarMensaje(message);
        startActivity(intent);
        Log.e(TAG, "========================================================");
    }

    @Override
    public void onNewToken(String token) {

        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        Log.e(TAG, "Refreshed token: " + token);
        Log.e(TAG, "Refreshed token: " + refreshedToken);

        updateTokenToServer(token);
    }

    private void updateTokenToServer(String refreshedToken) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(Common.token_tbl);
        Token token = new Token(refreshedToken);
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            tokens.child(FirebaseAuth
                    .getInstance()
                    .getCurrentUser()
                    .getUid())
                    .setValue(token);
        }

    }


    public void mostrarMensaje(RemoteMessage message) {

        Log.e(TAG, "title : " + message.getData().get("title"));
        Log.e(TAG, "body : " + message.getData().get("body"));
        Log.e(TAG, "pToken : " + message.getData().get("pToken"));
        Log.e(TAG, "dToken : " + message.getData().get("dToken"));
        Log.e(TAG, "json_lat_log : " + message.getData().get("json_lat_log"));
        Log.e(TAG, "uid_paciente : " + message.getData().get("uid_paciente"));
    }


}
