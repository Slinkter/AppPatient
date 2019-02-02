package com.cudpast.app.patientApp.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.cudpast.app.patientApp.Common.Common;
import com.cudpast.app.patientApp.Model.User;
import com.cudpast.app.patientApp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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

    }

    //1.Metodo Principal
    @Override
    protected void onStart() {
        super.onStart();
        //commo.user.
        FirebaseUser currentUser = auth.getCurrentUser();
        updateUI(currentUser);
    }
    //.VerificarSiUsuarioExiste
    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Toast.makeText(this, "Hola" + user.getDisplayName(), Toast.LENGTH_SHORT).show();
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
            Intent intent = new Intent(MainActivity.this, MapsActivity.class);
            startActivity(intent);
        } else if (i == R.id.btnUpdateInfo) {
            Intent intent = new Intent(MainActivity.this, MapsActivity.class);
            startActivity(intent);
        } else if (i == R.id.btnUbicacion) {
            Intent intent = new Intent(MainActivity.this, MapsActivity.class);
            startActivity(intent);
        } else if (i == R.id.btnCerra_sesion) {
            signOut();
        }
    }


    //. mostratInfoDelUsuario() {
    private void mostratInfoDelUsuario() {

        try {

            User usuario = Common.currentUser;
            Log.e("usuario ------> ", usuario.getNombre() + " \n" + usuario.getApellido());
            nameTextView.setText(usuario.getNombre());
            emailTextView.setText(usuario.getCorreo());
            idTextView.setText(usuario.getDni());

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    //4.LOGIN_ACTIVITY
    private void goLogIngScreen() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | intent.FLAG_ACTIVITY_CLEAR_TASK | intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }



    private void signOut() {
        auth.signOut();
        updateUI(null);
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | intent.FLAG_ACTIVITY_CLEAR_TASK | intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


}
