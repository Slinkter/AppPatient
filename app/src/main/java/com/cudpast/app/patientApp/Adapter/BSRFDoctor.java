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

import dmax.dialog.SpotsDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BSRFDoctor extends BottomSheetDialogFragment implements LocationListener {

    private static final String TAG = BSRFDoctor.class.getSimpleName();

    public String mTitle, doctorUID, pacienteUID;
    public Double doctorLatitude, doctorLongitud, pacienteLongitud, pacienteLatitude;
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


    TextView xml_countDown;
    //
    CountDownTimer yourCountDownTimer;
    //
    LottieAnimationView animationView;
    long START_TIME_IN_MILLS = 60 * 1000 * 1; // 60 s  5min
    long mTimeLeftInMillis;

    AlertDialog dialog;

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
        auth = FirebaseAuth.getInstance();

        pacienteUID = auth.getCurrentUser().getUid();
        doctorUID = getArguments().getString("doctorUID");
        mTitle = getArguments().getString("title");
        isTapOnMap = getArguments().getBoolean("isTapOnMap");
        doctorLatitude = getArguments().getDouble("doctorLatitude");
        doctorLongitud = getArguments().getDouble("doctorLongitud");
        pacienteLatitude = getArguments().getDouble("pacienteLatitude");
        pacienteLongitud = getArguments().getDouble("pacienteLongitud");

        Log.e(TAG, " onCreate doctorUID :  " + doctorUID);
        Log.e(TAG, " onCreate pacienteUID : " + pacienteUID);

        Log.e(TAG, "title " + mTitle);
        Log.e(TAG, "doctorLatitude " + doctorLatitude);
        Log.e(TAG, "doctorLongitud " + doctorLongitud);
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

        TB_AVAILABLE_DOCTOR = FirebaseDatabase.getInstance().getReference().child(Common.TB_INFO_DOCTOR);
        TB_AVAILABLE_DOCTOR.keepSynced(true);

        post_firstName = view.findViewById(R.id.bs_doctorFirstName);
        post_lastName = view.findViewById(R.id.bs_doctorLastName);
        post_phone = view.findViewById(R.id.bs_doctorPhone);
        post_especialidad = view.findViewById(R.id.bs_doctorEspecialidad);
        post_image = view.findViewById(R.id.bs_doctorImage);

        btn_yes = view.findViewById(R.id.btn_yes);
        btn_no = view.findViewById(R.id.btn_no);

        mFCMService = Common.getIFCMService();

        Log.e(TAG, "pacienteLatitude " + pacienteLatitude);
        Log.e(TAG, "pacienteLongitud " + pacienteLongitud);

        if (isTapOnMap) {
            TB_AVAILABLE_DOCTOR
                    .orderByKey()
                    .equalTo(doctorUID)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            for (DataSnapshot post : dataSnapshot.getChildren()) {
                                DoctorPerfil doctorPerfil = post.getValue(DoctorPerfil.class);
                                if (doctorPerfil != null) {
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
                                //Log
                                Log.e("doctorUID", doctorUID);
                                Log.e("doctorPerfil.uid:", doctorPerfil.getUid());
                                Log.e("doctorPerfil", doctorPerfil.toString());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.e(TAG, "USUARIO NO EXISTE EN LA BASE DE DATOS");
                        }
                    });
            //.------------------->
            btn_yes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendRequestDoctor(doctorUID);
                    showDialog1();

                }
            });
            //.------------------->
            btn_no.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
        }
        return view;
    }

    //
    private void showDialog1() {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            View view = getLayoutInflater().inflate(R.layout.alert_booking, null);
            builder.setView(view);
            builder.setCancelable(false);
            mTimeLeftInMillis = START_TIME_IN_MILLS;
            view.setKeepScreenOn(true);
            btn_s_cancelar = view.findViewById(R.id.btn_s_cancelar);
            Common.doctorAcept = false;

            dialog = builder.create();

            view.findViewById(R.id.animation_view_stopwatch);
            xml_countDown = view.findViewById(R.id.text_view_countDown);


            yourCountDownTimer = new CountDownTimer(mTimeLeftInMillis, 500) {
                @Override
                public void onTick(long millisUntilFinished) {

                    mTimeLeftInMillis = millisUntilFinished;
                    int minutos = (int) (mTimeLeftInMillis / 1000) / 60;
                    int secounds = (int) (mTimeLeftInMillis / 1000) % 60;
                    String timeFormated = String.format(Locale.getDefault(), "%02d:%02d", minutos, secounds);
                    xml_countDown.setText(timeFormated);
                    Log.e("onTick", " : mTimeLeftInMillis = " + mTimeLeftInMillis);
                }


                @Override
                public void onFinish() {
                    Log.e(TAG, " ==============================");
                    try {
                        Log.e(TAG, " onFinish()");
                        Log.e(TAG, " mTimeLeftInMillis : " + mTimeLeftInMillis);
                        Log.e(TAG, " START_TIME_IN_MILLS : " + START_TIME_IN_MILLS);
                        //  mTimeLeftInMillis = START_TIME_IN_MILLS;
                        //  mTimeLeftInMillis=0;
                        dialog.dismiss();
                        dialog.cancel();
                        dismiss();
                        //Enviar Notificacion-Data
                        timeOutRequestDoctor(doctorUID);
                        yourCountDownTimer.cancel();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Log.e(TAG, " ==============================");

                }
            };
            yourCountDownTimer.start();

            if (Common.doctorAcept == true) {
                Log.e(TAG, " Common.doctorAcept : " + Common.doctorAcept);
                Log.e(TAG, " yourCountDownTimer  : " + mTimeLeftInMillis);
                //  yourCountDownTimer.cancel();
                dialog.dismiss();
                dialog.cancel();
                dismiss();

            }

            btn_s_cancelar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cancelRequestDoctor(doctorUID);
                    dialog.dismiss();
                    yourCountDownTimer.cancel();
                    dismiss();
                }
            });
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    //.Caso 1
    private void sendRequestDoctor(String doctorUID) {
        Log.e(TAG, "======================================================");
        Log.e(TAG, "             sendRequestDoctor                    ");
        //
        final SpotsDialog waitingDialog = new SpotsDialog(getContext(), R.style.DialogLogin);
        waitingDialog.show();
        //
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(Common.token_tbl);
        tokens
                .orderByKey()
                .equalTo(doctorUID)
                .addValueEventListener
                        (new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot postSnapShot : dataSnapshot.getChildren()) {
                                    //convert to LatLng to json.
                                    LatLng userGeo = new LatLng(pacienteLatitude, pacienteLongitud);
                                    Token tokenDoctor = postSnapShot.getValue(Token.class);
                                    Log.e(TAG, " : Token tokenDoctor = " + tokenDoctor.getToken());
                                    //.Pre-envio-data
                                    String title = "App Doctor";
                                    String body = "Usted tiene una solicutud de atención";
                                    String dToken = tokenDoctor.getToken();//doctor token
                                    String pToken = FirebaseInstanceId.getInstance().getToken(); //todo: paciente token corregir
                                    String json_lat_lng = new Gson().toJson(userGeo);
                                    //.Data
                                    Data data = new Data(title, body, pToken, dToken, json_lat_lng, pacienteUID);
                                    //Sender (to:token,data:información_del_paciente)
                                    Sender sender = new Sender(dToken, data);
                                    //.Log
                                    Log.e(TAG, "title : " + title);
                                    Log.e(TAG, "body : " + body);
                                    Log.e(TAG, "doctorToken : " + dToken);
                                    Log.e(TAG, "pacienteToken : " + pToken);
                                    Log.e(TAG, "ubicacion de paciente : " + json_lat_lng);
                                    Log.e(TAG, "pacienteUID : " + pacienteUID);

                                    mFCMService
                                            .sendMessage(sender)
                                            .enqueue(new Callback<FCMResponse>() {
                                                @Override
                                                public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                                                    if (response.body().success == 1) {
                                                        waitingDialog.dismiss();
                                                        Log.e(TAG, "onResponse: success Caso 1");
                                                        Log.e(TAG, "======================================================");
                                                    }
                                                }

                                                @Override
                                                public void onFailure(Call<FCMResponse> call, Throwable t) {
                                                    waitingDialog.dismiss();
                                                    Log.e(TAG, "onFailure : " + t.getMessage());
                                                    Log.e(TAG, "======================================================");
                                                }
                                            });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                waitingDialog.dismiss();
                                Log.e(TAG, " onCancelled" + databaseError.getMessage());
                                Log.e(TAG, "======================================================");
                            }
                        });
    }

    //.Caso 2
    private void cancelRequestDoctor(String driverID) {
        Log.e(TAG, "======================================================");
        Log.e(TAG, "             cancelRequestDoctor                    ");
        final SpotsDialog waitingDialog = new SpotsDialog(getContext(), R.style.DialogLogin);
        waitingDialog.show();
        yourCountDownTimer.cancel();
        Log.e(TAG, "cancelRequestDoctor : mTimeLeftInMillis = " + mTimeLeftInMillis);
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(Common.token_tbl);
        tokens
                .orderByKey()
                .equalTo(driverID)
                .addValueEventListener(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot postSnapShot : dataSnapshot.getChildren()) {

                                    Token tokenDoctor = postSnapShot.getValue(Token.class);

                                    if (tokenDoctor.getToken() != null) {
                                        Log.e(TAG, "cancelRequestDoctor : Token tokenDoctor = " + tokenDoctor.getToken());
                                        String dToken = tokenDoctor.getToken();
                                        String title = "App Doctor";
                                        String body = "El usuario ha cancelado";
                                        //--->Data
                                        Data data = new Data(title, body, " ", " ", "", "");
                                        //-->Sender (to, data)
                                        Sender sender = new Sender(dToken, data);
                                        mFCMService
                                                .sendMessage(sender)
                                                .enqueue(new Callback<FCMResponse>() {
                                                    @Override
                                                    public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                                                        if (response.body().success == 1) {
                                                            Log.e(TAG, "SI se ha enviado correctamente notifación de cancelación ");
                                                            waitingDialog.dismiss();
                                                        } else {
                                                            Log.e(TAG, "NO se ha enviado correctamente notifación de cancelación ");
                                                            waitingDialog.dismiss();
                                                        }
                                                        Log.e(TAG, "======================================================");
                                                    }

                                                    @Override
                                                    public void onFailure(Call<FCMResponse> call, Throwable t) {
                                                        Log.e(TAG, "onFailure : " + t.getMessage());
                                                        waitingDialog.dismiss();
                                                    }
                                                });
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                waitingDialog.dismiss();
                                Log.e(TAG, " onCancelled : " + databaseError.getMessage());
                                Log.e(TAG, "======================================================");
                            }
                        });
    }

    //.Caso 3
    private void timeOutRequestDoctor(String driverID) {
        Log.e(TAG, "======================================================");
        Log.e(TAG, "             timeOutRequestDoctor                    ");
        final SpotsDialog waitingDialog = new SpotsDialog(getContext(), R.style.DialogLogin);
        waitingDialog.show();
        yourCountDownTimer.cancel();

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
                                                waitingDialog.dismiss();
                                                Log.e(TAG, "onResponse: success - timeOutRequestDoctor() ");
                                                Log.e(TAG, "======================================================");
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<FCMResponse> call, Throwable t) {
                                            waitingDialog.dismiss();
                                            Log.e(TAG, "onFailure : " + t.getMessage());
                                            Log.e(TAG, "======================================================");
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        waitingDialog.dismiss();
                        Log.e(TAG, " onCancelled" + databaseError.getMessage());
                        Log.e(TAG, "======================================================");
                    }
                });
    }
}
