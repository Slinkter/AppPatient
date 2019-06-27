package com.cudpast.app.patientApp.Activities.Option.extra;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.cudpast.app.patientApp.Adapter.CommentAdapter;
import com.cudpast.app.patientApp.Business.PlasmaWaiting;
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

public class PlasmaPerfilActivity extends AppCompatActivity {

    private static final String TAG = PlasmaPerfilActivity.class.getSimpleName();

    // plama info
    String photoDoctor;
    String firstName;
    String lastname;
    String numPhone;
    String especialidad;
    String doctor_uid;
    //
    RecyclerView RVComment;
    CommentAdapter commentAdapter;
    List<Comment> listComment;
    static String COMMENT_KEY = "AppDoctor_history_Comment";
    String PostKey;

    //
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference AppDoctor_history_Comment;

    //
    Button btn_cancelar_plasma;
    AlertDialog.Builder builder;
    LayoutInflater inflater;
    View view;
    AlertDialog dialog;
    TextView xml_countDown;
    CountDownTimer yourCountDownTimer;
    //
    Button btn_call_plasma;
    Button btn_plasmaBooking;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plasma_perfil);

        getSupportActionBar().setTitle("Perfil del Plasma");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


//        Window w = getWindow();
//        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        RVComment = findViewById(R.id.myrecycleviewComments);
        AppDoctor_history_Comment = FirebaseDatabase.getInstance().getReference(Common.AppDoctor_history_Comment);


        if (getIntent() != null) {
            //get extra intent

            Log.e(TAG, getIntent().getExtras().getString("doctor_uid"));
            Log.e(TAG, getIntent().getExtras().getString("doctor_name"));
            Log.e(TAG, getIntent().getExtras().getString("doctor_last"));


            photoDoctor = getIntent().getExtras().getString("doctor_img");
            firstName = getIntent().getExtras().getString("doctor_name");
            lastname = getIntent().getExtras().getString("doctor_last");
            numPhone = getIntent().getExtras().getString("doctor_phone");
            especialidad = getIntent().getExtras().getString("doctor_especilidad");

            doctor_uid = getIntent().getExtras().getString("doctor_uid");

            // init views-xml
            ImageView imgdoc = findViewById(R.id.aa_thumbnail_plasma);
            TextView post_firstName = findViewById(R.id.aa_firstname_plasma);
            TextView post_lastName = findViewById(R.id.aa_lastname_plasma);
            TextView post_phone = findViewById(R.id.aa_phone_plasma);
            TextView post_especialidad = findViewById(R.id.aa_especialidad_plasma);
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


            btn_call_plasma = findViewById(R.id.btn_call_plasma);
            btn_plasmaBooking = findViewById(R.id.btn_plasmaBooking);
            //
            btn_call_plasma.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + numPhone));
                    startActivity(intent);
                }
            });

            btn_plasmaBooking.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showAlertDialogBooking();
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

    private void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public void showAlertDialogBooking() {
           /*
            Intent waiting = new Intent(PlasmaPerfilActivity.this   , PlasmaWaiting.class);
            startActivity(waiting);
            finish();
            */
        try {
            AlertDialog.Builder mBuiler = new AlertDialog.Builder(PlasmaPerfilActivity.this);
            View view = getLayoutInflater().inflate(R.layout.plasma_booking_waiting, null);
            mBuiler.setView(view);
            mBuiler.setCancelable(false);
            AlertDialog dialog = mBuiler.create();
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


}
