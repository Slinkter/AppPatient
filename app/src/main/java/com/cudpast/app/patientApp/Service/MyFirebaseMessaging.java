package com.cudpast.app.patientApp.Service;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.cudpast.app.patientApp.Activities.UbicacionActivity;
import com.cudpast.app.patientApp.Business.DoctorEnd;
import com.cudpast.app.patientApp.Business.DoctorRoad;
import com.cudpast.app.patientApp.Common.Common;
import com.cudpast.app.patientApp.helper.Token;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessaging extends FirebaseMessagingService {

    private static String TAG = MyFirebaseMessaging.class.getSimpleName();

    @Override
    public void onMessageReceived(final RemoteMessage remoteMessage) {

        if (remoteMessage.getData() != null) {
            Log.e(TAG, "==========================================");
            Log.e(TAG, "          MyFirebaseMessaging             ");

            String caso_1 = "Acepta";
            String caso_2 = "Cancel";
            String caso_3 = "Arrived";

            String body = remoteMessage.getData().get("body");

            if (body.equalsIgnoreCase(caso_1) ) {
                showAceptedNotification(remoteMessage);
            } else if (body.equalsIgnoreCase(caso_2)) {
                showCancelNotification(remoteMessage);
            } else if (body.equalsIgnoreCase(caso_3)) {
                showArrivedNotification(remoteMessage);
            }
        }

    }
    private void showAceptedNotification(RemoteMessage message) {
        Log.e(TAG,"===============================================");
        Log.e(TAG, "          showAceptedNotification             ");
        mostrarMensaje(message);
        Common.doctorAcept = true;
        String firebaseDoctorUID = message.getData().get("dToken");
        //
        Intent intent = new Intent(MyFirebaseMessaging.this, DoctorRoad.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("firebaseDoctorUID", firebaseDoctorUID);
        startActivity(intent);
        Log.e(TAG,"===============================================");
    }

    private void showCancelNotification(RemoteMessage message) {
        //todo En la appDoctor , el doctor se pone off pero en appPaciente no actualiza esto
        Log.e(TAG, "          showCancelNotification             ");
        mostrarMensaje(message);
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {

                Toast.makeText(MyFirebaseMessaging.this, "" + "", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), UbicacionActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
    }

    private void showArrivedNotification(RemoteMessage message) {


        Log.e(TAG, "          showArrivedNotification             ");
        mostrarMensaje(message);
        Intent intent = new Intent(MyFirebaseMessaging.this, DoctorEnd.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);


    }



    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        DatabaseReference tb_tokens = FirebaseDatabase.getInstance().getReference(Common.token_tbl);
        Token token = new Token(s);
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            tb_tokens.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(token);
        }

    }


    public void mostrarMensaje(RemoteMessage message){

        Log.e(TAG, "title : " + message.getData().get("title"));
        Log.e(TAG, "body : " + message.getData().get("body"));
        Log.e(TAG, "pToken : " + message.getData().get("pToken"));
        Log.e(TAG, "dToken : " + message.getData().get("dToken"));
        Log.e(TAG, "json_lat_log : " + message.getData().get("json_lat_log"));
        Log.e(TAG, "pacienteUID : " + message.getData().get("pacienteUID"));
    }


}
