package com.cudpast.app.patientApp.Activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.cudpast.app.patientApp.R;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;

public class VerificacionLoginActivity extends AppCompatActivity {

//
//    private GoogleApiClient googleApiClient;
//    private SignInButton signInButton;
//    public static final int SIGN_IN_CODE = 777;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R .layout.activity_verificacion_login);
//        //
//        getSupportActionBar().hide();
//
//        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
//        googleApiClient = new GoogleApiClient.Builder(this)
//                .enableAutoManage(this, this)
//                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
//                .build();
//
//        signInButton = (SignInButton) findViewById(R.id.btnLoginGoogle);
//        //agregar estilo al boton de google
//
//        signInButton.setSize(SignInButton.SIZE_ICON_ONLY);
//        signInButton.setSize(SignInButton.COLOR_DARK);
//        signInButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
//                startActivityForResult(intent, SIGN_IN_CODE);
//            }
//        });

    }
//
//    @Override
//    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
//
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (requestCode == SIGN_IN_CODE) {
//            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
//            metodoSignInResult(result);
//        }
//
//
//    }
//
//    private void metodoSignInResult(GoogleSignInResult result) {
//        if (result.isSuccess()) {
//            goMainScreen();
//        } else {
//            Toast.makeText(this, "No inicio sesion", Toast.LENGTH_SHORT).show();
//        }
//
//
//    }
//
//    private void goMainScreen() {
//        Intent intent = new Intent(this, BusinessActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | intent.FLAG_ACTIVITY_CLEAR_TASK | intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(intent);
//    }
}
