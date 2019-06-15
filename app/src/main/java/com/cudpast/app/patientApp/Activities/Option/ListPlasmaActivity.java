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

import com.cudpast.app.patientApp.Common.Common;
import com.cudpast.app.patientApp.Model.DoctorPerfil;
import com.cudpast.app.patientApp.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

public class ListPlasmaActivity extends AppCompatActivity {

    private RecyclerView mBlogList;
    private DatabaseReference refDB_PlasmaDoctor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plasma);
        getSupportActionBar().setTitle("Lista de Enfermera");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //1.Hacer la referencia a la tabla
        refDB_PlasmaDoctor = FirebaseDatabase.getInstance().getReference(Common.TB_INFO_PLASMA);

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
        adapter = new FirebaseRecyclerAdapter<DoctorPerfil, ListDoctorActivity.BlogViewHolder>(DoctorPerfil.class, R.layout.doctor_layout_info, ListDoctorActivity.BlogViewHolder.class, refDB_PlasmaDoctor) {

            @Override
            protected void populateViewHolder(final ListDoctorActivity.BlogViewHolder view, final DoctorPerfil model, int position) {

                view.setImage(getApplicationContext(), model.getImage());
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
                final String img = model.getImage();
                final String lastname = model.getLastname();
                final String numPhone = model.getNumphone();
                final String pwd = model.getNumphone();
                final String uid = model.getNumphone();
                //Abrir perfil de doctor

                //  view.container.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_transition_animation));
                view.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(view.mView.getContext(), com.cudpast.app.patientApp.Activities.Option.extra.DoctorPerfil.class);
                        i.putExtra("doctor_uid" ,codmedpe );
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

    public static class BlogViewHolder extends RecyclerView.ViewHolder {

        View mView;
        //
        TextView tv_title, tv_content, tv_date;
        ImageView img_user;
        RelativeLayout container;
        //


        public BlogViewHolder(@NonNull final View itemView) {
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
