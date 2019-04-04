package com.cudpast.app.patientApp.Activities;


import com.cudpast.app.patientApp.Adapter.BSRFDoctor;
import com.cudpast.app.patientApp.Common.Common;
import com.cudpast.app.patientApp.Model.DoctorPerfil;
import com.cudpast.app.patientApp.R;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class UbicacionActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = UbicacionActivity.class.getSimpleName();
    private static final int MY_PERMISSION_REQUEST_CODE = 1;
    private FusedLocationProviderClient fusedLocationClient;
    LatLng pacienteLocation;

    private DatabaseReference DatabaseReference_TB_AVAILABLE_DOCTOR;
    private DatabaseReference DatabaseReference_TB_INFO_DOCTOR;

    private static final int DEFAULT_ZOOM = 15;
    private final LatLng mDefaultLocation = new LatLng(-12.071368, -76.962154);

    private int distance = 5;   // 3km
    private static final int LIMIT = 10;


    private boolean mPermissionDenied = false;

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ubicacion);
        getSupportActionBar().setTitle("Mapas de Doctores");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        SupportMapFragment mapFragment =(SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapUbicacion);
        mapFragment.getMapAsync(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        DatabaseReference_TB_AVAILABLE_DOCTOR = FirebaseDatabase.getInstance().getReference(Common.TB_AVAILABLE_DOCTOR);
        DatabaseReference_TB_INFO_DOCTOR = FirebaseDatabase.getInstance().getReference(Common.TB_INFO_DOCTOR);

        DatabaseReference_TB_AVAILABLE_DOCTOR.keepSynced(true);
        DatabaseReference_TB_AVAILABLE_DOCTOR.orderByKey();

        DatabaseReference_TB_INFO_DOCTOR.keepSynced(true);
        DatabaseReference_TB_INFO_DOCTOR.orderByKey();

        setupLocation();
    }

    private void setupLocation() {


    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e(TAG, "permission was granted");
                    mMap.setMyLocationEnabled(true);
                    try {
                        boolean isSuccess = mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.my_map_style));
                        if (!isSuccess) {
                            Log.e("ERROR", "El estilo de google map no carga");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    fusedLocationClient
                            .getLastLocation()
                            .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                                @Override
                                public void onSuccess(Location location) {
                                    if (location != null) {
                                        mMap.getUiSettings().setAllGesturesEnabled(true);
                                        Common.mLastLocation = location;
                                        mMap
                                                .moveCamera(CameraUpdateFactory
                                                        .newLatLngZoom(new LatLng(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude()), 16));

                                    }
                                }
                            });



                    getDeviceLocation();
                } else {
                    Log.e(TAG, "permission denied");
                    break;

                }
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        //.Permisos
        if (ContextCompat
                .checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat
                        .checkSelfPermission(this,
                                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSION_REQUEST_CODE);

            return;
        }
        mMap.setMyLocationEnabled(true);
        try {
            boolean isSuccess = mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.my_map_style));
            if (!isSuccess) {
                Log.e("ERROR", "El estilo de google map no carga");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        fusedLocationClient
                .getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            mMap.getUiSettings().setAllGesturesEnabled(true);
                            Common.mLastLocation = location;
                            mMap
                                    .moveCamera(CameraUpdateFactory
                                            .newLatLngZoom(new LatLng(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude()), 16));

                        }
                    }
                });




        //
        getDeviceLocation();

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                Double doctorLatitude = marker.getPosition().latitude;
                Double doctorLongitud = marker.getPosition().longitude;

                Double pacienteLatitude = Common.mLastLocation.getLatitude();
                Double pacienteLongitud = Common.mLastLocation.getLongitude();

                String title = marker.getTitle();
                String doctorUID = marker.getSnippet();

                Log.e(TAG, "title " + title);
                Log.e(TAG, "doctorUID " + doctorUID);

                Log.e(TAG, "doctorLatitude " + doctorLatitude);
                Log.e(TAG, "doctorLongitud " + doctorLongitud);

                Log.e(TAG, "pacienteLatitude " + pacienteLatitude);
                Log.e(TAG, "pacienteLongitud " + pacienteLongitud);

                BSRFDoctor mBottomSheet = BSRFDoctor.newInstance(title, doctorUID, true, doctorLatitude, doctorLongitud, pacienteLatitude, pacienteLongitud);
                mBottomSheet.show(getSupportFragmentManager(), mBottomSheet.getTag());
                return true;
            }
        });

    }


    private void getDeviceLocation() {

        if (ContextCompat
                .checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat
                        .checkSelfPermission(this,
                                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSION_REQUEST_CODE);

            return;
        }

        try {

            Task<Location> locationResult = fusedLocationClient.getLastLocation();
            locationResult
                    .addOnCompleteListener(this, new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            if (task.isSuccessful()) {

                                Common.mLastLocation = task.getResult();
                                Log.e(TAG, "fusedLocationClient : Common.mLastLocation.getLatitude() " + Common.mLastLocation.getLatitude());
                                Log.e(TAG, "fusedLocationClient : Common.mLastLocation.getLongitude()" + Common.mLastLocation.getLongitude());
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude()), DEFAULT_ZOOM));
                                pacienteLocation = new LatLng(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude());
                                DatabaseReference_TB_AVAILABLE_DOCTOR.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        Log.e(TAG, "319 : displayLocation() --> DatabaseReference_TB_AVAILABLE_DOCTOR --> pacienteLocation  " + pacienteLocation);

                                        loadDoctorAvailableOnMap(pacienteLocation);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        Log.d(TAG, "ERROR : " + "Cannot get your location");
                                    }
                                });
                                loadDoctorAvailableOnMap(pacienteLocation);
                            } else {
                                Log.d(TAG, "Current location is null. Using defaults.");
                                Log.e(TAG, "Exception: %s", task.getException());
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                            }
                        }
                    });

        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    // . loadAllAvailableDriver - loadDoctorAvailableOnMap
    private void loadDoctorAvailableOnMap(final LatLng pacienteLocation) {
        //.
        mMap.clear();
//        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pacienteLocation, 14.99f));
        //.Obtener a todos los doctores desde Firebase
        GeoFire gf = new GeoFire(DatabaseReference_TB_AVAILABLE_DOCTOR);
        //.
        GeoLocation pacienetGeo = new GeoLocation(pacienteLocation.latitude, pacienteLocation.longitude);
        GeoQuery geoQuery = gf.queryAtLocation(pacienetGeo, distance);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, final GeoLocation location) {
                //use key to get email from table users
                //table users is table when driver register account and update infomation
                // just open your driver to check this table name
                FirebaseDatabase
                        .getInstance()
                        .getReference(Common.TB_INFO_DOCTOR)
                        .child(key)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                // because rider and user model is same properties
                                // so we can user Rider model to get user here
                                Log.e(TAG, "==========================================");
                                Log.e(TAG, "        onDataChange        ");
                                Log.e(TAG, "onKeyEntered " + dataSnapshot.toString());

                                DoctorPerfil rider = dataSnapshot.getValue(DoctorPerfil.class);
                                Log.e(TAG, " rider.getDni()  " + rider.getFirstname());
                                Log.e(TAG, " rider.getDni()  " + rider.getLastname());
                                Log.e(TAG, " rider.getDni()  " + rider.getUid());
                                Log.e(TAG, " rider.getDni()  " + rider.getDni());
                                //add Driver to map

                                mMap
                                        .addMarker(new MarkerOptions()
                                                .position(new LatLng(location.latitude, location.longitude))
                                                .flat(true)
                                                .title(rider.getFirstname() + " " + rider.getLastname())
                                                .snippet(rider.getUid())
                                                .icon(bitmapDescriptorFromVector(UbicacionActivity.this, R.drawable.ic_doctorapp))
                                        );

                                mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                                    @Override
                                    public View getInfoWindow(Marker marker) {
                                        return null;
                                    }

                                    @Override
                                    public View getInfoContents(Marker marker) {

                                        View view = getLayoutInflater().inflate(R.layout.custom_ider_info_patient, null);

                                        TextView txt_PickupTitle = (view.findViewById(R.id.txtPickupInfo));
                                        txt_PickupTitle.setText(marker.getTitle());

                                        TextView txt_PickupSnippet = (view.findViewById(R.id.txtPickupSnippet));
                                        txt_PickupSnippet.setText(marker.getSnippet());

                                        return view;
                                    }
                                });
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
                if (distance <= LIMIT) {
                    distance++;
                    loadDoctorAvailableOnMap(pacienteLocation);
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });

    }

    //.
    private BitmapDescriptor bitmapDescriptorFromVector(Context context, @DrawableRes int vectorDrawableResourceId) {
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

}
