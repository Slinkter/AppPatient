package com.cudpast.app.patientApp.Business;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.cudpast.app.patientApp.Common.Common;
import com.cudpast.app.patientApp.Model.DoctorPerfil;
import com.cudpast.app.patientApp.R;
import com.cudpast.app.patientApp.Remote.IFCMService;
import com.cudpast.app.patientApp.Remote.IGoogleAPI;
import com.cudpast.app.patientApp.Soporte.DirectionJSONParser;
import com.cudpast.app.patientApp.helper.Data;
import com.cudpast.app.patientApp.helper.FCMResponse;
import com.cudpast.app.patientApp.helper.Notification;
import com.cudpast.app.patientApp.helper.Sender;
import com.cudpast.app.patientApp.helper.Token;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryDataEventListener;
import com.firebase.geofire.GeoQueryEventListener;
import com.firebase.geofire.LocationCallback;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.cudpast.app.patientApp.Common.Common.currentDoctor;
import static com.cudpast.app.patientApp.Common.Common.mLastLocation;

public class GoDoctor extends FragmentActivity implements
        OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks,
        LocationListener {

    private GoogleMap mMap;
    public static final String TAG = GoDoctor.class.getSimpleName();
    //Google Play Service -->
    private static final int PLAY_SERVICE_RES_REQUEST = 7001;
    private GoogleApiClient mGoogleApiCliente;
    private LocationRequest mLocationRequest;
    private static int UPDATE_INTERVAL = 5000;
    private static int FATEST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;
    //Google Play Service <--

    public double doctorLat, doctorLng;
    public String firebaseDoctorUID;
    public String requestApi = null;
    public Marker marketDoctorCurrent;
    public Marker pacienteMarker;
    private DatabaseReference referenceServiceDoctor, referenceTbDoctor;
    private int distance = 5;   // 3km

    Polyline direction;
    IGoogleAPI mService;
    IFCMService mFCMService;
    GeoFire geoFire;

    FusedLocationProviderClient ubicacion;
    public static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;

    Button btn_ruta_cancelar;
    Dialog myDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_go_doctor);
        //*************************************************
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapGoDoctor);
        mapFragment.getMapAsync(this);
        //*************************************************
        ubicacion = LocationServices.getFusedLocationProviderClient(this);
        btn_ruta_cancelar = findViewById(R.id.btn_ruta_cancelar);
        myDialog = new Dialog(this);

        btn_ruta_cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShowPopupCancelar();
            }
        });


        //.Recibir de la notificacion
        if (getIntent() != null) {
            firebaseDoctorUID = getIntent().getStringExtra("firebaseDoctorUID");
            Log.e("firebaseDoctorUID", "-------------->" + firebaseDoctorUID);
        }

        mService = Common.getGoogleService();
        mFCMService = Common.getIFCMService();

        referenceServiceDoctor = FirebaseDatabase.getInstance().getReference(Common.TB_SERVICIO_DOCTOR_PACIENTE);
        referenceTbDoctor = FirebaseDatabase.getInstance().getReference(Common.TB_INFO_DOCTOR);
        referenceServiceDoctor.keepSynced(true);
        geoFire = new GeoFire(referenceServiceDoctor);

        setUpLocation();

    }

    private void setUpLocation() {
        if (checkPlayService()) {
            builGoogleApiClient();
            createLocationRequest();
            displayLocation();
        }

    }

    private boolean checkPlayService() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICE_RES_REQUEST).show();
            } else {
                Toast.makeText(this, "this device is support ", Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;
    }

    private void builGoogleApiClient() {
        mGoogleApiCliente = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiCliente.connect();
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    //.DisplayLocation
    private void displayLocation() {
        Log.e(TAG, "==========================================================");
        Log.e(TAG, "                   displayLocation                     ");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            return;
        }
        //Obtener GPS desde googleApiCliente
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiCliente);

        if (mLastLocation != null) {

            Double latitud = mLastLocation.getLatitude();
            Double longitude = mLastLocation.getLongitude();
            final LatLng pacienteLocation = new LatLng(latitud, longitude);
            //.1
            referenceServiceDoctor
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            //cada evento de cambio vuelve a llamar a loadRoadDoctorOnMap
                            Log.e(TAG, "onDataChange --> pacienteLocation  " + pacienteLocation);
                            Log.e(TAG, "onDataChange --> DataSnapshot  " + dataSnapshot);
                            loadRoadDoctorOnMap(pacienteLocation);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.e(TAG, "ERROR : " + "Cannot get your location");
                        }
                    });
            //.2
            loadRoadDoctorOnMap(pacienteLocation);
        } else {
            Log.e("ERROR", "Cannot get your location");
        }

        Log.e(TAG, "                   fin - displayLocation                     ");
    }

    //.
    private void loadRoadDoctorOnMap(final LatLng pacienteLocation) {
        //todo : cuando el doctor esta en ruta , la hacer click se cambia el icono a doctora y se muestra
        // todo : crear por xml un layout para info del doctor y con click de cancelar

        Log.e(TAG, "==========================================================");
        Log.e(TAG, "                   loadRoadDoctorOnMap                     ");
        mMap.clear();
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pacienteLocation, 15.0f));
        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(pacienteLocation.latitude, pacienteLocation.longitude))
                .icon(BitmapDoctorApp(GoDoctor.this, R.drawable.ic_client)));
        //.
        GeoLocation pacienetGeo = new GeoLocation(pacienteLocation.latitude, pacienteLocation.longitude);
        Log.e(TAG, "loadRoadDoctorOnMap : pacienetGeo" + pacienetGeo);
        GeoQuery geoQuery = geoFire.queryAtLocation(pacienetGeo, distance);
        Log.e(TAG, "loadRoadDoctorOnMap : geoQuery" + geoQuery);
        geoQuery.removeAllListeners();
        geoQuery
                .addGeoQueryEventListener(new GeoQueryEventListener() {
                    @Override
                    public void onKeyEntered(String key, final GeoLocation location) {
                        Log.e(TAG, "onKeyEntered : key  " + key);
                        Log.e(TAG, "onKeyEntered : location  " + location);

                        referenceTbDoctor
                                .child(key)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        DoctorPerfil doctorPerfil = dataSnapshot.getValue(DoctorPerfil.class);
                                        currentDoctor = doctorPerfil;

//                                        mMap.addMarker(new MarkerOptions()
//                                                .position(new LatLng(location.latitude, location.longitude))
//                                                .flat(true)
//                                                .title(doctorPerfil.getFirstname() + " " + doctorPerfil.getLastname())
//                                                .snippet(doctorPerfil.getUid())
//                                                .icon(BitmapDoctorApp(GoDoctor.this, R.drawable.ic_doctoraapp))
//                                        );
                                        getDirection();

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });


                    }

                    @Override
                    public void onKeyExited(String key) {

                    }

                    @Override
                    public void onKeyMoved(String key, GeoLocation location) {

                    }

                    @Override
                    public void onGeoQueryReady() {

                    }

                    @Override
                    public void onGeoQueryError(DatabaseError error) {

                    }
                });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        try {

            boolean success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.my_map_style));
            if (!success) {
                Log.e("error", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("error", "Can't find style. Error: ", e);
        }

        mMap = googleMap;

        if (direction != null) {
            direction.remove();//remote old direction
        }
        if (marketDoctorCurrent != null) {
            marketDoctorCurrent.remove();
        }


    }

    //.metodos auxiliar para imagenes .svg
    private BitmapDescriptor BitmapDoctorApp(Context context, @DrawableRes int vectorDrawableResourceId) {
        Drawable background = ContextCompat.getDrawable(context, vectorDrawableResourceId);
        background.setBounds(0, 0, background.getIntrinsicWidth(), background.getIntrinsicHeight());
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorDrawableResourceId);
        vectorDrawable.setBounds(40, 20, vectorDrawable.getIntrinsicWidth() + 40, vectorDrawable.getIntrinsicHeight() + 20);
        Bitmap bitmap = Bitmap.createBitmap(background.getIntrinsicWidth(), background.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        background.draw(canvas);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    //.
    private void getDirection() {
        Log.e(TAG, "=============================================================");
        Log.e(TAG, "                     getDirection()                          ");


        geoFire.getLocation(firebaseDoctorUID, new LocationCallback() {
            @Override
            public void onLocationResult(String key, GeoLocation location) {
                if (location != null) {

                    //set marker to display on map
                    doctorLat = location.latitude;
                    doctorLng = location.longitude;
                    Log.e("doctorLat", " " + doctorLat);
                    Log.e("doctorLng", " " + doctorLng);

                    LatLng doctorlatlng = new LatLng(doctorLat, doctorLng);
                    MarkerOptions doctorMO = new MarkerOptions()
                            .position(doctorlatlng)
                            .title("Doctor")
                            .icon(BitmapDoctorApp(GoDoctor.this, R.drawable.ic_doctorapp));

                    marketDoctorCurrent = mMap.addMarker(doctorMO);

                    try {

                        requestApi =
                                "https://maps.googleapis.com/maps/api/directions/json?" +
                                        "mode=driving&" +
                                        "transit_routing_preference=less_driving&" +
                                        "origin=" + doctorLat + "," + doctorLng + "&" +
                                        "destination=" + mLastLocation.getLatitude() + "," + mLastLocation.getLongitude() + "&" +
                                        "key=" + "AIzaSyCZMjdhZ3FydT4lkXtHGKs-d6tZKylQXAA";


                        mService.getPath(requestApi)
                                .enqueue(new Callback<String>() {
                                    @Override
                                    public void onResponse(Call<String> call, Response<String> response) {
                                        try {

                                            if (direction != null) {
                                                direction.remove();//remote old direction

                                            }

                                            new getDireccionParserTask().execute(response.body().toString());


                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<String> call, Throwable t) {
                                        Toast.makeText(GoDoctor.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } catch (Exception e) {

                    }


                } else {
                    //When location is null
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //LogDatabase error
            }
        });


        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(doctorLat, doctorLng), 0.05f);
        geoQuery.addGeoQueryDataEventListener(new GeoQueryDataEventListener() {
            @Override
            public void onDataEntered(DataSnapshot dataSnapshot, GeoLocation location) {
                Log.e(TAG, " =================================");
                Log.e(TAG, " dataSnapshot" + dataSnapshot);
                Log.e(TAG, " onDataEntered" + location);
            }

            @Override
            public void onDataExited(DataSnapshot dataSnapshot) {
                Log.e(TAG, " =================================");
                Log.e(TAG, " onDataExited" + dataSnapshot);
            }

            @Override
            public void onDataMoved(DataSnapshot dataSnapshot, GeoLocation location) {
                Log.e(TAG, " =================================");
                Log.e(TAG, " dataSnapshot" + dataSnapshot);
                Log.e(TAG, " onDataMoved" + location);
            }

            @Override
            public void onDataChanged(DataSnapshot dataSnapshot, GeoLocation location) {
                Log.e(TAG, " =================================");
                Log.e(TAG, " dataSnapshot" + dataSnapshot);
                Log.e(TAG, " onDataChanged" + location);
            }

            @Override
            public void onGeoQueryReady() {
                Log.e(TAG, " onGeoQueryReady");

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                Log.e(TAG, " onGeoQueryError");
            }
        });


    }

    //.
    private class getDireccionParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        ProgressDialog mDialog = new ProgressDialog(GoDoctor.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog.setMessage("Actualizando a Ubicación...");
            mDialog.show();
        }

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
            JSONObject object;
            List<List<HashMap<String, String>>> router = null;
            try {
                object = new JSONObject(strings[0]);
                DirectionJSONParser parser = new DirectionJSONParser();
                router = parser.parse(object);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return router;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {

            mDialog.dismiss();
            ArrayList points = null;
            PolylineOptions polylineOptions = null;

            for (int i = 0; i < lists.size(); i++) {
                points = new ArrayList();
                polylineOptions = new PolylineOptions();
                List<HashMap<String, String>> path = lists.get(i);
                //-->
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);
                    points.add(position);
                }
                //<--
                polylineOptions.addAll(points);
                polylineOptions.width(5);
                polylineOptions.color(Color.MAGENTA);
                polylineOptions.geodesic(true);
            }

            direction = mMap.addPolyline(polylineOptions);


        }
    }

    //.
    private void startLocationUpdate() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiCliente, mLocationRequest, this);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdate();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiCliente.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        displayLocation();

    }


    private void cancelBooking(String IdToken) {
        Log.e(TAG, "======================================================");
        Log.e(TAG, "             cancelRequestDoctor                    ");
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(Common.token_tbl);
        Log.e(TAG, "TOKEN : -->" + tokens.toString());
        //Buscar a doctor por su id
        tokens
                .orderByKey()
                .equalTo(IdToken)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapShot : dataSnapshot.getChildren()) {
                            //convert to LatLng to json.
                            LatLng userGeo = new LatLng(15.0f, 15.0f);
                            Token tokenDoctor = postSnapShot.getValue(Token.class);
                            //Get token doctor and paciente
                            String dToken = tokenDoctor.getToken();
                            String pToken = FirebaseInstanceId.getInstance().getToken();
                            String json_lat_lng = new Gson().toJson(userGeo);
                            //Notification
                            Notification notification = new Notification("el usuario ha cancelado", "el usuario ha cancelado");// envia la ubicacion lat y lng  hacia Doctor APP
                            //Data
                            Data data = new Data(pToken, json_lat_lng);
                            //Sender (to, Notification)
                            Sender sender = new Sender(dToken, notification, data);
                            mFCMService
                                    .sendMessage(sender)
                                    .enqueue(new Callback<FCMResponse>() {
                                        @Override
                                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                                            if (response.body().success == 1) {
                                                Log.e(TAG, "onResponse: success");
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<FCMResponse> call, Throwable t) {
                                            Log.e(TAG, "onFailure : " + t.getMessage());
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e(TAG, " onCancelled" + databaseError.getMessage());
                    }
                });

        Log.e(TAG, "======================================================");
    }

    //.
    public void ShowPopupCancelar() {
        Button btn_accept_cancelar, btn_decline_cancelar;

        myDialog.setContentView(R.layout.pop_up_cancelar);
        btn_accept_cancelar = myDialog.findViewById(R.id.btn_accept_cancelar);
        btn_decline_cancelar = myDialog.findViewById(R.id.btn_decline_cancelar);

        btn_accept_cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "Confirma cancelar", Toast.LENGTH_SHORT).show();
                cancelBooking(Common.token_doctor);
                myDialog.dismiss();
                finish();
            }
        });

        btn_decline_cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myDialog.dismiss();
            }
        });

        myDialog.show();
    }


}
