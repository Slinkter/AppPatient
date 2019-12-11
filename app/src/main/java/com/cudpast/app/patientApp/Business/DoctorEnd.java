package com.cudpast.app.patientApp.Business;

import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cudpast.app.patientApp.Activities.MainActivity;
import com.cudpast.app.patientApp.Common.Common;
import com.cudpast.app.patientApp.Model.Comment;
import com.cudpast.app.patientApp.Model.DoctorProfile;
import com.cudpast.app.patientApp.Model.PacientProfile;
import com.cudpast.app.patientApp.R;
import com.cudpast.app.patientApp.Remote.IFCMService;
import com.cudpast.app.patientApp.helper.Data;
import com.cudpast.app.patientApp.helper.FCMResponse;
import com.cudpast.app.patientApp.helper.Sender;
import com.cudpast.app.patientApp.helper.Token;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DoctorEnd extends AppCompatActivity {

    private static final String TAG = DoctorEnd.class.getSimpleName();

    private ImageView image_doctor;
    private TextView tv_doctor_firstname;
    private TextView tv_doctor_lastName, c_especialidad, c_tiempo, c_servicio;

    private EditText id_paciente_comment;

    private TextView tv_paciente_firstname;
    private TextView tv_paciente_lastName;

    private Button btn_fin_atencion;

    private DatabaseReference AppPaciente_history, AppDoctor_history, AppDoctor_history_Comment;
    private FirebaseAuth auth;

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;


    IFCMService mFCMService;

    private Animation animation;
    private Vibrator vib;

    public String firebaseDoctorUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fin);
        getSupportActionBar().hide();

        auth = FirebaseAuth.getInstance();
        if (getIntent() != null) {
            firebaseDoctorUID = getIntent().getStringExtra("firebaseDoctorUID");
            Log.e(TAG, "firebaseDoctorUID" + " = " + firebaseDoctorUID);
        }

        animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.shake);
        vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();

        AppPaciente_history = FirebaseDatabase.getInstance().getReference(Common.AppPaciente_history);
        AppDoctor_history = FirebaseDatabase.getInstance().getReference(Common.AppDoctor_history);
        AppDoctor_history_Comment = FirebaseDatabase.getInstance().getReference(Common.AppDoctor_history_Comment);


        //   image_doctor = findViewById(R.id.fin_doctorImage);
        //    tv_doctor_lastName = findViewById(R.id.fin_doctorLastNameFin);
        //  tv_paciente_firstname = findViewById(R.id.fin_pacienteFirstName);
        //tv_paciente_lastName = findViewById(R.id.fin_pacienteLastName);

        tv_doctor_firstname = findViewById(R.id.fin_doctorFirstNameFin);

        id_paciente_comment = findViewById(R.id.id_paciente_comment);

        c_especialidad = findViewById(R.id.c_especialidad);
        c_tiempo = findViewById(R.id.c_tiempo);
        c_servicio = findViewById(R.id.c_servicio);


        metodoSignInResult();

        btn_fin_atencion = findViewById(R.id.btn_fin_atencion);

        btn_fin_atencion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (submitForm()) {
                    //
                    mFCMService = Common.getIFCMService();
                    sendEndAttention(Common.currentDoctorProfile.getUid());
                    insertarHistoryPacienteDoctor();
                    //
                    Intent intent = new Intent(DoctorEnd.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }


            }
        });
        //pdf
        //        templatePDF = new TemplatePDF(getApplicationContext());
        //        templatePDF.openDocument();
        //        templatePDF.addMetada("Clientes", "Ventas", "Marines");
        //        templatePDF.addTitles("Cudpast DoctorApp", "Dr. Juan Perez", "2019/04/27");
        //        templatePDF.addParagraph(shortText);
        //        templatePDF.addParagraph(longText);
        //        templatePDF.addCreateTable(header, getClients());
        //        templatePDF.closeDocument();


    }

    private boolean submitForm() {

        if (!checkCommentPaciente()) {
            id_paciente_comment.setAnimation(animation);
            id_paciente_comment.startAnimation(animation);
            vib.vibrate(120);
            return false;
        }
        return true;
    }

    private boolean checkCommentPaciente() {
        if (id_paciente_comment.getText().toString().trim().isEmpty()) {
            id_paciente_comment.setError("Debes ingresar un comentario ");
            return false;
        }
        return true;
    }


    private void metodoSignInResult() {
        try {
            DoctorProfile currentDoctor = Common.currentDoctorProfile;
            PacientProfile currentPacientProfile = Common.currentPacientProfile;

            tv_doctor_firstname.setText(currentDoctor.getFirstname());
            tv_doctor_lastName.setText(currentDoctor.getLastname());
            c_especialidad.setText(currentDoctor.getEspecialidad());
            c_tiempo.setText("30 min");
            c_servicio.setText("Consulta medica");

            tv_paciente_firstname.setText(currentPacientProfile.getFirstname());
            tv_paciente_lastName.setText(currentPacientProfile.getLastname());

            Picasso
                    .with(this)
                    .load(currentDoctor.getImagePhoto())
                    .placeholder(R.drawable.ic_doctorapp)
                    .error(R.drawable.ic_doctorapp)
                    .into(image_doctor);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //.
    private void sendEndAttention(String driverUID) {
        Log.e(TAG, "======================================================");
        Log.e(TAG, "             sendEndAttention                    ");
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(Common.token_tbl);
        Log.e(TAG, "driverUID : " + driverUID);
        //Buscar a doctor por su id
        tokens
                .orderByKey()
                .equalTo(driverUID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapShot : dataSnapshot.getChildren()) {
                            //Get token doctor and paciente
                            Token tokenDoctor = postSnapShot.getValue(Token.class);
                            String dToken = tokenDoctor.getToken();
                            String title = "App Doctor";
                            String body = "El usuario ha finalizado";
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
        String end_atention = getCurrentTimeStamp();
        String UID_paciente = auth.getCurrentUser().getUid();
        String UID_doctor = Common.currentDoctorProfile.getUid();

        AppPaciente_history
                .child(UID_paciente)
                .child(end_atention)
                .setValue(Common.currentDoctorProfile)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.e("AppPaciente_history ", "onSuccess ");
                    }
                });


        AppDoctor_history
                .child(UID_doctor)
                .child(end_atention)
                .setValue(Common.currentPacientProfile)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.e("AppDoctor_history ", "onSuccess ");
                    }
                });


        String comment_pacient = id_paciente_comment.getText().toString();
        //
        String img_pacient = "default";
        String username_paciente = Common.currentPacientProfile.getFirstname();

        Comment comment = new Comment(comment_pacient, UID_paciente, img_pacient, username_paciente);

        AppDoctor_history_Comment
                .child(UID_doctor)
                .child(end_atention)
                .setValue(comment)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        showMessage("¡Gracias por commentar !");
                        id_paciente_comment.setText("");
                    }
                });

        Log.e(TAG, "UID_paciente = " + UID_paciente);
        Log.e(TAG, "UID_doctor = " + UID_doctor);
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

    private void showMessage(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }
}
