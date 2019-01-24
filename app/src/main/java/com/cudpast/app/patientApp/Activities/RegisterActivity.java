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
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.cudpast.app.patientApp.Model.User;
import com.cudpast.app.patientApp.R;
import com.cudpast.app.patientApp.Soporte.VolleyRP;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;

import dmax.dialog.SpotsDialog;

public class RegisterActivity extends AppCompatActivity {

    //
    FirebaseAuth auth;
    FirebaseDatabase db;
    DatabaseReference db_users;
    //
    private static String TAG = "Date";
    private static final String IP_REGISTRAR = "http://www.cudpast.com/AppUsuario/Registro_INSERT.php";
    private RequestQueue mRequest;
    private VolleyRP volleyRP;

    EditText signupDNI,signupUser,signupPassword,signupName,signupLast,signupNumPhone,signupDate,signupAnddress;
    RelativeLayout root;
    Button guardar, salir;
    //fecha
    private EditText msignupDate;
    private DatePickerDialog.OnDateSetListener mDateSetListener;

    public int year_n,month_n,day_n;

    //Validacion
    private Animation animation;
    private Vibrator vib;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getSupportActionBar().hide();
        //-----------------------------------------------
        //---------------Instancia de Firebase
        auth = FirebaseAuth.getInstance();
        db_users = FirebaseDatabase.getInstance().getReference();
        //-----------------------------------------------
        //--------------->Servidor
        volleyRP = VolleyRP.getInstance(this);
        mRequest = volleyRP.getRequestQueue();
        //-----------------------------------------------
        //--------------->Botones
        guardar     = findViewById(R.id.btnGuardar);
        salir       = findViewById(R.id.btnSalir);
        animation   = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.shake);
        vib         = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        //-----------------------------------------------
        //--------------->Formulario- Registro
        signupName      = findViewById(R.id.signupName);
        signupLast      = findViewById(R.id.signupLast);
        signupNumPhone  = findViewById(R.id.signupNumPhone);
        signupDate      = findViewById(R.id.signupDate);//fecha de nacimiento
        signupAnddress  = findViewById(R.id.signupAnddress);
        signupDNI       = findViewById(R.id.signupDNI);
        signupUser      = findViewById(R.id.signupUser);//correo
        signupPassword  = findViewById(R.id.signupPassword);
        //----------------------------------------------->
        //--------------->fecha de nacimiento
        msignupDate = findViewById(R.id.signupDate);

        msignupDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Calendar cal = Calendar.getInstance();
                int year     = cal.get(Calendar.YEAR);
                int month    = cal.get(Calendar.MONTH);
                int day      = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        RegisterActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        mDateSetListener,year,month,day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        mDateSetListener = new DatePickerDialog.OnDateSetListener(){
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                String fechauser = year+ "/"+ month + "/" + dayOfMonth;
                year_n=year;
                month_n = month;
                day_n = dayOfMonth;
                msignupDate.setText(fechauser);
            }
        };

        signupDate = findViewById(R.id.signupDate);

        guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (submitForm()){

                    final SpotsDialog waitingDialog = new SpotsDialog(RegisterActivity.this, R.style.CustomSDialog);
                    waitingDialog.show();

                    String user = signupUser.getText().toString();
                    String pass = signupPassword.getText().toString();
                    auth.createUserWithEmailAndPassword(user, pass)
                            .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {

                                    if (task.isSuccessful()) {
                                        //Preparar los datos del usuarios para registrar
                                        User user = new User();
                                        user.setNombre(signupName.getText().toString());
                                        user.setApellido(signupLast.getText().toString());
                                        user.setTelefono(signupNumPhone.getText().toString());
                                        user.setDni(signupDNI.getText().toString());
                                        user.setCorreo(signupUser.getText().toString());
                                        user.setPassword(signupPassword.getText().toString());
                                        user.setFecha(signupDate.getText().toString());
                                        user.setDireecion(signupAnddress.getText().toString());
                                        //Guadar en Firebase
                                        db_users.child("db_usuarios")  //nombre carperta.
                                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                // .child(user.getDni()) //subcarpera.
                                                .setValue(user);       //toda la información.
                                        //Guardar en GoDaddy
                                        registrarWebGoDaddy(
                                                user.getDni(),
                                                user.getCorreo(),
                                                user.getPassword(),
                                                user.getNombre(),
                                                user.getApellido(),
                                                user.getTelefono(),
                                                user.getFecha(),
                                                user.getDireecion()
                                        );
                                        //Regresar al LoginActivity
                                        waitingDialog.dismiss();
                                        iniciarActivity();
                                        Log.d(TAG, "signInWithEmail:success");
                                    } else {
                                        waitingDialog.dismiss();
                                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                                        Toast.makeText(RegisterActivity.this, "Authentication failed.",Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });

        salir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }




    public void iniciarActivity() {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }


    //Insertar en la base de datos de Godaddy
    public void registrarWebGoDaddy(String dni, String correo, String password, String nombre, String apellido, String telefono, String fecha,String direecion) {

        HashMap<String, String> hashMapRegistro = new HashMap<>();
        hashMapRegistro.put("iddni",dni);
        hashMapRegistro.put("correo",correo);
        hashMapRegistro.put("password",password);
        hashMapRegistro.put("nombre",nombre);
        hashMapRegistro.put("apellido",apellido);
        hashMapRegistro.put("telefono",telefono);
        hashMapRegistro.put("birth",fecha);
        hashMapRegistro.put("direccion",direecion);

        JsonObjectRequest solicitar = new JsonObjectRequest(Request.Method.POST,
                IP_REGISTRAR,
                new JSONObject(hashMapRegistro),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject datos) {
                        try {
                            String estado = datos.getString("resultado");
                            if (estado.equalsIgnoreCase("Datos registrados  :) ")) {
                                Toast.makeText(RegisterActivity.this, estado, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(RegisterActivity.this, estado, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(RegisterActivity.this, "no se pudo registrar", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(RegisterActivity.this, "no se pudo registrar", Toast.LENGTH_SHORT).show();
            }
        });
        VolleyRP.addToQueue(solicitar, mRequest, this, volleyRP);
    }

    //Validación de formulario parte 1
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
            signupUser.setAnimation(animation);
            signupUser.startAnimation(animation);
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
    //Validación de formulario parte 2

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
        if (signupUser.getText().toString().trim().isEmpty()) {
            signupUser.setError("Correo invalido");
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
