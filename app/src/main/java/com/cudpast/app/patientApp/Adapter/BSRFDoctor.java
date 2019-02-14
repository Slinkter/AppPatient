package com.cudpast.app.patientApp.Adapter;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.cudpast.app.patientApp.Activities.UbicacionActivity;
import com.cudpast.app.patientApp.Common.Common;
import com.cudpast.app.patientApp.Model.Doctor;
import com.cudpast.app.patientApp.Model.DoctorPerfil;
import com.cudpast.app.patientApp.R;
import com.cudpast.app.patientApp.Remote.IFCMService;
import com.cudpast.app.patientApp.helper.FCMResponse;
import com.cudpast.app.patientApp.helper.Notification;
import com.cudpast.app.patientApp.helper.Sender;
import com.cudpast.app.patientApp.helper.Token;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.cudpast.app.patientApp.Common.Common.mLastLocation;

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


            btn_yes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    Toast.makeText(getContext(), "si", Toast.LENGTH_SHORT).show();
                    Log.e("driverID", driverID);
                    sendRequestToDriver(driverID);


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
                            LatLng userGeo = new LatLng(pacienteLatitude, pacienteLongitud);
                            Token tokenDoctor = postSnapShot.getValue(Token.class);
                            String json_lat_lng = new Gson().toJson(userGeo);
                            String pacienteToken = FirebaseInstanceId.getInstance().getToken();

                            Log.e(TAG, "userGeo " + userGeo);
                            Log.e(TAG, "tokenDoctor " + tokenDoctor.toString() + " tokenDoctor : " + tokenDoctor.getToken());
                            Log.e(TAG, "json_lat_lng " + json_lat_lng);
                            Log.e(TAG, "pacienteToken " + pacienteToken);

                            Notification notificacionData = new Notification(pacienteToken, json_lat_lng);// envia la ubicacion lat y lng  hacia Doctor APP
                            //Sender (to, Notification)
                            String doctorToken = tokenDoctor.getToken();
                            Sender mensaje = new Sender(doctorToken, notificacionData);
                            Log.e(TAG, "======================================================");

                            //enviar al appDOCTOR
                            mService.sendMessage(mensaje)
                                    .enqueue(new Callback<FCMResponse>() {
                                        @Override
                                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                                            Log.e("CustomerCallActivity", "response :--------->" + response);
                                            Log.e("CustomerCallActivity", "response.body().success:--------->" + response.body().success);
                                            if (response.body().success == 1) {
//                                                Toast.makeText(UbicacionActivity.this, "Contactando al doctor", Toast.LENGTH_SHORT).show();
                                            } else {
//                                                Toast.makeText(UbicacionActivity.this, "Doctor Fuera de Servicio", Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<FCMResponse> call, Throwable t) {
                                            Log.e("ERROR", t.getMessage());
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e(TAG, " onCancelled" + databaseError.getMessage());
                    }
                });
    }


}
