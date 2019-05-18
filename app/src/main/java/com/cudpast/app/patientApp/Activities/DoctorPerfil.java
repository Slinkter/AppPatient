package com.cudpast.app.patientApp.Activities;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.cudpast.app.patientApp.R;
import com.squareup.picasso.Picasso;

public class DoctorPerfil extends AppCompatActivity {

    String photoDoctor;
    String firstName;
    String lastname;
    String numPhone;
    String especialidad;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.doctor_perfil);

        getSupportActionBar().setTitle("Perfil del Médico");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (getIntent() != null) {
            //get extra intent
            photoDoctor = getIntent().getExtras().getString("doctor_img");
            firstName = getIntent().getExtras().getString("doctor_name");
            lastname = getIntent().getExtras().getString("doctor_last");
            numPhone = getIntent().getExtras().getString("doctor_phone");
            especialidad = getIntent().getExtras().getString("doctor_especilidad");
            // init views-xml
            ImageView imgdoc = findViewById(R.id.aa_thumbnail);
            TextView post_firstName = findViewById(R.id.aa_firstname);
            TextView post_lastName = findViewById(R.id.aa_lastname);
            TextView post_phone = findViewById(R.id.aa_phone);
            TextView post_especialidad = findViewById(R.id.aa_especialidad);
            //setting views
            post_firstName.setText(firstName);
            post_lastName.setText(lastname);
            post_phone.setText(numPhone);
            post_especialidad.setText(especialidad);
            Picasso
                    .with(this)
                    .load(photoDoctor)
                    .resize(500, 500)
                    .centerInside()
                    .placeholder(R.drawable.ic_doctorapp)
                    .error(R.drawable.ic_doctorapp)
                    .into(imgdoc);
            //Call phone
            Button btnsgi = findViewById(R.id.btn_phoneDoctor);
            btnsgi.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + numPhone));
                    startActivity(intent);
                }
            });

        }


    }
}
