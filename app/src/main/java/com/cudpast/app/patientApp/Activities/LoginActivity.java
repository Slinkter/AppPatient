package com.cudpast.app.patientApp.Activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cudpast.app.patientApp.Common.Common;
import com.cudpast.app.patientApp.Model.User;
import com.cudpast.app.patientApp.R;
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
import com.rengwuxian.materialedittext.MaterialEditText;

import dmax.dialog.SpotsDialog;

public class LoginActivity extends AppCompatActivity implements TextWatcher, CompoundButton.OnCheckedChangeListener {

    private static final String TAG = "LoginActivity";

    private Button btn_login, btn_register;
    private FirebaseAuth auth;
    private FirebaseDatabase db;
    private DatabaseReference users;
    private LinearLayout root;

    private TextView txt_forgot_pwd;

    //Check
    private MaterialEditText ed_login_email, ed_login_pwd;
    private CheckBox rem_userpass;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    public static final String PREF_NAME = "prefs";
    public static final String KEY_REMEMBER = "remeber";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_PASS = "password";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportActionBar().hide();
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        users = db.getReference("db_usuarios");
        btn_login = findViewById(R.id.btn_login);
        btn_register = findViewById(R.id.btn_register);
        root = findViewById(R.id.root);

        //check
        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        ed_login_email = findViewById(R.id.ed_login_email);
        ed_login_pwd = findViewById(R.id.ed_login_pwd);
        rem_userpass = (CheckBox) findViewById(R.id.rem_userpass);

        if (sharedPreferences.getBoolean(KEY_REMEMBER, false)) {
            rem_userpass.setChecked(true);
        } else {
            rem_userpass.setChecked(false);
        }

        ed_login_email.setText(sharedPreferences.getString(KEY_USERNAME, ""));
        ed_login_pwd.setText(sharedPreferences.getString(KEY_PASS, ""));

        ed_login_email.addTextChangedListener(this);
        ed_login_pwd.addTextChangedListener(this);
        rem_userpass.setOnCheckedChangeListener(this);


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
        if (rem_userpass.isChecked()) {
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
                Toast.makeText(this, "Correo verificado", Toast.LENGTH_SHORT).show();
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
                            String user_Uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            String tabla_paciente = Common.TB_INFO_PACIENTE;
                            FirebaseDatabase
                                    .getInstance()
                                    .getReference(tabla_paciente)
                                    .child(user_Uid)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            waitingDialog.dismiss();
                                            User user001 = dataSnapshot.getValue(User.class);
                                            Common.currentUser = user001;

                                            Log.e(TAG, "currentUser ------>" + Common.currentUser.getNombre());
                                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);
                                            finish();
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

}
