package com.cudpast.app.patientApp.Activities.Option;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cudpast.app.patientApp.Activities.Option.extra.DoctorPerfilActivity;
import com.cudpast.app.patientApp.Model.DoctorProfile;
import com.cudpast.app.patientApp.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

public class ListDoctorActivity extends AppCompatActivity {

    private RecyclerView mBlogList;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_doctor);
        getSupportActionBar().setTitle("Lista de Medicos");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //1.Hacer la referencia a la tabla
        mDatabase = FirebaseDatabase.getInstance().getReference().child("tb_Info_Doctor");
        mDatabase.keepSynced(true);
        mDatabase.orderByKey();
        //2.
        mBlogList = findViewById(R.id.myrecycleview);
        mBlogList.setHasFixedSize(true);

        mBlogList.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<DoctorProfile, myViewHolder> adapter;
        adapter = new FirebaseRecyclerAdapter<DoctorProfile, myViewHolder>(
                DoctorProfile.class,
                R.layout.doctor_layout_info,
                myViewHolder.class,
                mDatabase.orderByChild("especialidad").equalTo("Medicina General")) {

            @Override
            protected void populateViewHolder(final myViewHolder view, final DoctorProfile model, int position) {

                view.setImage(getApplicationContext(), model.getImagePhoto());
                view.setFirstName(model.getFirstname() + " " + model.getLastname());
                view.setPhone(model.getNumphone());
                view.setEspecialidad(model.getEspecialidad());

                final String codmedpe = model.getUid();
                final String correoG = model.getFirstname();
                final String direccion = model.getFirstname();
                final String dni = model.getFirstname();
                final String especialidad = model.getEspecialidad();
                final String fecha = model.getEspecialidad();
                final String firstName = model.getFirstname();
                final String img = model.getImagePhoto();
                final String lastname = model.getLastname();
                final String numPhone = model.getNumphone();
                final String pwd = model.getNumphone();
                final String uid = model.getNumphone();
                //Abrir perfil de doctor

                //  view.container.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_transition_animation));
                view.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(view.mView.getContext(), DoctorPerfilActivity.class);
                        i.putExtra("doctor_uid", codmedpe);
                        i.putExtra("doctor_img", img);
                        i.putExtra("doctor_name", firstName);
                        i.putExtra("doctor_last", lastname);
                        i.putExtra("doctor_phone", numPhone);
                        i.putExtra("doctor_especilidad", especialidad);
                        view.mView.getContext().startActivity(i);
                    }
                });
            }
        };

        mBlogList.setAdapter(adapter);
    }

    public static class myViewHolder extends RecyclerView.ViewHolder {

        View mView;
        RelativeLayout container;

        public myViewHolder(@NonNull final View itemView) {
            super(itemView);
            mView = itemView;
            container = mView.findViewById(R.id.containerDoctorInfo);
        }

        public void setImage(Context context, String image) {
            ImageView post_image = mView.findViewById(R.id.profile_doctor_image);
            Picasso
                    .with(context)
                    .load(image)
                    .resize(300, 300)
                    .centerInside()
                    .placeholder(R.drawable.ic_doctorapp)
                    .error(R.drawable.ic_doctorapp)
                    .into(post_image);
        }

        public void setFirstName(String firstName) {
            TextView post_firstName = mView.findViewById(R.id.profile_doctor_firstname);
            post_firstName.setText(firstName);
        }

        public void setEspecialidad(String especialidad) {
            TextView post_especialidad = mView.findViewById(R.id.profile_doctor_especialidad);
            post_especialidad.setText(especialidad);
        }

        public void setPhone(String phone) {
            TextView post_phone = mView.findViewById(R.id.profile_doctor_phone);
            post_phone.setText(phone);
        }


    }

}
