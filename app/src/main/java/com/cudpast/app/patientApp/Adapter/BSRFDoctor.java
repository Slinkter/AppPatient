package com.cudpast.app.patientApp.Adapter;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;

import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.cudpast.app.patientApp.Common.Common;
import com.cudpast.app.patientApp.Model.DoctorPerfil;
import com.cudpast.app.patientApp.R;
import com.cudpast.app.patientApp.Remote.IFCMService;
import com.cudpast.app.patientApp.helper.Data;
import com.cudpast.app.patientApp.helper.FCMResponse;
import com.cudpast.app.patientApp.helper.Sender;
import com.cudpast.app.patientApp.helper.Token;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.model.LatLng;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import com.google.gson.Gson;

import com.squareup.picasso.Picasso;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BSRFDoctor extends BottomSheetDialogFragment implements LocationListener {

    private static final String TAG = BSRFDoctor.class.getSimpleName();

    public String mTitle, doctorUID, pacienteUID;
    public Double mLatitude, mLongitud, pacienteLongitud, pacienteLatitude;
    boolean isTapOnMap;
    public DatabaseReference TB_AVAILABLE_DOCTOR;
    public FirebaseAuth auth;

    Button btn_yes, btn_no, btn_s_cancelar;

    TextView post_firstName;
    TextView post_lastName;
    TextView post_phone;
    TextView post_especialidad;
    ImageView post_image;
    Location mLastLocation;
    IFCMService mFCMService;
    String driverID;

    TextView xml_countDown;
    LottieAnimationView animationView;
    long START_TIME_IN_MILLS = 60 * 1000 * 2; // 60 s  5min
    long mTimeLeftInMillis = START_TIME_IN_MILLS;


    //Constructor
    public static BSRFDoctor newInstance(String title, String doctorUID, boolean isTapOnMap, Double doctorLatitude, Double doctorLongitud, Double pacienteLatitude, Double pacienteLongitud) {
        BSRFDoctor f = new BSRFDoctor();
        Bundle args = new Bundle();
        //--->
        args.putString("title", title);
        args.putString("doctorUID", doctorUID);
        args.putBoolean("isTapOnMap", isTapOnMap);
        args.putDouble("doctorLatitude", doctorLatitude);
        args.putDouble("doctorLongitud", doctorLongitud);
        args.putDouble("pacienteLatitude", pacienteLatitude);
        args.putDouble("pacienteLongitud", pacienteLongitud);
        //<---
        f.setArguments(args);
        return f;
    }

    // Get Info
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTitle = getArguments().getString("title");
        doctorUID = getArguments().getString("doctorUID");

        isTapOnMap = getArguments().getBoolean("isTapOnMap");

        mLatitude = getArguments().getDouble("doctorLatitude");
        mLongitud = getArguments().getDouble("doctorLongitud");

        pacienteLatitude = getArguments().getDouble("pacienteLatitude");
        pacienteLongitud = getArguments().getDouble("pacienteLongitud");

        auth = FirebaseAuth.getInstance();
        pacienteUID = auth.getCurrentUser().getUid();

        Log.e(TAG, "title " + mTitle);

        Log.e(TAG, "doctorUID " + doctorUID);
        Log.e(TAG, "pacienteUID " + pacienteUID);

        Log.e(TAG, "doctorLatitude " + mLatitude);
        Log.e(TAG, "doctorLongitud " + mLongitud);

        Log.e(TAG, "pacienteLatitude " + pacienteLatitude);
        Log.e(TAG, "pacienteLongitud " + pacienteLongitud);
    }

    //Location
    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.bsrfdoctor, container, false);

        //.Obtener Toda tabla de doctore online
        TB_AVAILABLE_DOCTOR = FirebaseDatabase.getInstance().getReference().child("tb_Info_Doctor");
        TB_AVAILABLE_DOCTOR.keepSynced(true);

        post_firstName = view.findViewById(R.id.bs_doctorFirstName);
        post_lastName = view.findViewById(R.id.bs_doctorLastName);
        post_phone = view.findViewById(R.id.bs_doctorPhone);
        post_especialidad = view.findViewById(R.id.bs_doctorEspecialidad);
        post_image = view.findViewById(R.id.bs_doctorImage);

        btn_yes = view.findViewById(R.id.btn_yes);
        btn_no = view.findViewById(R.id.btn_no);

        Log.e(TAG, "pacienteLatitude " + pacienteLatitude);
        Log.e(TAG, "pacienteLongitud " + pacienteLongitud);
        mFCMService = Common.getIFCMService();
        driverID = doctorUID;

        if (isTapOnMap) {

            TB_AVAILABLE_DOCTOR
                    .orderByKey()
                    .equalTo(driverID)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            DoctorPerfil doctorPerfil;
                            for (DataSnapshot post : dataSnapshot.getChildren()) {

                                doctorPerfil = post.getValue(DoctorPerfil.class);
                                Log.e("driverID", driverID);
                                Log.e("doctorPerfil.uid:", doctorPerfil.getUid());
                                Log.e("doctorPerfil", doctorPerfil.toString());

                                post_firstName.setText(doctorPerfil.getFirstname());
                                post_lastName.setText(doctorPerfil.getLastname());
                                post_phone.setText(doctorPerfil.getNumphone());
                                post_especialidad.setText(doctorPerfil.getEspecialidad());
                                Picasso.with(getContext())
                                        .load(doctorPerfil.getImage())
                                        .resize(300, 300)
                                        .centerInside().
                                        into(post_image);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
            //.------------------->
            btn_yes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendRequestDoctor(driverID);
                    showDialog1();

                    //todo : si se cierra el showDialog enviar
                }
            });
            //.------------------->
            btn_no.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getContext(), "Cancelado", Toast.LENGTH_SHORT).show();
                    dismiss();
                }
            });
        }
        return view;
    }


    //.
    private void sendRequestDoctor(String doctorUID) {
        Log.e(TAG, "======================================================");
        Log.e(TAG, "             sendRequestDoctor                    ");
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(Common.token_tbl);
        tokens
                .orderByKey()
                .equalTo(doctorUID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapShot : dataSnapshot.getChildren()) {
                            //convert to LatLng to json.
                            LatLng userGeo = new LatLng(pacienteLatitude, pacienteLongitud);
                            Token tokenDoctor = postSnapShot.getValue(Token.class);
                            //.Pre-envio-data
                            String title = "App Doctor";
                            String body = "Usted tiene una solicutud de atención";
                            String dToken = tokenDoctor.getToken();//doctor token
                            String pToken = FirebaseInstanceId.getInstance().getToken(); // paciente token
                            String json_lat_lng = new Gson().toJson(userGeo);
                            //.Data
                            Data data = new Data(title, body, pToken, dToken, json_lat_lng, pacienteUID);
                            //.Log
                            Log.e(TAG, "title : " + title);
                            Log.e(TAG, "body : " + body);
                            Log.e(TAG, "doctorToken : " + dToken);
                            Log.e(TAG, "pacienteToken : " + pToken);
                            Log.e(TAG, "ubicacion de paciente : " + json_lat_lng);
                            Log.e(TAG, "pacienteUID : " + pacienteUID);
                            //Sender (to:token,data:informacion_del_paciente)
                            Sender sender = new Sender(dToken, data);
                            mFCMService
                                    .sendMessage(sender)
                                    .enqueue(new Callback<FCMResponse>() {
                                        @Override
                                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                                            if (response.body().success == 1) {
                                                Log.e(TAG, "onResponse: success");
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<FCMResponse> call, Throwable t) {
                                            Log.e(TAG, "onFailure : " + t.getMessage());
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e(TAG, " onCancelled" + databaseError.getMessage());
                    }
                });

        Log.e(TAG, "======================================================");
    }

    //.

    private void cancelRequestDoctor(String driverID) {
        Log.e(TAG, "======================================================");
        Log.e(TAG, "             cancelRequestDoctor                    ");
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(Common.token_tbl);
        tokens
                .orderByKey()
                .equalTo(driverID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapShot : dataSnapshot.getChildren()) {
                            //convert to LatLng to json.
                            Token tokenDoctor = postSnapShot.getValue(Token.class);
                            // pre-envio-data
                            String dToken = tokenDoctor.getToken();
                            String title = "App Doctor";
                            String body = "El usuario ha cancelado";
                            //Data
                            Data data = new Data(title, body, " ", " ", "", "");
                            //Sender (to, data)
                            Sender sender = new Sender(dToken, data);
                            mFCMService
                                    .sendMessage(sender)
                                    .enqueue(new Callback<FCMResponse>() {
                                        @Override
                                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                                            if (response.body().success == 1) {
                                                Log.e(TAG, "onResponse: success  cancelRequestDoctor() ");
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<FCMResponse> call, Throwable t) {
                                            Log.e(TAG, "onFailure : " + t.getMessage());
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e(TAG, " onCancelled" + databaseError.getMessage());
                    }
                });

        Log.e(TAG, "======================================================");
    }
    //.
    private void timeOutRequestDoctor(String driverID) {
        Log.e(TAG, "======================================================");
        Log.e(TAG, "             cancelRequestDoctor                    ");
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(Common.token_tbl);
        tokens
                .orderByKey()
                .equalTo(driverID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapShot : dataSnapshot.getChildren()) {
                            //convert to LatLng to json.
                            Token tokenDoctor = postSnapShot.getValue(Token.class);
                            // pre-envio-data
                            String dToken = tokenDoctor.getToken();
                            String title = "App Doctor";
                            String body = "Tiempo fuera";
                            //Data
                            Data data = new Data(title, body, " ", " ", "", "");
                            //Sender (to, data)
                            Sender sender = new Sender(dToken, data);
                            mFCMService
                                    .sendMessage(sender)
                                    .enqueue(new Callback<FCMResponse>() {
                                        @Override
                                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                                            if (response.body().success == 1) {
                                                Log.e(TAG, "onResponse: success  cancelRequestDoctor() ");
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<FCMResponse> call, Throwable t) {
                                            Log.e(TAG, "onFailure : " + t.getMessage());
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e(TAG, " onCancelled" + databaseError.getMessage());
                    }
                });

        Log.e(TAG, "======================================================");
    }

    //
    private void showDialog1() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getLayoutInflater();
        final View view = inflater.inflate(R.layout.alert_booking, null);
        builder.setView(view);
        builder.setCancelable(false);
        view.setKeepScreenOn(true);


        final AlertDialog dialog = builder.create();

        view.findViewById(R.id.animation_view);
        xml_countDown = view.findViewById(R.id.text_view_countDown);
        Common.token_doctor = driverID;

        Toast.makeText(getContext(), "Solicitando atención", Toast.LENGTH_SHORT).show();

        new CountDownTimer(mTimeLeftInMillis, 500) {
            @Override
            public void onTick(long millisUntilFinished) {
                // millisUntilFinished = mTimeLeftInMillis - 500
                mTimeLeftInMillis = millisUntilFinished;
                int minutos = (int) (mTimeLeftInMillis / 1000) / 60;
                int secounds = (int) (mTimeLeftInMillis / 1000) % 60;
                String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutos, secounds);
                Log.e(TAG, " mTimeLeftInMillis : " + mTimeLeftInMillis);
                xml_countDown.setText(timeLeftFormatted);
            }

            //todo : aun envia
            @Override
            public void onFinish() {
                Log.e(TAG, " ==============================");
                try {

                    Log.e(TAG, " onFinish()");
                    Log.e(TAG, " mTimeLeftInMillis : " + mTimeLeftInMillis);
                    Log.e(TAG, " START_TIME_IN_MILLS : " + START_TIME_IN_MILLS);
                    // mTimeLeftInMillis = START_TIME_IN_MILLS;
                    dialog.dismiss();
                    dismiss();

                    //Enviar Notificacion-Data
                    timeOutRequestDoctor(driverID);


                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.e(TAG, " ==============================");

            }
        }.start();

        btn_s_cancelar = view.findViewById(R.id.btn_s_cancelar);

        btn_s_cancelar
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(view.getContext(), "Cancelado", Toast.LENGTH_SHORT).show();
                        xml_countDown.setText("");
                        dismiss();
                        dialog.dismiss();
                        cancelRequestDoctor(driverID);
                    }
                });


        try {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


}
