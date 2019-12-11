package com.cudpast.app.patientApp.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.cudpast.app.patientApp.Activities.Option.HistoryActivity;
import com.cudpast.app.patientApp.Activities.Option.ListDoctorActivity;
import com.cudpast.app.patientApp.Activities.Option.ListPlasmaActivity;
import com.cudpast.app.patientApp.Activities.Option.UbicacionActivity;
import com.cudpast.app.patientApp.Activities.Option.UpdateProfilePacienteActivity;
import com.cudpast.app.patientApp.Common.Common;
import com.cudpast.app.patientApp.Model.PacientProfile;
import com.cudpast.app.patientApp.R;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String TAG = MainActivity.class.getSimpleName();
    private TextView nameTextView;
    private TextView emailTextView;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);
        //
        auth = FirebaseAuth.getInstance();
        //
        nameTextView = findViewById(R.id.nameTextView);
        emailTextView = findViewById(R.id.emailTextView);
        // View-Click
        findViewById(R.id.btnMedicos).setOnClickListener(this);
        findViewById(R.id.btnHistorial).setOnClickListener(this);
        findViewById(R.id.btnUpdateInfo).setOnClickListener(this);
        findViewById(R.id.btnUbicacion).setOnClickListener(this);
        findViewById(R.id.btnCerra_sesion).setOnClickListener(this);
        findViewById(R.id.btnPlasma).setOnClickListener(this);

    }

    // METODO PRINCIPAL
    @Override
    public void onClick(View v) {
        int i = v.getId();
        //
        if (i == R.id.btnMedicos) {
            Intent intent = new Intent(this, ListDoctorActivity.class);
            startActivity(intent);
        } else if (i == R.id.btnHistorial) {
            Intent intent = new Intent(this, HistoryActivity.class);
            startActivity(intent);
        } else if (i == R.id.btnUpdateInfo) {
            Intent intent = new Intent(this, UpdateProfilePacienteActivity.class);
            startActivity(intent);
        } else if (i == R.id.btnUbicacion) {
            Intent intent = new Intent(this, UbicacionActivity.class);
            startActivity(intent);
        } else if (i == R.id.btnPlasma) {
            Intent intent = new Intent(this, ListPlasmaActivity.class);
            startActivity(intent);
        } else if (i == R.id.btnCerra_sesion) {
            signOut();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        PacientProfile currentPacientProfile = Common.currentPacientProfile;
        updateUI(currentPacientProfile);
    }

    //.Check user
    private void updateUI(PacientProfile pacientProfile) {
        if (pacientProfile != null) {
            displayInfoPacient();
        } else {
            goLogIngScreen();
        }
    }


    //. Cargar info del Paciente
    private void displayInfoPacient() {

        if (Common.currentPacientProfile != null) {
            nameTextView.setText(Common.currentPacientProfile.getFirstname());
            emailTextView.setText(Common.currentDoctorProfile.getMail());
        }

    }

    //.Ir al Login sino esta logeado
    private void goLogIngScreen() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    //. Sign Out
    private void signOut() {
        auth.signOut();
        updateUI(null); // si es null regresa al login
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.e(TAG, "onBackPressed() ");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Log.e(TAG, "onKeyDown");
        }
        return super.onKeyDown(keyCode, event);
    }



}
