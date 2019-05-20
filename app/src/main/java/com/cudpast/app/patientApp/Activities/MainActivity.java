package com.cudpast.app.patientApp.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.cudpast.app.patientApp.Common.Common;
import com.cudpast.app.patientApp.Model.User;
import com.cudpast.app.patientApp.R;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity implements
        View.OnClickListener {

    // private ImageView photoImageView;
    private TextView nameTextView;
    private TextView emailTextView;
    private TextView idTextView;
    private FirebaseAuth auth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        nameTextView = findViewById(R.id.nameTextView);
        emailTextView = findViewById(R.id.emailTextView);
        auth = FirebaseAuth.getInstance();

        // Buttons
        findViewById(R.id.btnMedicos).setOnClickListener(this);
        findViewById(R.id.btnEspecialidad).setOnClickListener(this);
        findViewById(R.id.btnHistorial).setOnClickListener(this);
        findViewById(R.id.btnUpdateInfo).setOnClickListener(this);
        findViewById(R.id.btnUbicacion).setOnClickListener(this);
        findViewById(R.id.btnCerra_sesion).setOnClickListener(this);

    }

    //.Metodo Principal
    @Override
    protected void onStart() {
        super.onStart();
        User currentUser = Common.currentUser;
        updateUI(currentUser);
    }

    //.VerificarSiUsuarioExiste
    private void updateUI(User user) {
        if (user != null) {
            mostratInfoDelUsuario();
        } else {
            goLogIngScreen();
        }
    }

    //.Escoger Btn
    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btnMedicos) {
            Intent intent = new Intent(MainActivity.this, ListDoctorActivity.class);
            startActivity(intent);
        } else if (i == R.id.btnEspecialidad) {
            Intent intent = new Intent(MainActivity.this, EspecialidadActivity.class);
            startActivity(intent);
        } else if (i == R.id.btnHistorial) {
            Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
            startActivity(intent);
        } else if (i == R.id.btnUpdateInfo) {
            Intent intent = new Intent(MainActivity.this, UpdateProfilePacienteActivity.class);
            startActivity(intent);
        } else if (i == R.id.btnUbicacion) {
            Intent intent = new Intent(MainActivity.this, UbicacionActivity.class);
            startActivity(intent);
        } else if (i == R.id.btnCerra_sesion) {
            signOut();
        }
    }


    //. mostratInfoDelUsuario() {
    private void mostratInfoDelUsuario() {

        try {

            User usuario = Common.currentUser;
            nameTextView.setText(usuario.getNombre() + " " + usuario.getApellido());
            emailTextView.setText(usuario.getCorreo());
            idTextView.setText(usuario.getDni());

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    //.
    private void goLogIngScreen() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | intent.FLAG_ACTIVITY_CLEAR_TASK | intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    //.
    private void signOut() {
        auth.signOut();
        updateUI(null);
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | intent.FLAG_ACTIVITY_CLEAR_TASK | intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


}
