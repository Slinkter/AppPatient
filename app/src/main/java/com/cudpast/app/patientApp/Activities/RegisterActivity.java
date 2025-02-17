package com.cudpast.app.patientApp.Activities;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.Calendar;

import dmax.dialog.SpotsDialog;

public class RegisterActivity extends AppCompatActivity {

    private static String TAG = RegisterActivity.class.getSimpleName();
    private FirebaseAuth auth;
    private DatabaseReference tb_Info_Paciente;
    private Button btnCreatePacientUser;
    private MaterialEditText
            signupEmail,
            signupPassword,
            signupName,
            signupLast,
            signupNumPhone,
            signupDate,
            signupAnddress,
            signupDNI;

    //--> fecha
    private EditText msignupDate;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    public int year_n, month_n, day_n;
    //--> Validation
    private Animation animation;
    private Vibrator vib;

    PacientProfile newPaciente;

    SpotsDialog waitingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getSupportActionBar().setTitle("Registro");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //---------------Instancia de Firebase
        auth = FirebaseAuth.getInstance();
        tb_Info_Paciente = FirebaseDatabase.getInstance().getReference(Common.TB_INFO_PACIENTE);
        //--------------->Botones
        btnCreatePacientUser = findViewById(R.id.btnCreatePacientUser);
        animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.shake);
        vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        //--------------->Formulario- Registro
        signupName = findViewById(R.id.signupName);
        signupLast = findViewById(R.id.signupLast);
        signupNumPhone = findViewById(R.id.signupNumPhone);
        signupDate = findViewById(R.id.signupDate);//fecha de nacimiento
        signupAnddress = findViewById(R.id.signupAnddress);
        signupDNI = findViewById(R.id.signupDNI);
        signupEmail = findViewById(R.id.signupUser);
        signupPassword = findViewById(R.id.signupPassword);
        //--------------->OBTENER FECHA DE NACIMIENTO
        msignupDate = findViewById(R.id.signupDate);
        msignupDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        RegisterActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        mDateSetListener, year, month, day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });
        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                String fechauser = year + "/" + month + "/" + dayOfMonth;
                year_n = year;
                month_n = month;
                day_n = dayOfMonth;
                msignupDate.setText(fechauser);
            }
        };
        signupDate = findViewById(R.id.signupDate);
        //
        waitingDialog = new SpotsDialog(RegisterActivity.this, R.style.RegsiterActivity);
        btnCreatePacientUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                init();
            }
        });

    }

    private void init() {
        if (submitForm()) {
            waitingDialog.show();


            //



            //
            String mail = signupEmail.getText().toString().trim();
            String pwd = signupPassword.getText().toString().trim();
            //
            auth.createUserWithEmailAndPassword(mail, pwd)
                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            newPaciente = new PacientProfile();
                            newPaciente.setUid(authResult.getUser().getUid());
                            newPaciente.setFirstname(signupName.getText().toString());
                            newPaciente.setLastname(signupLast.getText().toString());
                            newPaciente.setPhone(signupNumPhone.getText().toString());
                            newPaciente.setAddress(signupAnddress.getText().toString());
                            newPaciente.setDni(signupDNI.getText().toString());
                            newPaciente.setMail(signupEmail.getText().toString().trim());
                            newPaciente.setDateborn(signupDate.getText().toString());

                            tb_Info_Paciente
                                    .child(authResult.getUser().getUid())
                                    .setValue(newPaciente)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            generarToken();
                                            sendEmailVerification();
                                            goToLoginActivity();
                                            Log.e(TAG, " onSuccess : El usuario tiene que verificar su correo ");
                                            Log.e(TAG, " onSuccess : El usuario fue registrado en la base de datos Firebase ");
                                            Toast.makeText(RegisterActivity.this, "Verificar cuenta por correo ", Toast.LENGTH_SHORT).show();
                                            waitingDialog.dismiss();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            waitingDialog.dismiss();
                                            Toast.makeText(RegisterActivity.this, "No se pudo registar usuario", Toast.LENGTH_SHORT).show();

                                        }
                                    });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            waitingDialog.dismiss();
                            Log.e(TAG, "El correo ya existe -->" + e.getMessage());
                            Toast.makeText(RegisterActivity.this, "Error al registrar usuario ", Toast.LENGTH_SHORT).show();

                        }
                    });
        }
    }

    //METODOS

    public void generarToken() {
        Log.e(TAG, "Generar Token ");

        FirebaseDatabase dbMain = FirebaseDatabase.getInstance();
        DatabaseReference refDB_tokens = dbMain.getReference(Common.token_tbl);
        String newToken = FirebaseInstanceId.getInstance().getToken();

        Token token = new Token(newToken);
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String doctorUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            refDB_tokens
                    .child(doctorUID)
                    .setValue(token);
            Common.token_doctor = token.getToken();
            Log.e("new token =  ", newToken);
        }
    }

    public void sendEmailVerification() {

        auth
                .getCurrentUser()
                .sendEmailVerification()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this, "Verificar correo ", Toast.LENGTH_SHORT).show();
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(RegisterActivity.this, "Error al verificar correo ", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    public void goToLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    // Validación de formulario parte 1

    private boolean submitForm() {

        if (!checkName()) {
            signupName.setAnimation(animation);
            signupName.startAnimation(animation);
            vib.vibrate(120);
            return false;
        }

        if (!checkLast()) {
            signupLast.setAnimation(animation);
            signupLast.startAnimation(animation);
            vib.vibrate(120);
            return false;
        }

        if (!checkNumPhone()) {
            signupNumPhone.setAnimation(animation);
            signupNumPhone.startAnimation(animation);
            vib.vibrate(120);
            return false;
        }

        if (!checkUser()) {
            signupEmail.setAnimation(animation);
            signupEmail.startAnimation(animation);
            vib.vibrate(120);
            return false;
        }

        if (!checkPassword()) {
            signupPassword.setAnimation(animation);
            signupPassword.startAnimation(animation);
            vib.vibrate(120);
            return false;
        }

        if (!checkDNI()) {
            signupDNI.setAnimation(animation);
            signupDNI.startAnimation(animation);
            vib.vibrate(120);
            return false;
        }
        return true;
    }

    // Validación de formulario parte 2
    private boolean checkName() {
        if (signupName.getText().toString().trim().isEmpty()) {
            signupName.setError("Debes ingresar tu nombre  ");
            return false;
        }
        return true;
    }

    private boolean checkLast() {
        if (signupLast.getText().toString().trim().isEmpty()) {
            signupLast.setError("Debes ingresar tus apellidos ");
            return false;
        }
        return true;
    }

    private boolean checkNumPhone() {

        if (signupNumPhone.getText().toString().trim().isEmpty()) {
            signupNumPhone.setError("debes ingresar tu numero ");
            return false;
        }
        if (signupNumPhone.length() < 8) {
            signupNumPhone.setError("son 9 digitos  ");
            return false;
        }
        return true;
    }

    private boolean checkDateBirth() {
        if (signupDate.getText().toString().trim().isEmpty()) {
            signupDate.setError("Debes ingresar tu fecha de nacimiento");
            return false;
        }
        return true;
    }

    private boolean checkAnddress() {
        if (signupPassword.getText().toString().trim().isEmpty()) {
            signupPassword.setError("Debes ingresar tu dirección");
            return false;
        }
        return true;
    }

    private boolean checkDNI() {
        if (signupDNI.length() < 8) {
            signupDNI.setError("Son 8 digitos");
            return false;
        }
        return true;
    }

    private boolean checkUser() {
        if (signupEmail.getText().toString().trim().isEmpty()) {
            signupEmail.setError("Correo invalido");
            return false;
        }
        return true;
    }

    private boolean checkPassword() {
        if (signupPassword.getText().toString().trim().isEmpty()) {
            signupPassword.setError("Ingresar una contraseña");
            return false;
        }
        return true;
    }


}
