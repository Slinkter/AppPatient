package com.cudpast.app.patientApp.Activities;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.cudpast.app.patientApp.Model.Doctor;
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
        getSupportActionBar().hide();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("db_doctor_consulta");
        mDatabase.keepSynced(true);
        mDatabase.orderByKey();

        mBlogList = findViewById(R.id.myrecycleview);
        mBlogList.setHasFixedSize(true);
        mBlogList.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Doctor, BlogViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Doctor, BlogViewHolder>
                (Doctor.class, R.layout.doctor_layout_info, BlogViewHolder.class, mDatabase) {

            @Override
            protected void populateViewHolder(final BlogViewHolder viewHolder, final Doctor model, int position) {

                viewHolder.setImage(getApplicationContext(), model.getImage());
                viewHolder.setFirstName(model.getFirstname());
                viewHolder.setLastName(model.getLastname());
                viewHolder.setPhone(model.getNumphone());
                viewHolder.setEspecialidad(model.getEspecialidad());


                final String img = model.getImage();
                final String firstName = model.getFirstname();
                final String lastname = model.getLastname();
                final String numPhone = model.getNumphone();
                final String especialidad = model.getEspecialidad();

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(viewHolder.mView.getContext(), DoctorPerfilActivity.class);

                        i.putExtra("doctor_img", img);
                        i.putExtra("doctor_name", firstName);
                        i.putExtra("doctor_last", lastname);
                        i.putExtra("doctor_phone", numPhone);
                        i.putExtra("doctor_especilidad", especialidad);


                        viewHolder.mView.getContext().startActivity(i);
                    }
                });
            }
        };

        mBlogList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class BlogViewHolder extends RecyclerView.ViewHolder {
        View mView;

        public BlogViewHolder(@NonNull final View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setFirstName(String firstName) {
            TextView post_firstName = mView.findViewById(R.id.firstname);
            post_firstName.setText(firstName);
        }

        public void setLastName(String lastName) {
            TextView post_lastName = mView.findViewById(R.id.lastname);
            post_lastName.setText(lastName);
        }

        public void setPhone(String phone) {
            TextView post_phone = mView.findViewById(R.id.phone);
            post_phone.setText(phone);

        }

        public void setEspecialidad(String especialidad) {
            TextView post_especialidad = mView.findViewById(R.id.especialidad);
            post_especialidad.setText(especialidad);

        }

        public void setImage(Context context, String image) {
            ImageView post_image = mView.findViewById(R.id.profile_image);
            Picasso.with(context)
                    .load(image)
                    .resize(300, 300)
                    .centerInside()
                    .placeholder(R.drawable.ic_doctorapp)
                    .error(R.drawable.ic_doctorapp)
                    .into(post_image);

        }


    }
}
