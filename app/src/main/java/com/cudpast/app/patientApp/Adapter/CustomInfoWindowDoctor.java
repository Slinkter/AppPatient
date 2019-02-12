package com.cudpast.app.patientApp.Adapter;


import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.cudpast.app.patientApp.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class CustomInfoWindowDoctor implements GoogleMap.InfoWindowAdapter {

    View myView;


    public CustomInfoWindowDoctor(Context context) {
        //Obtener un XML : custom_ider_info_doctor.xml
        this.myView = LayoutInflater.from(context).inflate(R.layout.custom_ider_info_doctor,null);

//        Button btn_yes= myView.findViewById(R.id.btn_yes_doctor);
//        Button btn_no= myView.findViewById(R.id.btn_no_doctor);

//        btn_yes.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                Log.e("TAG" , " SI");
//            }
//        });
//
//
//        btn_no.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                Log.e("TAG" , " NO");
//            }
//        });
    }



    @Override
    public View getInfoWindow(Marker marker) {
//
//        TextView txt_PickupTitle = (myView.findViewById(R.id.txtPickupInfoDoc));
//        txt_PickupTitle.setText(marker.getTitle());
//
//        TextView txt_PickupSnippet = (myView.findViewById(R.id.txtPickupSnippetDoc));
//        txt_PickupSnippet.setText(marker.getSnippet());

        return myView;


    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    public void btnQuizas(View view) {

    }
}
