package com.cudpast.app.patientApp.Activities.Option;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cudpast.app.patientApp.Common.Common;
import com.cudpast.app.patientApp.Model.DoctorProfile;
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
        getSupportActionBar().setTitle("Historial");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        auth = FirebaseAuth.getInstance();
        String userUID = auth.getCurrentUser().getUid();

        AppPaciente_history = FirebaseDatabase.getInstance().getReference(Common.AppPaciente_history).child(userUID);
        AppPaciente_history.keepSynced(true);
        AppPaciente_history.orderByKey();

        if (AppPaciente_history != null) {
            mBlogList = findViewById(R.id.myrecycleviewHistory);
            mBlogList.setHasFixedSize(true);
            mBlogList.setLayoutManager(new LinearLayoutManager(this));
        } else {
            Toast.makeText(this, "No tiene registros de atencion", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<DoctorProfile, BlogViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<DoctorProfile, BlogViewHolder>(DoctorProfile.class, R.layout.doctor_layout_info, BlogViewHolder.class, AppPaciente_history) {

            @Override
            protected void populateViewHolder(BlogViewHolder viewHolder, DoctorProfile model, int position) {
                viewHolder.setImage(getApplicationContext(), model.getImagePhoto());
                viewHolder.setFirstName(model.getFirstname() + " " + model.getLastname());
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
            TextView post_firstName = mView.findViewById(R.id.profile_doctor_firstname);
            post_firstName.setText(firstName);
        }

        public void setPhone(String phone) {
            TextView post_phone = mView.findViewById(R.id.profile_doctor_phone);
            post_phone.setText(phone);

        }

        public void setEspecialidad(String especialidad) {
            TextView post_especialidad = mView.findViewById(R.id.profile_doctor_especialidad);
            post_especialidad.setText(especialidad);

        }

        public void setImage(Context context, String image) {
            ImageView post_image = mView.findViewById(R.id.profile_doctor_image);
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
