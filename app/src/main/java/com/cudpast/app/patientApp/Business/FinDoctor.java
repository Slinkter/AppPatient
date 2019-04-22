package com.cudpast.app.patientApp.Business;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.cudpast.app.patientApp.Activities.MainActivity;
import com.cudpast.app.patientApp.Common.Common;
import com.cudpast.app.patientApp.Model.DoctorPerfil;
import com.cudpast.app.patientApp.Model.User;
import com.cudpast.app.patientApp.R;
import com.cudpast.app.patientApp.Remote.IFCMService;
import com.cudpast.app.patientApp.helper.Data;
import com.cudpast.app.patientApp.helper.FCMResponse;
import com.cudpast.app.patientApp.helper.Notification;
import com.cudpast.app.patientApp.helper.Sender;
import com.cudpast.app.patientApp.helper.Token;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FinDoctor extends AppCompatActivity {

    private static final String TAG = FinDoctor.class.getSimpleName();

    private ImageView image_doctor;
    private TextView tv_doctor_firstname;
    private TextView tv_doctor_lastName, c_especialidad, c_tiempo, c_servicio;

    private EditText id_paciente_comment;

    private TextView tv_paciente_firstname;
    private TextView tv_paciente_lastName;

    private Button btn_fin_atencion;

    private DatabaseReference AppPaciente_history, AppDoctor_history,AppDoctor_history_Comment;
    private FirebaseAuth auth;


    IFCMService mFCMService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fin);
        getSupportActionBar().hide();

        auth = FirebaseAuth.getInstance();

        AppPaciente_history = FirebaseDatabase.getInstance().getReference(Common.AppPaciente_history);
        AppDoctor_history = FirebaseDatabase.getInstance().getReference(Common.AppDoctor_history);
        AppDoctor_history_Comment = FirebaseDatabase.getInstance().getReference(Common.AppDoctor_history_Comment);


        image_doctor = findViewById(R.id.fin_doctorImage);
        tv_doctor_firstname = findViewById(R.id.fin_doctorFirstNameFin);
        tv_doctor_lastName = findViewById(R.id.fin_doctorLastNameFin);

        id_paciente_comment = findViewById(R.id.id_paciente_comment);

        c_especialidad = findViewById(R.id.c_especialidad);
        c_tiempo = findViewById(R.id.c_tiempo);
        c_servicio = findViewById(R.id.c_servicio);


        tv_paciente_firstname = findViewById(R.id.fin_pacienteFirstName);
        tv_paciente_lastName = findViewById(R.id.fin_pacienteLastName);

        metodoSignInResult();

        btn_fin_atencion = findViewById(R.id.btn_fin_atencion);
        btn_fin_atencion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //
                sendEndAttention(Common.token_doctor);
                insertarHistoryPacienteDoctor();
                //
                Intent intent = new Intent(FinDoctor.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });

        mFCMService = Common.getIFCMService();


    }


    private void metodoSignInResult() {
        try {
            DoctorPerfil currentDoctor = Common.currentDoctor;
            User currentUser = Common.currentUser;

            tv_doctor_firstname.setText(currentDoctor.getFirstname());
            tv_doctor_lastName.setText(currentDoctor.getLastname());
            c_especialidad.setText(currentDoctor.getEspecialidad());
            c_tiempo.setText("30 min");
            c_servicio.setText("Consulta medica");

            tv_paciente_firstname.setText(currentUser.getNombre());
            tv_paciente_lastName.setText(currentUser.getApellido());

            Picasso
                    .with(this)
                    .load(currentDoctor.getImage())
                    .placeholder(R.drawable.ic_doctorapp)
                    .error(R.drawable.ic_doctorapp)
                    .into(image_doctor);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //.
    private void sendEndAttention(String driverID) {
        Log.e(TAG, "======================================================");
        Log.e(TAG, "             sendEndAttention                    ");
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(Common.token_tbl);
        Log.e(TAG, "driverID : " + driverID);
        //Buscar a doctor por su id
        tokens
                .orderByKey()
                .equalTo(driverID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapShot : dataSnapshot.getChildren()) {
                            //convert to LatLng to json.
                            LatLng userGeo = new LatLng(15.0f, 15.0f);
                            Token tokenDoctor = postSnapShot.getValue(Token.class);
                            //Get token doctor and paciente
                            String dToken = tokenDoctor.getToken();
                            String pToken = FirebaseInstanceId.getInstance().getToken();
                            String json_lat_lng = new Gson().toJson(userGeo);
                            //Notification
                            Notification notification = new Notification("DoctorFin", "el usuario ha finalizado");// envia la ubicacion lat y lng  hacia Doctor APP
                            //Data
                            Data data = new Data();
                            //Sender (to, Notification)
                            Sender sender = new Sender(dToken, notification, data);
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

    private void insertarHistoryPacienteDoctor() {
        String fecha = getCurrentTimeStamp();
        String pacienteUID = auth.getCurrentUser().getUid();
        String doctorUID = Common.currentDoctor.getUid();
        Log.e(TAG, "pacienteUID  " + pacienteUID);
        Log.e(TAG, "doctorUID  " + doctorUID);


        AppPaciente_history
                .child(pacienteUID)
                .child(fecha)
                .setValue(Common.currentDoctor)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.e("AppPaciente_history ", "onSuccess ");
                    }
                });
        AppDoctor_history
                .child(doctorUID)
                .child(fecha)
                .setValue(Common.currentUser)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.e("AppDoctor_history ", "onSuccess ");
                    }
                });

        String commnet = id_paciente_comment.getText().toString();

        AppDoctor_history_Comment
                .child(doctorUID)
                .child(fecha)
                .setValue(commnet)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.e("AppDoctor_history ", "AppDoctor_history_Comment ");
                    }
                });



    }


    public static String getCurrentTimeStamp() {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currentTimeStamp = dateFormat.format(new Date());
            return currentTimeStamp;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


}
