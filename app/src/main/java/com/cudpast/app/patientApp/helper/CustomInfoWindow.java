package com.cudpast.app.patientApp.helper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.cudpast.app.patientApp.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class CustomInfoWindow implements GoogleMap.InfoWindowAdapter {

    View myView;

    public CustomInfoWindow(Context context) {
        //Obtener un XML : custom_ider_info_patient.xml
        this.myView = LayoutInflater.from(context).inflate(R.layout.custom_ider_info_patient,null);
    }



    @Override
    public View getInfoWindow(Marker marker) {

        TextView txt_PickupTitle = (myView.findViewById(R.id.txtPickupInfo));
        txt_PickupTitle.setText(marker.getTitle());

//        TextView txt_PickupSnippet = (myView.findViewById(R.id.txtPickupSnippet));
//        txt_PickupSnippet.setText(marker.getSnippet());
        return myView;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
}
