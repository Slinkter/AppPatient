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
        this.myView = LayoutInflater.from(context).inflate(R.layout.custom_ider_info_window,null);
    }

    @Override
    public View getInfoWindow(Marker marker) {

        TextView txtPickupTitle = (myView.findViewById(R.id.txtPickupInfo));
        txtPickupTitle.setText(marker.getTitle());

        TextView txtPickupSnippet = (myView.findViewById(R.id.txtPickupSnippet));
        txtPickupSnippet.setText(marker.getSnippet());
        //


        return myView;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
}
