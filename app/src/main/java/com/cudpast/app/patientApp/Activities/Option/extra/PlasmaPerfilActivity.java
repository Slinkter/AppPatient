package com.cudpast.app.patientApp.Activities.Option.extra;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cudpast.app.patientApp.Adapter.CommentAdapter;
import com.cudpast.app.patientApp.Common.Common;
import com.cudpast.app.patientApp.Model.Comment;
import com.cudpast.app.patientApp.R;
import com.cudpast.app.patientApp.Remote.IFCMService;
import com.cudpast.app.patientApp.helper.Data;
import com.cudpast.app.patientApp.helper.FCMResponse;
import com.cudpast.app.patientApp.helper.Sender;
import com.cudpast.app.patientApp.helper.Token;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import dmax.dialog.SpotsDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlasmaPerfilActivity extends AppCompatActivity {

    private static final String TAG = PlasmaPerfilActivity.class.getSimpleName();
    private static final int MY_PERMISSION_REQUEST_CODE = 7000;
    //
    String pacienteUID;
    // plasma info
    String photoDoctor;
    String firstName;
    String lastname;
    String numPhone;
    String especialidad;
    String doctor_uid;
    //
    RecyclerView RVComment;
    CommentAdapter commentAdapter;
    List<Comment> listComment;
    static String COMMENT_KEY = "AppDoctor_history_Comment";
    String PostKey;

    //
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference AppDoctor_history_Comment;

    //
    CountDownTimer yourCountDownTimer;
    long START_TIME_IN_MILLS = 60 * 1000 * 1;
    long mTimeLeftInMillis;
    IFCMService mFCMService;
    //
    FusedLocationProviderClient fusedLocationClient;
    //
    Button btn_plasma_call;
    Button btn_plasma_Booking;
    //
    Double pacienteLatitude, pacienteLongitud;
    SpotsDialog waitingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plasma_perfil);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getSupportActionBar().setTitle("Perfil del Plasma");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Common.doctorAcept = false;
        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        RVComment = findViewById(R.id.myrecycleviewComments);
        AppDoctor_history_Comment = FirebaseDatabase.getInstance().getReference(Common.AppDoctor_history_Comment);
        mFCMService = Common.getIFCMService();
        firebaseAuth = FirebaseAuth.getInstance();
        pacienteUID = firebaseAuth.getCurrentUser().getUid();
        //
        waitingDialog = new SpotsDialog(PlasmaPerfilActivity.this, R.style.DialogLogin);
        //
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSION_REQUEST_CODE);
            return;
        }



        if (getIntent() != null) {
            //get extra intent

            Log.e(TAG, getIntent().getExtras().getString("doctor_uid"));
            Log.e(TAG, getIntent().getExtras().getString("doctor_name"));
            Log.e(TAG, getIntent().getExtras().getString("doctor_last"));


            photoDoctor = getIntent().getExtras().getString("doctor_img");
            firstName = getIntent().getExtras().getString("doctor_name");
            lastname = getIntent().getExtras().getString("doctor_last");
            numPhone = getIntent().getExtras().getString("doctor_phone");
            especialidad = getIntent().getExtras().getString("doctor_especilidad");

            doctor_uid = getIntent().getExtras().getString("doctor_uid");

            // init views-xml
            ImageView imgdoc = findViewById(R.id.aa_thumbnail_plasma);
            TextView post_firstName = findViewById(R.id.aa_firstname_plasma);
            TextView post_lastName = findViewById(R.id.aa_lastname_plasma);
            TextView post_phone = findViewById(R.id.aa_phone_plasma);
            TextView post_especialidad = findViewById(R.id.aa_especialidad_plasma);
            //setting views
            post_firstName.setText(firstName);
            post_lastName.setText(lastname);
            post_phone.setText(numPhone);
            post_especialidad.setText(especialidad);
            Picasso
                    .with(this)
                    .load(photoDoctor)
                    .resize(500, 500)
                    .centerInside()
                    .placeholder(R.drawable.ic_doctorapp)
                    .error(R.drawable.ic_doctorapp)
                    .into(imgdoc);


            btn_plasma_call = findViewById(R.id.btn_call_plasma);
            btn_plasma_Booking = findViewById(R.id.btn_plasmaBooking);

            btn_plasma_call.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + numPhone));
                    startActivity(intent);
                }
            });

            btn_plasma_Booking.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    RequestDoctor_booking(doctor_uid);
                    showAlertDialogBooking();
                }
            });
        }
        initRVComment();
    }


    private void initRVComment() {

        RVComment.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        DatabaseReference commentRef = AppDoctor_history_Comment.child(doctor_uid);
        commentRef
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        listComment = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Comment comment = snapshot.getValue(Comment.class);
                            listComment.add(comment);
                        }
                        commentAdapter = new CommentAdapter(getApplicationContext(), listComment);
                        RVComment.setAdapter(commentAdapter);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }


    private String timestampToString(long time) {
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(time);
        String date = DateFormat.format("dd-MM-yyyy", calendar).toString();
        return date;
    }

    private void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public void locationalgo (){
        fusedLocationClient
                .getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {

                        Log.e(TAG, " :  fusedLocationClient    " + location);
                        pacienteLatitude = location.getLatitude();
                        pacienteLongitud = location.getLongitude();
                        Log.e(TAG, " :  pacienteLatitude  = " + pacienteLongitud);
                        Log.e(TAG, " :  pacienteLongitud  = " + pacienteLongitud);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(PlasmaPerfilActivity.this, "Fused Location Cliente Falta", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void showAlertDialogBooking() {

        try {
            AlertDialog.Builder mBuiler = new AlertDialog.Builder(PlasmaPerfilActivity.this);
            View view = getLayoutInflater().inflate(R.layout.plasma_booking_waiting, null);
            Button btn_cancelar_plasma = view.findViewById(R.id.btn_cancelar_plasma);
            final TextView xml_countDown = view.findViewById(R.id.text_view_countDown_plasma);
            mBuiler.setView(view);
            mBuiler.setCancelable(false);
            final AlertDialog dialog = mBuiler.create();
            dialog.show();
            //
            mTimeLeftInMillis = START_TIME_IN_MILLS;
            yourCountDownTimer = new CountDownTimer(mTimeLeftInMillis, 500) {
                @Override
                public void onTick(long millisUntilFinished) {
                    //Cargar los minutos
                    mTimeLeftInMillis = millisUntilFinished;
                    int minutos = (int) (mTimeLeftInMillis / 1000) / 60;
                    int secounds = (int) (mTimeLeftInMillis / 1000) % 60;
                    String timeFormated = String.format(Locale.getDefault(), "%02d:%02d", minutos, secounds);
                    xml_countDown.setText(timeFormated);
                    if (Common.doctorAcept) {
                        yourCountDownTimer.cancel();
                        Log.e("onTick", " : Common.doctorAcept = " + Common.doctorAcept);
                    }

                    Log.e("onTick", " : mTimeLeftInMillis = " + mTimeLeftInMillis);


                }

                @Override
                public void onFinish() {
                    //Enviar NotificacionData
                    RequestDoctor_timeOut(doctor_uid);
                    yourCountDownTimer.cancel();
                    dialog.dismiss();
                    Log.e("onFinish", " Tiempo fuera , el doctor no respondio ele mensaje ");
                }
            };
            yourCountDownTimer.start();


            btn_cancelar_plasma.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    RequestDoctor_cancel(doctor_uid);
                    yourCountDownTimer.cancel();
                    dialog.dismiss();
                }
            });


        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void RequestDoctor_booking(String doctor_uid) {
        Log.e(TAG, "======================================================");
        Log.e(TAG, "             RequestDoctor_booking                    ");
        //
        final SpotsDialog waitingDialog = new SpotsDialog(PlasmaPerfilActivity.this, R.style.DialogLogin);
        waitingDialog.show();
        //
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(Common.token_tbl);
        tokens.orderByKey().equalTo(doctor_uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapShot : dataSnapshot.getChildren()) {
                    Token tokenDoctor = postSnapShot.getValue(Token.class);
                    if (tokenDoctor != null) {
                        //convert to LatLng to json.
                        LatLng userGeo = new LatLng(pacienteLatitude, pacienteLongitud);
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
                        Log.e(TAG, "ubicacion de paciente : json_lat_lng = " + json_lat_lng);
                        Log.e(TAG, "uid_paciente : " + pacienteUID);
                        Log.e(TAG, " : Token tokenDoctor = " + tokenDoctor.getToken());
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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                waitingDialog.dismiss();
                Log.e(TAG, " onCancelled" + databaseError.getMessage());
                Log.e(TAG, "======================================================");
            }
        });

    }

    private void RequestDoctor_cancel(String doctor_uid) {
        Log.e(TAG, "======================================================");
        Log.e(TAG, "             RequestDoctor_cancel                    ");
        //
        waitingDialog.show();
        //.Obtener el token del doctor apartir de su id del doctor
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(Common.token_tbl);
        tokens.orderByKey().equalTo(doctor_uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapShot : dataSnapshot.getChildren()) {
                    Token tokenDoctor = postSnapShot.getValue(Token.class);
                    if (tokenDoctor != null) {
                        //
                        String title = "App Doctor";
                        String body = "El usuario ha cancelado";
                        String dToken = tokenDoctor.getToken();
                        Log.e(TAG, "Token tokenDoctor = " + tokenDoctor.getToken());
                        //-->Data
                        Data data = new Data(title, body);
                        //-->Sender (to, data)
                        Sender sender = new Sender(dToken, data);
                        //
                        sendNotification(sender);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                waitingDialog.dismiss();
                Log.e(TAG, " onCancelled : " + databaseError.getMessage());
            }
        });
    }

    private void RequestDoctor_timeOut(String doctor_uid) {
        Log.e(TAG, "======================================================");
        Log.e(TAG, "             RequestDoctor_timeOut                    ");
        //
        waitingDialog.show();
        //Obtener el token del doctor apartir de su id del doctor
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(Common.token_tbl);
        tokens.orderByKey().equalTo(doctor_uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapShot : dataSnapshot.getChildren()) {
                    Token tokenDoctor = postSnapShot.getValue(Token.class);
                    if (tokenDoctor != null) {
                        String title = "App Doctor";
                        String body = "Tiempo fuera";
                        String dToken = tokenDoctor.getToken();
                        Log.e(TAG, "Token tokenDoctor = " + tokenDoctor.getToken());
                        //Data
                        Data data = new Data(title, body);
                        //Sender (to, data)
                        Sender sender = new Sender(dToken, data);
                        //
                        sendNotification(sender);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                waitingDialog.dismiss();
                Log.e(TAG, " onCancelled" + databaseError.getMessage());
            }
        });

    }

    private void sendNotification(Sender sender) {
        mFCMService
                .sendMessage(sender)
                .enqueue(new Callback<FCMResponse>() {
                    @Override
                    public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                        if (response.body().success == 1) {
                            Log.e(TAG, "sendNotification :SUCCESS ");
                            waitingDialog.dismiss();
                        }
                    }

                    @Override
                    public void onFailure(Call<FCMResponse> call, Throwable t) {
                        Log.e(TAG, "sendNotification :FAILURE ");
                        waitingDialog.dismiss();
                    }
                });
    }

}
