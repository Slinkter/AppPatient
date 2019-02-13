package com.cudpast.app.patientApp.Adapter;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.cudpast.app.patientApp.Activities.UbicacionActivity;
import com.cudpast.app.patientApp.Common.Common;
import com.cudpast.app.patientApp.Model.Doctor;
import com.cudpast.app.patientApp.Model.DoctorPerfil;
import com.cudpast.app.patientApp.R;
import com.cudpast.app.patientApp.helper.Token;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class BSRFDoctor extends BottomSheetDialogFragment {

    private static final String TAG = BSRFDoctor.class.getSimpleName();
    private String mTitle ,mSnippet;
    boolean isTapOnMap;
    TextView title,snippet;
    Button btn_yes ,btn_no;
    TextView post_firstName;
    TextView post_lastName;
    TextView post_phone;
    TextView post_especialidad;
    ImageView post_image;

    private DatabaseReference mDatabase;




    public static BSRFDoctor newInstance(String title , String snippet, boolean isTapOnMap){
        BSRFDoctor f = new BSRFDoctor();
        Bundle args = new Bundle();
        args.putString("title",title);
        args.putString("snippet",snippet);
        args.putBoolean("isTapOnMap",isTapOnMap);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTitle = getArguments().getString("title");
        mSnippet = getArguments().getString("snippet");
        isTapOnMap = getArguments().getBoolean("isTapOnMap");


    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view  = inflater.inflate(R.layout.botton_sheet_doctor,container,false);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("tb_Info_Doctor");
        mDatabase.keepSynced(true);

        // llenar to_do el xml
        title = (TextView) view.findViewById(R.id.txt_doctor_title);
        snippet = (TextView) view.findViewById(R.id.txt_doctor_snippet);

        btn_yes = view.findViewById(R.id.btn_yes);
        btn_no = view.findViewById(R.id.btn_no);

         post_firstName = view.findViewById(R.id.bs_doctorFirstName);
         post_lastName= view.findViewById(R.id.bs_doctorLastName);
         post_phone= view.findViewById(R.id.bs_doctorPhone);
         post_especialidad= view.findViewById(R.id.bs_doctorEspecialidad);
         post_image= view.findViewById(R.id.bs_doctorImage);


        if (!isTapOnMap){

        }else {

            mDatabase
                    .orderByKey()
                    .equalTo(mSnippet)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Log.e("mSnippet",mSnippet);
                            DoctorPerfil doctorPerfil ;
                            for (DataSnapshot post: dataSnapshot.getChildren()){
                                doctorPerfil = post.getValue(DoctorPerfil.class);
                                Log.e("doctorPerfil.uid:",doctorPerfil.getUid());
                                Log.e("doctorPerfil",doctorPerfil.toString());

                                title.setText(doctorPerfil.getFirstname());
                                snippet.setText(doctorPerfil.getCorreoG());

                                post_firstName.setText(doctorPerfil.getFirstname());
                                post_lastName.setText(doctorPerfil.getLastname());
                                post_phone.setText(doctorPerfil.getNumphone());
                                post_especialidad.setText(doctorPerfil.getEspecialidad());
                                Picasso.with(getContext())
                                        .load(doctorPerfil.getImage())
                                        .resize(300,300)
                                        .centerInside().
                                        into(post_image);
                            }




                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });




            btn_yes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getContext(), "si", Toast.LENGTH_SHORT).show();
//                    startActivity(new Intent(getActivity(),YourActivity.class));
//                    Intent ir = new Intent(getActivity(),goToMain.class);
//                    startActivity(ir);
//                    dismiss();
                }
            });

            btn_no.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getContext(), "Cancelado", Toast.LENGTH_SHORT).show();
                    dismiss();
                }
            });
        }

        return view;

    }


}
