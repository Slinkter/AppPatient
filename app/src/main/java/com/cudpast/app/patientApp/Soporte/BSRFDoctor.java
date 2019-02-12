package com.cudpast.app.patientApp.Soporte;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.cudpast.app.patientApp.Common.Common;
import com.cudpast.app.patientApp.R;
import com.cudpast.app.patientApp.Remote.IGoogleAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BSRFDoctor extends BottomSheetDialogFragment {

    private String mTitle ,mSnippet;
    boolean isTapOnMap;
    TextView title,snippet;
    Button btn_yes ,btn_no;

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

        View view  = inflater.inflate(R.layout.botton_sheet_rider,container,false);
        title = (TextView) view.findViewById(R.id.txt_doctor_title);
        snippet = (TextView) view.findViewById(R.id.txt_doctor_snippet);

        btn_yes = view.findViewById(R.id.btn_yes);
        btn_no = view.findViewById(R.id.btn_no);

        if (!isTapOnMap){

        }else {

            title.setText(mTitle);
            snippet.setText(mSnippet);

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
