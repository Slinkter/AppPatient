package com.cudpast.app.patientApp.Activities;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.cudpast.app.patientApp.Common.Common;
import com.cudpast.app.patientApp.Model.PacientProfile;
import com.cudpast.app.patientApp.R;
import com.cudpast.app.patientApp.helper.Token;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.rengwuxian.materialedittext.MaterialEditText;

import dmax.dialog.SpotsDialog;

public class LoginActivity extends AppCompatActivity implements TextWatcher, CompoundButton.OnCheckedChangeListener {

    private static final String TAG = LoginActivity.class.getSimpleName();

    private Button btn_login, btn_register;
    private FirebaseAuth auth;
    private FirebaseDatabase db;
    private DatabaseReference users;

    private TextView txt_forgot_pwd;

    private static final int MY_PERMISSION_REQUEST_CODE = 7000;

    //Check
    private MaterialEditText ed_login_email, ed_login_pwd;
    private CheckBox checkBox;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    public static final String PREF_NAME = "prefs";
    public static final String KEY_REMEMBER = "remeber";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_PASS = "password";

    // GPS
    LocationManager locationManager;
    AlertDialog alert = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);
        getSupportActionBar().hide();

        permisos();
        //AppIntro
        //        Intent intent = new Intent(this, IntroActivity.class);
        //      startActivity(intent);
        //

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        users = db.getReference("db_usuarios");
        btn_login = findViewById(R.id.btn_login);
        btn_register = findViewById(R.id.btn_register);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        //check
        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        ed_login_email = findViewById(R.id.ed_login_email);
        ed_login_pwd = findViewById(R.id.ed_login_pwd);
        checkBox = (CheckBox) findViewById(R.id.rem_userpass);

        if (sharedPreferences.getBoolean(KEY_REMEMBER, false)) {
            checkBox.setChecked(true);
        } else {
            checkBox.setChecked(false);
        }

        ed_login_email.setText(sharedPreferences.getString(KEY_USERNAME, ""));
        ed_login_pwd.setText(sharedPreferences.getString(KEY_PASS, ""));

        ed_login_email.addTextChangedListener(this);
        ed_login_pwd.addTextChangedListener(this);
        checkBox.setOnCheckedChangeListener(this);


        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);

            }
        });

        txt_forgot_pwd = findViewById(R.id.txt_forgot_password);
        txt_forgot_pwd.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                showDialogForgotPwd();
                return false;
            }
        });

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            //   AlertNoGps();
            Log.e(TAG, "gps no activado");
        }
    }

    //GPS
    private void AlertNoGps() {
        try {
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(LoginActivity.this);
            LayoutInflater inflater = getLayoutInflater();
            View view = inflater.inflate(R.layout.alert_location_null, null);
            builder.setView(view);
            builder.setCancelable(false);
            view.setKeepScreenOn(true);
            final android.app.AlertDialog dialog = builder.create();
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.show();

            Button btn_gps_active = view.findViewById(R.id.btn_gps_active);
            Button btn_gps_cancele = view.findViewById(R.id.btn_gps_cancele);

            btn_gps_active.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    dialog.dismiss();
                }
            });

            btn_gps_cancele.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            dialog.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
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
            Log.e(TAG, "si tiene los permisos");
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e(TAG, "si tiene los permisos  v2 ");
                }
            }
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        managePrefs();
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        managePrefs();
    }
    //
    private void managePrefs() {
        if (checkBox.isChecked()) {
            editor.putString(KEY_USERNAME, ed_login_email.getText().toString().trim());
            editor.putString(KEY_PASS, ed_login_pwd.getText().toString().trim());
            editor.putBoolean(KEY_REMEMBER, true);
            editor.apply();
        } else {
            editor.putBoolean(KEY_REMEMBER, true);
            editor.remove(KEY_PASS);
            editor.remove(KEY_USERNAME);
            editor.apply();
        }
    }

    //.
    private void showDialogForgotPwd() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(LoginActivity.this);
        alertDialog.setTitle("Recuperar Contraseña");
        alertDialog.setMessage("Escriba su correo");

        LayoutInflater inflater = LayoutInflater.from(LoginActivity.this);
        View forgot_pwd_layout = inflater.inflate(R.layout.layout_forgot_pwd, null);

        final MaterialEditText editEmail = (MaterialEditText) forgot_pwd_layout.findViewById(R.id.edtEmailForgot);
        alertDialog.setView(forgot_pwd_layout);

        alertDialog.setPositiveButton("ENVIAR", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialogInterface, int i) {

                final SpotsDialog waitingDialog = new SpotsDialog(LoginActivity.this, R.style.DialogResetearPassword);
                waitingDialog.show();

                auth.sendPasswordResetEmail(editEmail.getText().toString().trim())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                dialogInterface.dismiss();
                                waitingDialog.dismiss();
                                Toast.makeText(LoginActivity.this, "Revise su correo", Toast.LENGTH_SHORT).show();

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                dialogInterface.dismiss();
                                waitingDialog.dismiss();
                                Toast.makeText(LoginActivity.this, "error" + e.getMessage(), Toast.LENGTH_SHORT).show();

                            }
                        });
            }
        });

        alertDialog.setNegativeButton("CANCELAR", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        alertDialog.show();
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            if (user.isEmailVerified()) {
                //  Toast.makeText(this, "Correo verificado", Toast.LENGTH_SHORT).show();
                //     Snackbar.make(root, "Correo verificado", Snackbar.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "correo no verificado", Toast.LENGTH_SHORT).show();
            // Snackbar.make(root, "correo no verificado", Snackbar.LENGTH_SHORT).show();
        }
    }

    public void login() {
        //
        final SpotsDialog waitingDialog = new SpotsDialog(LoginActivity.this, R.style.DialogLogin);
        waitingDialog.show();
        //
        String email = ed_login_email.getText().toString();
        String pwd = ed_login_pwd.getText().toString();

        if (TextUtils.isEmpty(ed_login_email.getText().toString())) {
            waitingDialog.dismiss();
            Toast.makeText(this, "Ingrese su correo", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(ed_login_pwd.getText().toString())) {
            waitingDialog.dismiss();
            Toast.makeText(this, "Contraseña vacia", Toast.LENGTH_SHORT).show();
            return;
        }

        //
        auth
                .signInWithEmailAndPassword(email, pwd)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {

                        Log.e(TAG, "onSuccess");
                        final FirebaseUser user = auth.getCurrentUser();

                        if (user.isEmailVerified()) {
                            Log.e(TAG, "isEmailVerified");
                            updateUI(user);
                            String user_Uid = auth.getCurrentUser().getUid();

                            FirebaseDatabase
                                    .getInstance()
                                    .getReference(Common.TB_INFO_PACIENTE)
                                    .child(user_Uid)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            waitingDialog.dismiss();

                                            PacientProfile pacientProfile001 = dataSnapshot.getValue(PacientProfile.class);

                                            Common.currentPacientProfile = pacientProfile001;
                                            if (Common.currentPacientProfile != null) {
                                                String tokenPaciente = FirebaseInstanceId.getInstance().getToken();
                                                updateTokenToServer(tokenPaciente);
                                                Log.e(TAG, "currentPacientProfile : " + Common.currentPacientProfile.getFirstname());
                                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                Toast.makeText(LoginActivity.this, "El usuario esta registrado en la AppDoctor", Toast.LENGTH_SHORT).show();
                                            }

                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            waitingDialog.dismiss();
                                            Log.e("ERROR", "DatabaseError -->" + databaseError.toString());
                                            updateUI(null);
                                        }
                                    });

                        } else {
                            waitingDialog.dismiss();
                            Log.e(TAG, "NotIsEmailVerified");
                            updateUI(null);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Usuario o contraseña Incorrecto", Toast.LENGTH_SHORT).show();
                        Log.e("LoginActivity ", "onFailure : " + e.getMessage());
                        waitingDialog.dismiss();
                    }
                });

    }

    private void updateTokenToServer(String refreshedToken) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(Common.token_tbl);
        Token token = new Token(refreshedToken);
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            tokens.child(FirebaseAuth
                    .getInstance()
                    .getCurrentUser()
                    .getUid())
                    .setValue(token);
        }
    }

}
