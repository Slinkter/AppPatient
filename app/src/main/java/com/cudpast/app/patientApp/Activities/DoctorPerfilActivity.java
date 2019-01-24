package com.cudpast.app.patientApp.Activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.cudpast.app.patientApp.R;
import com.squareup.picasso.Picasso;

public class DoctorPerfilActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_perfil);

        String img = getIntent().getExtras().getString("doctor_img");
        String firstName = getIntent().getExtras().getString("doctor_name");
        String lastname = getIntent().getExtras().getString("doctor_last");
        String numPhone = getIntent().getExtras().getString("doctor_phone") ;
        String especialidad = getIntent().getExtras().getString("doctor_especilidad");


        // init views

//        CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.collapsingtoolbar_id);
//        collapsingToolbarLayout.setTitleEnabled(true);

        ImageView imgdoc = findViewById(R.id.aa_thumbnail);
        TextView post_firstName = findViewById(R.id.aa_firstname);
        TextView post_lastName = findViewById(R.id.aa_lastname);
        TextView post_phone = findViewById(R.id.aa_phone);
        TextView post_especialidad = findViewById(R.id.aa_especialidad);

        post_firstName.setText(firstName);
        post_lastName.setText(lastname);
        post_phone.setText(numPhone);
        post_especialidad.setText(especialidad);

        Picasso.with(this).load(img).into(imgdoc);
    }
}
