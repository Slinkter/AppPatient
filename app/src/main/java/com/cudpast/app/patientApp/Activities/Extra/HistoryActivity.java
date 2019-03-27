package com.cudpast.app.patientApp.Activities.Extra;

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

import com.cudpast.app.patientApp.Activities.DoctorPerfilActivity;
import com.cudpast.app.patientApp.Activities.ListDoctorActivity;
import com.cudpast.app.patientApp.Common.Common;
import com.cudpast.app.patientApp.Model.Doctor;
import com.cudpast.app.patientApp.Model.DoctorPerfil;
import com.cudpast.app.patientApp.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView mBlogList;
    private DatabaseReference AppPaciente_history;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        auth = FirebaseAuth.getInstance();
        String userUID = auth.getCurrentUser().getUid();

        AppPaciente_history = FirebaseDatabase.getInstance().getReference(Common.AppPaciente_history).child(userUID);

    //    mDatabase = FirebaseDatabase.getInstance().getReference().child("db_doctor_consulta");
        AppPaciente_history.keepSynced(true);
        AppPaciente_history.orderByKey();

        mBlogList = findViewById(R.id.myrecycleviewHistory);
        mBlogList.setHasFixedSize(true);
        mBlogList.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<DoctorPerfil, BlogViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<DoctorPerfil, BlogViewHolder>  (DoctorPerfil.class, R.layout.doctor_layout_info, BlogViewHolder.class, AppPaciente_history) {

            @Override
            protected void populateViewHolder(BlogViewHolder viewHolder, DoctorPerfil model, int position) {

                viewHolder.setImage(getApplicationContext(), model.getImage());
                viewHolder.setFirstName(model.getFirstname());
                viewHolder.setLastName(model.getLastname());
                viewHolder.setPhone(model.getNumphone());
                viewHolder.setEspecialidad(model.getEspecialidad());

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
