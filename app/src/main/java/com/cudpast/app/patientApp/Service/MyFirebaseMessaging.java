package com.cudpast.app.patientApp.Service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.cudpast.app.patientApp.Common.Common;
import com.cudpast.app.patientApp.R;
import com.cudpast.app.patientApp.helper.Token;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessaging  extends FirebaseMessagingService {


    @Override
    public void onMessageReceived(final RemoteMessage remoteMessage) {

        if (remoteMessage.getNotification().getTitle().equals("Cancel")){
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {

                    Toast.makeText(MyFirebaseMessaging.this, "" + remoteMessage.getNotification().getBody(), Toast.LENGTH_SHORT).show();

                }
            });
        }else  if (remoteMessage.getNotification().getTitle().equals("Arrived")){
            showArrivedNotification(remoteMessage.getNotification().getBody());
        }
    }


    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);

        String refreshedToken1 = FirebaseInstanceId.getInstance().getToken();
        String refreshedToken2 = FirebaseInstanceId.getInstance().getInstanceId().toString();


        Log.e("onNewToke        : ",s);
        Log.e("refreshedToken1  : ",refreshedToken1);
        Log.e("refreshedToken2  : ",refreshedToken2);

        DatabaseReference tb_tokens = FirebaseDatabase.getInstance().getReference(Common.token_tbl);

        Token token = new Token(s);

        if (FirebaseAuth.getInstance().getCurrentUser() !=null){
            tb_tokens.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(token);

        }

    }

    private void showArrivedNotification(String body) {
        //Only version 25
        //Create Canal de Notificacion > 26
        PendingIntent contentIntent = PendingIntent.getActivity(getBaseContext(),0,new Intent(),PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getBaseContext());

        builder.setAutoCancel(true)
                .setDefaults(android.app.Notification.DEFAULT_LIGHTS|android.app.Notification.DEFAULT_SOUND)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle("Arriver")
                .setContentText(body)
                .setContentIntent(contentIntent);
        NotificationManager manager = (NotificationManager) getBaseContext().getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(1,builder.build());

    }
}
