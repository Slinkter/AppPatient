package com.cudpast.app.patientApp.Adapter;


import android.app.Dialog;
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
import com.cudpast.app.patientApp.helper.Notification;
import com.cudpast.app.patientApp.helper.Sender;
import com.cudpast.app.patientApp.helper.Token;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.model.LatLng;

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
    private String mTitle, mSnippet;
    private Double mLatitude, mLongitud, pacienteLongitud, pacienteLatitude;
    boolean isTapOnMap;
    private DatabaseReference mDatabase;
    TextView title, snippet;
    Button btn_yes, btn_no;
    TextView post_firstName;
    TextView post_lastName;
    TextView post_phone;
    TextView post_especialidad;
    ImageView post_image;
    Location mLastLocation;
    IFCMService mService;
    String driverID;

    //.GIF Dialog
    Dialog myDialog;
    LottieAnimationView animationView;
    long START_TIME_IN_MILLS = 60000;
    long mTimeLeftInMillis = START_TIME_IN_MILLS;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTitle = getArguments().getString("title");
        mSnippet = getArguments().getString("snippet");
        isTapOnMap = getArguments().getBoolean("isTapOnMap");
        mLatitude = getArguments().getDouble("latitude");
        mLongitud = getArguments().getDouble("longitud");

        pacienteLatitude = getArguments().getDouble("pacienteLatitude");
        pacienteLongitud = getArguments().getDouble("pacienteLongitud");


    }

    //Constructor
    public static BSRFDoctor newInstance(String title, String snippet, boolean isTapOnMap, Double doctorLatitude, Double doctorLongitud, Double pacienteLatitude, Double pacienteLongitud) {
        BSRFDoctor f = new BSRFDoctor();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("snippet", snippet);
        args.putBoolean("isTapOnMap", isTapOnMap);
        args.putDouble("doctorLatitude", doctorLatitude);
        args.putDouble("doctorLongitud", doctorLongitud);
        args.putDouble("pacienteLatitude", pacienteLatitude);
        args.putDouble("pacienteLongitud", pacienteLongitud);

        f.setArguments(args);
        return f;

    }


    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.botton_sheet_doctor, container, false);

        // Construct a FusedLocationProviderClient.


        mDatabase = FirebaseDatabase.getInstance().getReference().child("tb_Info_Doctor");
        mDatabase.keepSynced(true);

        // llenar to_do el xml
        title = (TextView) view.findViewById(R.id.txt_doctor_title);
        snippet = (TextView) view.findViewById(R.id.txt_doctor_snippet);

        btn_yes = view.findViewById(R.id.btn_yes);
        btn_no = view.findViewById(R.id.btn_no);

        post_firstName = view.findViewById(R.id.bs_doctorFirstName);
        post_lastName = view.findViewById(R.id.bs_doctorLastName);
        post_phone = view.findViewById(R.id.bs_doctorPhone);
        post_especialidad = view.findViewById(R.id.bs_doctorEspecialidad);
        post_image = view.findViewById(R.id.bs_doctorImage);

        mService = Common.getIFCMService();

        driverID = mSnippet;


        Log.e(TAG, "pacienteLatitude " + pacienteLatitude);
        Log.e(TAG, "pacienteLongitud " + pacienteLongitud);


        if (!isTapOnMap) {

        } else {

            mDatabase
                    .orderByKey()
                    .equalTo(driverID)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Log.e("driverID", driverID);
                            DoctorPerfil doctorPerfil;
                            for (DataSnapshot post : dataSnapshot.getChildren()) {
                                doctorPerfil = post.getValue(DoctorPerfil.class);
                                Log.e("doctorPerfil.uid:", doctorPerfil.getUid());
                                Log.e("doctorPerfil", doctorPerfil.toString());

                                title.setText(doctorPerfil.getFirstname());
                                snippet.setText(doctorPerfil.getCorreoG());

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

            myDialog = new Dialog(getContext());
            myDialog.setContentView(R.layout.pop_up_doctor);
            myDialog.findViewById(R.id.animation_view);
            final TextView mTextViewCountDown = myDialog.findViewById(R.id.text_view_countDown);


            btn_yes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Toast.makeText(getContext(), "si", Toast.LENGTH_SHORT).show();
                    Log.e("driverID", driverID);
                    sendRequestToDriver(driverID);
                    dismiss();
                    //
//                    myDialog = new Dialog(getContext());
//                    myDialog.setContentView(R.layout.pop_up_doctor);
//                    myDialog.findViewById(R.id.animation_view);
//                    final TextView mTextViewCountDown = myDialog.findViewById(R.id.text_view_countDown);

                    new CountDownTimer(mTimeLeftInMillis, 500) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            mTimeLeftInMillis = millisUntilFinished;
                            int minutos = (int) (mTimeLeftInMillis / 1000) / 60;
                            int secounds = (int) (mTimeLeftInMillis / 1000) % 60;
                            String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutos, secounds);
                            mTextViewCountDown.setText(timeLeftFormatted);
                        }

                        @Override
                        public void onFinish() {
                            myDialog.dismiss();
                            mTimeLeftInMillis = START_TIME_IN_MILLS;

                        }
                    }.start();


                    Log.e("BSTFDoctor", "myDialog" + myDialog.getContext());
                    Log.e("BSTFDoctor", "myDialog.findViewById(R.id.animation_view) <-- " + myDialog.findViewById(R.id.animation_view));
                    //

                    myDialog.show();




                }
            });

            Button btn_s_cancelar;
            btn_s_cancelar = myDialog.findViewById(R.id.btn_s_cancelar);
            btn_s_cancelar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(myDialog.getContext(), "sadadsdad", Toast.LENGTH_SHORT).show();
                    mTextViewCountDown.setText("");
                    myDialog.dismiss();
                    //todo : debe ir un intent para cancelar hacia ---
                    cancelRequestToDriver(driverID);

                }
            });

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
    private void sendRequestToDriver(String driverID) {

        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(Common.token_tbl);

        Log.e(TAG, "TOKEN : -->" + tokens.toString());
        //Buscar a driver por su id
        tokens
                .orderByKey()
                .equalTo(driverID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapShot : dataSnapshot.getChildren()) {
                            //convert to LatLng to json.
                            Log.e(TAG, "======================================================");
                            Log.e(TAG, "             sendRequestToDriver                    ");
                            LatLng userGeo = new LatLng(pacienteLatitude, pacienteLongitud);
                            Token tokenDoctor = postSnapShot.getValue(Token.class);
                            //Get token doctor and paciente
                            String dToken = tokenDoctor.getToken();
                            String pToken = FirebaseInstanceId.getInstance().getToken();
                            String json_lat_lng = new Gson().toJson(userGeo);
                            //Notification
                            Notification notification = new Notification("CUDPAST", "Usted tiene una solicutud de atención");// envia la ubicacion lat y lng  hacia Doctor APP
                            //Data
                            Data data = new Data(pToken, json_lat_lng);
                            //Log
                            Log.e(TAG, "doctorToken : " + dToken);
                            Log.e(TAG, "pacienteToken : " + pToken);
                            Log.e(TAG, "ubicacion de paciente: " + json_lat_lng);
                            //Sender (to, Notification)
                            Sender sender = new Sender(dToken, notification, data);
                            mService
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


                            Log.e(TAG, "======================================================");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e(TAG, " onCancelled" + databaseError.getMessage());
                    }
                });


    }

    private void cancelRequestToDriver(String driverID) {

        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(Common.token_tbl);

        Log.e(TAG, "TOKEN : -->" + tokens.toString());
        //Buscar a driver por su id
        tokens
                .orderByKey()
                .equalTo(driverID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapShot : dataSnapshot.getChildren()) {
                            //convert to LatLng to json.
                            Log.e(TAG, "======================================================");
                            Log.e(TAG, "             sendRequestToDriver                    ");
                            LatLng userGeo = new LatLng(pacienteLatitude, pacienteLongitud);
                            Token tokenDoctor = postSnapShot.getValue(Token.class);
                            //Get token doctor and paciente
                            String dToken = tokenDoctor.getToken();
                            String pToken = FirebaseInstanceId.getInstance().getToken();
                            String json_lat_lng = new Gson().toJson(userGeo);
                            //Notification
                            Notification notification = new Notification("el usuario ha cancelado", "el usuario ha cancelado");// envia la ubicacion lat y lng  hacia Doctor APP
                            //Data
                            Data data = new Data(pToken, json_lat_lng);
                            //Log
                            Log.e(TAG, "doctorToken : " + dToken);
                            Log.e(TAG, "pacienteToken : " + pToken);
                            Log.e(TAG, "ubicacion de paciente: " + json_lat_lng);
                            //Sender (to, Notification)
                            Sender sender = new Sender(dToken, notification, data);
                            mService
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


                            Log.e(TAG, "======================================================");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e(TAG, " onCancelled" + databaseError.getMessage());
                    }
                });


    }


}
