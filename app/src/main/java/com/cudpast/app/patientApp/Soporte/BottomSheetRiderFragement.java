package com.cudpast.app.patientApp.Soporte;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.cudpast.app.patientApp.Common.Common;
import com.cudpast.app.patientApp.R;
import com.cudpast.app.patientApp.Remote.IGoogleAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BottomSheetRiderFragement extends BottomSheetDialogFragment {

    private String mLocation ,mDestination;

    boolean isTapOnMap;

    IGoogleAPI mService;

    TextView textCalculate,textLocation,textDestination;

    public static BottomSheetRiderFragement newInstance(String location,String destination ,    boolean isTapOnMap){
        BottomSheetRiderFragement f = new BottomSheetRiderFragement();
        Bundle args = new Bundle();
        args.putString("location",location);
        args.putString("destination",destination);
        args.putBoolean("isTapOnMap",isTapOnMap);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLocation = getArguments().getString("location");
        mDestination = getArguments().getString("destination");
        isTapOnMap = getArguments().getBoolean("isTapOnMap");

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view  = inflater.inflate(R.layout.botton_sheet_rider,container,false);
        textLocation    = (TextView) view.findViewById(R.id.txtLocation);
        textDestination = (TextView) view.findViewById(R.id.txtLocation1);
        textCalculate   = (TextView) view.findViewById(R.id.txtLocation2);



        mService = Common.getGoogleService();


        getPrice(mLocation,mDestination);

        if (!isTapOnMap){
            textLocation.setText(mLocation);
            textDestination.setText(mDestination);
//            textCalculate.setText("mmmm");
        }

        return view;

    }

    private void getPrice(String mLocation, String mDestination) {

        String requestUrl = null;

        try{
            requestUrl = "https://maps.googleapis.com/maps/api/directions/json?"+
                    "mode=driving&"+
                    "transit_routing_preference=less_driving&"+
                    "origin="+mLocation+"&"+
                    "destination="+mDestination+"&"+
                    "key=AIzaSyBacspf1KdZ4vAsvcHqoKQg_Pqe54pSivU";

            Log.e("Link",requestUrl);
            mService.getPath(requestUrl).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    // get OBject

                    try{
                        JSONObject jsonObject = new JSONObject(response.body().toString());
                        JSONArray routes = jsonObject.getJSONArray("routes");
                        Log.e("BottomSheetRider" , "" +routes);

                        JSONObject object = routes.getJSONObject(0);
                        JSONArray legs = object.getJSONArray("legs");

                        JSONObject legsObject = legs.getJSONObject(0);

                        //
                        JSONObject distance = legsObject.getJSONObject("distance");
                        String distance_text = distance.getString("text");

                        Double distance_value = Double.parseDouble(distance_text.replaceAll("[^0-9\\\\.]+",""));

                        JSONObject time = legsObject.getJSONObject("duration");
                        String time_text = time.getString("text");
                        Integer time_value = Integer.parseInt(time_text.replaceAll("\\D+" ,""));

                        String final_calcule= String.format("%s + %s = $%.2f",distance_text,time_text, Common.getPrice(distance_value,time_value));

                        textCalculate.setText(final_calcule);

                        if (isTapOnMap){
                            String start_address = legsObject.getString("start_address");
                            String end_address = legsObject.getString("end_address");

                            textLocation.setText(start_address);
                            textDestination.setText(end_address);

                        }

                    }catch (JSONException e ){
                        e.printStackTrace();
                    }




                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Log.e("ERROR",t.getMessage());
                }
            });

        }catch (Exception e){
            e.printStackTrace();
        }

    }




}
