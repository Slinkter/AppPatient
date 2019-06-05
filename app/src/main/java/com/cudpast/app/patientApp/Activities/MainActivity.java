package com.cudpast.app.patientApp.Activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.cudpast.app.patientApp.Activities.Option.HistoryActivity;
import com.cudpast.app.patientApp.Activities.Option.ListDoctorActivity;
import com.cudpast.app.patientApp.Activities.Option.UbicacionActivity;
import com.cudpast.app.patientApp.Activities.Option.UpdateProfilePacienteActivity;
import com.cudpast.app.patientApp.Common.Common;
import com.cudpast.app.patientApp.Model.User;
import com.cudpast.app.patientApp.R;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity implements
        View.OnClickListener {

    public static final String TAG = MainActivity.class.getSimpleName();

    // private ImageView photoImageView;
    private TextView nameTextView;
    private TextView emailTextView;
    private TextView idTextView;
    private FirebaseAuth auth;

    private static final long SPLASH_SCREEN_DELAY = 3000;
    private static final int MY_PERMISSION_REQUEST_CODE = 7000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        //
        nameTextView = findViewById(R.id.nameTextView);
        emailTextView = findViewById(R.id.emailTextView);
        auth = FirebaseAuth.getInstance();
        // Buttons
        findViewById(R.id.btnMedicos).setOnClickListener(this);
        findViewById(R.id.btnHistorial).setOnClickListener(this);
        findViewById(R.id.btnUpdateInfo).setOnClickListener(this);
        findViewById(R.id.btnUbicacion).setOnClickListener(this);
        findViewById(R.id.btnCerra_sesion).setOnClickListener(this);

    }

    //.Metodo Principal
    @Override
    protected void onStart() {
        super.onStart();
        permisos();
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Toast.makeText(this, "onBackPressed() ", Toast.LENGTH_SHORT).show();
        Log.e(TAG, "onBackPressed() ");

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

    //permisos
    public void permisos() {
        if (ContextCompat
                .checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat
                        .checkSelfPermission(this,
                                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSION_REQUEST_CODE);
        } else {
            // Si tiene los permisos
            // verficiar el  checkPlayService
            Log.e("hola", "si tiene los permisos");
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case MY_PERMISSION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("hola", "si tiene los permisos  v2 ");
                }
            }
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //        Toast.makeText(this, "Hola on key Down ", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "onKeyDown");
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        Log.e(TAG, "------------> onDestroy called <--------------");
        Toast.makeText(this, "onDestroy called", Toast.LENGTH_LONG).show();


        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(TAG, "onStop");
    }
}
