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
import com.cudpast.app.patientApp.Model.DoctorProfile;
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

    public String mTitle, doctorUID, uid_paciente;
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
    long START_TIME_IN_MILLS = 60 * 1000 * 5; // 60 s  5min
    long mTimeLeftInMillis;



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
        Common.doctorAcept = false;
        isTapOnMap = getArguments().getBoolean("isTapOnMap");
        //
        uid_paciente = auth.getCurrentUser().getUid();
        doctorUID = getArguments().getString("doctorUID");
        mTitle = getArguments().getString("title");
        doctorLatitude = getArguments().getDouble("doctorLatitude");
        doctorLongitud = getArguments().getDouble("doctorLongitud");
        pacienteLatitude = getArguments().getDouble("pacienteLatitude");
        pacienteLongitud = getArguments().getDouble("pacienteLongitud");

        Log.e(TAG, "======================================");
        Log.e(TAG, "----------> onCreate");
        Log.e(TAG, " uid_paciente : " + uid_paciente);
        Log.e(TAG, " uid_doctor :  " + doctorUID);
        Log.e(TAG, " title : " + mTitle);
        Log.e(TAG, " doctorLatitude = " + doctorLatitude);
        Log.e(TAG, " doctorLongitud = " + doctorLongitud);
        Log.e(TAG, " pacienteLatitude = " + pacienteLatitude);
        Log.e(TAG, " pacienteLongitud = " + pacienteLongitud);

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

        if (isTapOnMap) {
            TB_AVAILABLE_DOCTOR
                    .orderByKey()
                    .equalTo(doctorUID)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            for (DataSnapshot post : dataSnapshot.getChildren()) {
                                DoctorProfile doctorProfile = post.getValue(DoctorProfile.class);
                                if (doctorProfile != null) {
                                    post_firstName.setText(doctorProfile.getFirstname());
                                    post_lastName.setText(doctorProfile.getLastname());
                                    post_phone.setText(doctorProfile.getNumphone());
                                    post_especialidad.setText(doctorProfile.getEspecialidad());
                                    Picasso.with(getContext())
                                            .load(doctorProfile.getImagePhoto())
                                            .resize(200, 200)
                                            .centerInside().
                                            into(post_image);
                                }
                                //
                                Log.e("doctorUID", doctorUID);
                                Log.e("doctorProfile.uid:", doctorProfile.getUid());
                                Log.e("doctorProfile", doctorProfile.toString());
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
            LayoutInflater inflater = getLayoutInflater();
            View view = inflater.inflate(R.layout.alert_booking, null);
            builder.setView(view);
            builder.setCancelable(false);
            view.setKeepScreenOn(true);


            final AlertDialog dialog = builder.create();
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


            mTimeLeftInMillis = START_TIME_IN_MILLS;
            Common.doctorAcept = false;
            Common.doctorAcept = false;
        //    view.findViewById(R.id.animation_view_stopwatch);
            btn_s_cancelar = view.findViewById(R.id.btn_s_cancelar);
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
                    Log.e("onTick", " : Common.doctorAcept = " + Common.doctorAcept);
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
            yourCountDownTimer.start();// on Tick

            if (Common.doctorAcept) {
                Log.e(TAG, " Common.doctorAcept : " + Common.doctorAcept);
                Log.e(TAG, " yourCountDownTimer  : " + mTimeLeftInMillis);
                yourCountDownTimer.cancel();
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


            dialog.show();
        } catch (Exception  e) {
            e.printStackTrace();
        }


    }

    //.Caso 1
    private void sendRequestDoctor(String uid_doctor) {
        Log.e(TAG, "======================================================");
        Log.e(TAG, "--------------> sendRequestDoctor                    ");
        //Obtener token del doctor a travez de su UID
        DatabaseReference refDB_tokens = FirebaseDatabase.getInstance().getReference(Common.token_tbl);
        //
        final SpotsDialog waitingDialog = new SpotsDialog(getContext(), R.style.DialogResetearPassword);
        waitingDialog.show();

        refDB_tokens
                .orderByKey()
                .equalTo(uid_doctor)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapShot : dataSnapshot.getChildren()) {
                            //convert to LatLng to json.
                            LatLng userGeo = new LatLng(pacienteLatitude, pacienteLongitud);
                            Token token_doctor = postSnapShot.getValue(Token.class);
                            Log.e(TAG, " : Token token_doctor = " + token_doctor.getToken());
                            //.Pre-envio-data
                            String title = "App Doctor";
                            String body = "Usted tiene una solicutud de atención";
                            String dToken = token_doctor.getToken();
                            String pToken = FirebaseInstanceId.getInstance().getToken();
                            String json_lat_lng = new Gson().toJson(userGeo);
                            //.Data
                            Data data = new Data(title, body, pToken, dToken, json_lat_lng, uid_paciente);
                            //Sender (to:token,data:información_del_paciente)
                            Sender sender = new Sender(dToken, data);
                            //.Log
                            Log.e(TAG, "title : " + title);
                            Log.e(TAG, "body : " + body);
                            Log.e(TAG, "token_doctor : " + dToken);
                            Log.e(TAG, "token_paciente : " + pToken);
                            Log.e(TAG, "ubicacion de paciente : " + json_lat_lng);
                            Log.e(TAG, "uid_paciente : " + uid_paciente);

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
