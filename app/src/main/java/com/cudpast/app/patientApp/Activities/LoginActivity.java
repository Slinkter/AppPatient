package com.cudpast.app.patientApp.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
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

public class LoginActivity extends AppCompatActivity {

    private RelativeLayout root;
    private Button login_button;
    private FirebaseAuth auth;
    private FirebaseDatabase db;
    private DatabaseReference users;

    private static final String TAG = "LoginActivity";

    TextView txt_forgot_pwd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().hide();
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        users = db.getReference("db_usuarios");
        login_button = findViewById(R.id.login_button);
        root = findViewById(R.id.root);

        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoginDialog();
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
                final SpotsDialog waitingDialog = new SpotsDialog(LoginActivity.this,R.style.DialogLogin);
                waitingDialog.show();

                auth.sendPasswordResetEmail(editEmail.getText().toString().trim())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                dialogInterface.dismiss();
                                waitingDialog.dismiss();
                                Toast.makeText(LoginActivity.this, "Revise su correo", Toast.LENGTH_SHORT).show();

                            }
                        }).addOnFailureListener(new OnFailureListener() {
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

    private void showLoginDialog() {

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View layout_login = inflater.inflate(R.layout.layout_login, null);
        final MaterialEditText edtEmail = layout_login.findViewById(R.id.edtEmail);
        final MaterialEditText edtPassword = layout_login.findViewById(R.id.edtPassowrd);
        dialog.setView(layout_login);
        dialog.setTitle(" ");

        final SpotsDialog waitingDialog = new SpotsDialog(LoginActivity.this, R.style.DialogLogin);
        waitingDialog.show();


        dialog.setPositiveButton("Ingresar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (TextUtils.isEmpty(edtEmail.getText().toString())) {
                    waitingDialog.dismiss();
                    Snackbar.make(root, "Error : Usuario o contraseña incorrecto", Snackbar.LENGTH_SHORT).show();

                    return;
                }

                if (TextUtils.isEmpty(edtPassword.getText().toString())) {
                    waitingDialog.dismiss();
                    Snackbar.make(root, "Error : Contraseña vacia", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if (edtEmail.getText().toString().length() < 6) {
                    waitingDialog.dismiss();
                    Snackbar.make(root, "Error : Contraseña muy Corta", Snackbar.LENGTH_SHORT).show();
                    return;
                }


                //-->Login
                auth.signInWithEmailAndPassword(edtEmail.getText().toString(), edtPassword.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {

                                Log.e(TAG, "onSuccess");
                                final FirebaseUser user = auth.getCurrentUser();

                                if (user.isEmailVerified()){
                                    Log.e(TAG, "isEmailVerified");
                                    updateUI(user);
                                    String user_Uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                    String tabla_paciente = Common.tb_Info_Paciente;
                                    FirebaseDatabase
                                            .getInstance()
                                            .getReference(tabla_paciente)
                                            .child(user_Uid)
                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    waitingDialog.dismiss();
                                                    User user001= dataSnapshot.getValue(User.class);
                                                    Common.currentUser = user001;
                                                    Log.e(TAG, "currentUser ------>" + Common.currentUser.getNombre() );
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

                                }else {
                                    waitingDialog.dismiss();
                                    Log.e(TAG, "NotIsEmailVerified");
                                    updateUI(null);
                                }

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                               // Snackbar.make(root, "Failed" + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                                Log.e("LoginActivity " , "onFailure : " + e.getMessage());
                                Snackbar.make(root, "Usuario o Contraseña Incorrecta", Snackbar.LENGTH_SHORT).show();
                                waitingDialog.dismiss();
                            }
                        });
            }
        });

        dialog.setNegativeButton("Salir", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                waitingDialog.dismiss();
            }
        });

        dialog.show();
    }

    private void updateUI(FirebaseUser user) {
        if (user !=null){
            if (user.isEmailVerified() )   {
                Toast.makeText(this, "Correo verificado", Toast.LENGTH_SHORT).show();
           //     Snackbar.make(root, "Correo verificado", Snackbar.LENGTH_SHORT).show();
            }
        }else {
            Toast.makeText(this, "correo no verificado", Toast.LENGTH_SHORT).show();
           // Snackbar.make(root, "correo no verificado", Snackbar.LENGTH_SHORT).show();
        }
    }

    public void btnregister(View view) {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
        finish();
    }




}
