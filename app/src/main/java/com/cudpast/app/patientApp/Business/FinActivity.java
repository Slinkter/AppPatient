package com.cudpast.app.patientApp.Business;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.cudpast.app.patientApp.Activities.MainActivity;
import com.cudpast.app.patientApp.Common.Common;
import com.cudpast.app.patientApp.Model.DoctorPerfil;
import com.cudpast.app.patientApp.Model.User;
import com.cudpast.app.patientApp.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

public class FinActivity extends AppCompatActivity {

    private ImageView image_doctor;
    private TextView tv_doctor_firstname;
    private TextView tv_doctor_lastName,c_especialidad,c_tiempo,c_servicio;

    private TextView tv_paciente_firstname;
    private TextView tv_paciente_lastName;

    private Button btn_fin_atencion;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fin);
        getSupportActionBar().hide();

        image_doctor = findViewById(R.id.fin_doctorImage);
        tv_doctor_firstname = findViewById(R.id.fin_doctorFirstNameFin);
        tv_doctor_lastName = findViewById(R.id.fin_doctorLastNameFin);

        c_especialidad = findViewById(R.id.c_especialidad);
        c_tiempo = findViewById(R.id.c_tiempo);
        c_servicio = findViewById(R.id.c_servicio);



        tv_paciente_firstname = findViewById(R.id.fin_pacienteFirstName);
        tv_paciente_lastName = findViewById(R.id.fin_pacienteLastName);

        metodoSignInResult();

        btn_fin_atencion = findViewById(R.id.btn_fin_atencion);
        btn_fin_atencion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FinActivity.this, MainActivity.class);
                startActivity(intent);

            }
        });



    }


    private void metodoSignInResult() {
        try {
            DoctorPerfil currentDoctor = Common.currentDoctor;
            User currentUser = Common.currentUser;

            tv_doctor_firstname.setText(currentDoctor.getFirstname());
            tv_doctor_lastName.setText(currentDoctor.getLastname());
            c_especialidad.setText(currentDoctor.getEspecialidad());
            c_tiempo.setText("30 min");
            c_servicio.setText("Consulta medica");


            tv_paciente_firstname.setText(currentUser.getNombre());
            tv_paciente_lastName.setText(currentUser.getApellido());

            Picasso
                    .with(this)
                    .load(currentDoctor.getImage())
                    .placeholder(R.drawable.ic_doctorapp)
                    .error(R.drawable.ic_doctorapp)
                    .into(image_doctor);

            Picasso
                    .with(this)
                    .load(currentDoctor.getImage())
                    .placeholder(R.drawable.ic_client)
                    .error(R.drawable.ic_client)
                    .into(image_doctor);





        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
