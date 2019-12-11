package com.cudpast.app.patientApp.Activities.Option;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;

import com.cudpast.app.patientApp.Activities.Option.extra.PlasmaPerfilActivity;
import com.cudpast.app.patientApp.Common.Common;
import com.cudpast.app.patientApp.Model.DoctorProfile;
import com.cudpast.app.patientApp.R;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class ListPlasmaActivity extends AppCompatActivity {

    private static final String TAG = ListPlasmaActivity.class.getSimpleName();
    private RecyclerView mBlogList;

    private DatabaseReference DbRef_TB_AVAILABLE_DOCTOR;
    private DatabaseReference refDB_PlasmaDoctor;

    private int distance = 5;
    private static final int LIMIT = 10;
    FirebaseRecyclerAdapter<DoctorProfile, ListDoctorActivity.myViewHolder> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plasma);
        getSupportActionBar().setTitle("Lista de Enfermera");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //1.Hacer la referencia a la tabla
        DbRef_TB_AVAILABLE_DOCTOR = FirebaseDatabase.getInstance().getReference(Common.TB_AVAILABLE_DOCTOR);
        DbRef_TB_AVAILABLE_DOCTOR.keepSynced(true);
        DbRef_TB_AVAILABLE_DOCTOR.orderByKey();
        //
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
        activar();
    }

    private void activar() {
        Log.e(TAG, "============ Activar ===========");
        DbRef_TB_AVAILABLE_DOCTOR.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.e(TAG, "onDataChange = " + dataSnapshot.toString());
                if (dataSnapshot.getValue() != null) {
                    Log.e(TAG, "0 ");
                    loadDoctorAvailableOnMap();
                } else {

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "onCancelled " + databaseError.toString());
            }
        });

    }

    private void loadDoctorAvailableOnMap() {
        GeoFire gf = new GeoFire(DbRef_TB_AVAILABLE_DOCTOR);
        Log.e(TAG, "1 ");
        GeoLocation pacienetGeo = new GeoLocation(-12.071867, -76.959285);
        Log.e(TAG, "2");
        GeoQuery geoQuery = gf.queryAtLocation(pacienetGeo, distance);
        geoQuery.removeAllListeners();
        geoQuery
                .addGeoQueryEventListener(new GeoQueryEventListener() {
                    @Override
                    public void onKeyEntered(String key, final GeoLocation location) {
                        Log.e(TAG, "3 ");
                        Query aux = refDB_PlasmaDoctor.orderByChild("uid ").equalTo(key);
                        Log.e(TAG, "aux " + aux);
                        DatabaseReference databaseReference_aux = aux.getRef();
                        Log.e(TAG, "databaseReference_aux = " + databaseReference_aux);
                        Query query = databaseReference_aux
                                .orderByChild("especialidad")
                                .equalTo("Plasma");

                        Log.e(TAG, "query =  " + query);
                        Log.e(TAG, "4 ");

                        adapter = new FirebaseRecyclerAdapter<DoctorProfile, ListDoctorActivity.myViewHolder>(
                                DoctorProfile.class,
                                R.layout.doctor_layout_info,
                                ListDoctorActivity.myViewHolder.class,
                                query) {

                            @Override
                            protected void populateViewHolder(final ListDoctorActivity.myViewHolder view, final DoctorProfile model, int position) {

                                view.setImage(getApplicationContext(), model.getImagePhoto());
                                view.setFirstName(model.getFirstname() + " " + model.getLastname());
                                view.setPhone(model.getNumphone());
                                view.setEspecialidad(model.getEspecialidad());
                                view.container.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_transition_animation));

                                view.mView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent i = new Intent(view.mView.getContext(), PlasmaPerfilActivity.class);
                                        i.putExtra("doctor_uid", model.getUid());
                                        i.putExtra("doctor_img", model.getImagePhoto());
                                        i.putExtra("doctor_name", model.getFirstname());
                                        i.putExtra("doctor_last", model.getLastname());
                                        i.putExtra("doctor_phone", model.getNumphone());
                                        i.putExtra("doctor_especilidad", model.getEspecialidad());
                                        view.mView.getContext().startActivity(i);
                                    }
                                });
                            }
                        };


                    }

                    @Override
                    public void onKeyExited(String key) {
                    }

                    @Override
                    public void onKeyMoved(String key, GeoLocation location) {

                    }

                    @Override
                    public void onGeoQueryReady() {
                        if (distance <= LIMIT) {
                            distance++;
                            loadDoctorAvailableOnMap();

                        }
                    }

                    @Override
                    public void onGeoQueryError(DatabaseError error) {

                    }
                });
        mBlogList.setAdapter(adapter);

    }


}
