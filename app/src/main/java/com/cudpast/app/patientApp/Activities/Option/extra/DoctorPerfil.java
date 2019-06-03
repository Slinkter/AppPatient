package com.cudpast.app.patientApp.Activities.Option.extra;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cudpast.app.patientApp.Adapter.CommentAdapter;
import com.cudpast.app.patientApp.Common.Common;
import com.cudpast.app.patientApp.Model.Comment;
import com.cudpast.app.patientApp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DoctorPerfil extends AppCompatActivity {

    String photoDoctor;
    String firstName;
    String lastname;
    String numPhone;
    String especialidad;
    String doctor_uid;


    RecyclerView RVComment;
    CommentAdapter commentAdapter;
    List<Comment> listComment;
    static String COMMENT_KEY = "AppDoctor_history_Comment";

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;
    String PostKey;

    DatabaseReference AppDoctor_history_Comment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.doctor_perfil);

        getSupportActionBar().setTitle("Perfil del Médico");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

//        Window w = getWindow();
//        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        RVComment = findViewById(R.id.myrecycleviewComments);
        AppDoctor_history_Comment = FirebaseDatabase.getInstance().getReference(Common.AppDoctor_history_Comment);

        if (getIntent() != null) {
            //get extra intent
            photoDoctor = getIntent().getExtras().getString("doctor_img");
            firstName = getIntent().getExtras().getString("doctor_name");
            lastname = getIntent().getExtras().getString("doctor_last");
            numPhone = getIntent().getExtras().getString("doctor_phone");
            especialidad = getIntent().getExtras().getString("doctor_especilidad");
            doctor_uid = getIntent().getExtras().getString("doctor_uid");


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

        initRVComment();
    }

    private void initRVComment() {

        RVComment.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        DatabaseReference commentRef = AppDoctor_history_Comment.child(doctor_uid);
        commentRef
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        listComment = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Comment comment = snapshot.getValue(Comment.class);
                            listComment.add(comment);
                        }
                        commentAdapter = new CommentAdapter(getApplicationContext(), listComment);
                        RVComment.setAdapter(commentAdapter);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }


    private String timestampToString(long time) {
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(time);
        String date = DateFormat.format("dd-MM-yyyy", calendar).toString();
        return date;
    }

    private void showMessage(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }


}
