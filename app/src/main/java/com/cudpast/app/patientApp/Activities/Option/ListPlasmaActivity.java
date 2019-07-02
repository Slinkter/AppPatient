package com.cudpast.app.patientApp.Activities.Option;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cudpast.app.patientApp.Activities.Option.extra.DoctorPerfilActivity;
import com.cudpast.app.patientApp.Activities.Option.extra.PlasmaPerfilActivity;
import com.cudpast.app.patientApp.Common.Common;
import com.cudpast.app.patientApp.Model.DoctorPerfil;
import com.cudpast.app.patientApp.R;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ListPlasmaActivity extends AppCompatActivity {

    private static final String TAG = ListPlasmaActivity.class.getSimpleName();
    private RecyclerView mBlogList;

    private DatabaseReference refDB_PlasmaDoctor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plasma);
        getSupportActionBar().setTitle("Lista de Enfermera");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //1.Hacer la referencia a la tabla

        refDB_PlasmaDoctor = FirebaseDatabase.getInstance().getReference(Common.TB_INFO_DOCTOR);
        refDB_PlasmaDoctor.keepSynced(true);
        refDB_PlasmaDoctor.orderByKey();

        //2.
        mBlogList = findViewById(R.id.myrecycleviewPlasma);
        mBlogList.setHasFixedSize(true);
        mBlogList.setLayoutManager(new LinearLayoutManager(this));
    }


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<DoctorPerfil, ListDoctorActivity.BlogViewHolder> adapter;

        adapter = new FirebaseRecyclerAdapter<DoctorPerfil, ListDoctorActivity.BlogViewHolder>(
                DoctorPerfil.class,
                R.layout.doctor_layout_info,
                ListDoctorActivity.BlogViewHolder.class,
                refDB_PlasmaDoctor.orderByChild("especialidad").equalTo("Plasma")) {

            @Override
            protected void populateViewHolder(final ListDoctorActivity.BlogViewHolder view, final DoctorPerfil model, int position) {

                view.setImage(getApplicationContext(), model.getImage());
                view.setFirstName(model.getFirstname() + " " + model.getLastname());
                view.setPhone(model.getNumphone());
                view.setEspecialidad(model.getEspecialidad());

                view.container.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_transition_animation));
                view.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(view.mView.getContext(), PlasmaPerfilActivity.class);
                        Log.e(TAG, model.getUid());
                        Log.e(TAG, model.getFirstname());
                        Log.e(TAG, model.getLastname());

                        i.putExtra("doctor_uid", model.getUid());
                        i.putExtra("doctor_img", model.getImage());
                        i.putExtra("doctor_name", model.getFirstname());
                        i.putExtra("doctor_last", model.getLastname());
                        i.putExtra("doctor_phone", model.getNumphone());
                        i.putExtra("doctor_especilidad", model.getEspecialidad());
                        view.mView.getContext().startActivity(i);
                    }
                });
            }
        };

        mBlogList.setAdapter(adapter);
    }

}
