package com.cudpast.app.patientApp.Activities.Option;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.cudpast.app.patientApp.Activities.MainActivity;
import com.cudpast.app.patientApp.Common.Common;
import com.cudpast.app.patientApp.Model.PacientProfile;
import com.cudpast.app.patientApp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import dmax.dialog.SpotsDialog;

public class UpdateProfilePacienteActivity extends AppCompatActivity {

    public static final String TAG = UpdateProfilePacienteActivity.class.getSimpleName();
    private TextView updatePacienteName, updatePacienteLast, updatePacientePhone, updatePacienteAnddress;
    private DatabaseReference tb_Info_Paciente;
    private Button btn_UpdateInfoUpdate;

    private FirebaseAuth auth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile_paciente);
        getSupportActionBar().setTitle("Actualizar Datos");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //.
        tb_Info_Paciente = FirebaseDatabase.getInstance().getReference(Common.TB_INFO_PACIENTE);
        auth = FirebaseAuth.getInstance();
        //.XML
        updatePacienteName = findViewById(R.id.updatePacienteName);
        updatePacienteLast = findViewById(R.id.updatePacienteLast);
        updatePacientePhone = findViewById(R.id.updatePacientePhone);
        updatePacienteAnddress = findViewById(R.id.updatePacienteAnddress);
        btn_UpdateInfoUpdate = findViewById(R.id.btn_UpdateInfoUpdate);


        //.Obtener usuario actualizr
        final PacientProfile currentPacientProfile = Common.currentPacientProfile;
        //.Display on XML
        updatePacienteName.setText(currentPacientProfile.getFirstname());
        updatePacienteLast.setText(currentPacientProfile.getLastname());
        updatePacientePhone.setText(currentPacientProfile.getPhone());
        updatePacienteAnddress.setText(currentPacientProfile.getAddress());
        //


        btn_UpdateInfoUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final SpotsDialog waitingDialog = new SpotsDialog(UpdateProfilePacienteActivity.this, R.style.DialogUpdateDoctorProfile);
                waitingDialog.show();

                //.Actualizar campos
                final PacientProfile updatePacientProfile = new PacientProfile();
                updatePacientProfile.setFirstname(updatePacienteName.getText().toString());
                updatePacientProfile.setLastname(updatePacienteLast.getText().toString());
                updatePacientProfile.setPhone(updatePacientePhone.getText().toString());
                updatePacientProfile.setAddress(updatePacienteAnddress.getText().toString());

                updatePacientProfile.setMail(Common.currentPacientProfile.getMail());
                updatePacientProfile.setPassword(Common.currentPacientProfile.getPassword());
                updatePacientProfile.setDateborn(Common.currentPacientProfile.getDateborn());
                updatePacientProfile.setDni(Common.currentPacientProfile.getDni());
                Common.currentPacientProfile = updatePacientProfile;

                tb_Info_Paciente
                        .child(auth.getCurrentUser().getUid())
                        .setValue(updatePacientProfile)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                waitingDialog.dismiss();
                                Log.e(TAG, " onSuccess Update Profile Paciente");
                                iniciarActivity();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                waitingDialog.dismiss();
                                Log.e(TAG, " OnFailureListener");
                                e.printStackTrace();
                            }
                        });
            }
        });
    }

    public void iniciarActivity() {
        Intent intent = new Intent(UpdateProfilePacienteActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
