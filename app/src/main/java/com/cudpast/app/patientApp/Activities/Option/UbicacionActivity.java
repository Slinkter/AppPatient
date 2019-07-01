package com.cudpast.app.patientApp.Activities.Option;


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
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class UbicacionActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = UbicacionActivity.class.getSimpleName();
    private static final int MY_PERMISSION_REQUEST_CODE = 1;
    private FusedLocationProviderClient fusedLocationClient;
    LatLng pacienteLocation;

    private DatabaseReference DbRef_TB_AVAILABLE_DOCTOR;
    private DatabaseReference DbRef_TB_INFO_DOCTOR;

    private static final int DEFAULT_ZOOM = 15;
    private final LatLng mDefaultLocation = new LatLng(-12.141177, -77.026342);

    private int distance = 5;   // 3km
    private static final int LIMIT = 10;
    private boolean mPermissionDenied = false;

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ubicacion);
        //
        getSupportActionBar().setTitle("Mapas de Doctores");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapUbicacion);
        mapFragment.getMapAsync(this);
        //
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        //.
        DbRef_TB_AVAILABLE_DOCTOR = FirebaseDatabase.getInstance().getReference(Common.TB_AVAILABLE_DOCTOR);
        DbRef_TB_INFO_DOCTOR = FirebaseDatabase.getInstance().getReference(Common.TB_INFO_DOCTOR);
        //.
        DbRef_TB_AVAILABLE_DOCTOR.keepSynced(true);
        DbRef_TB_AVAILABLE_DOCTOR.orderByKey();
        //.
        DbRef_TB_INFO_DOCTOR.keepSynced(true);
        DbRef_TB_INFO_DOCTOR.orderByKey();
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
                            mMap.setMyLocationEnabled(true);
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
                                        mMap.setMyLocationEnabled(true);
                                        mMap.getUiSettings().setAllGesturesEnabled(true);
                                        Common.mLastLocation = location;
                                        LatLng p = new LatLng(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude());
                                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(p, 16.05f));
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
                            LatLng indexUbicacion = new LatLng(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(indexUbicacion, DEFAULT_ZOOM));
                        }
                    }
                });


        //
        getDeviceLocation();

        mMap
                .setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
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

                        BSRFDoctor showDoctorInfo = BSRFDoctor.newInstance(title, doctorUID, true, doctorLatitude, doctorLongitud, pacienteLatitude, pacienteLongitud);
                        showDoctorInfo.show(getSupportFragmentManager(), showDoctorInfo.getTag());
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
        Log.e(TAG, " getDeviceLocation()");
        try {

            fusedLocationClient
                    .getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {

                                Common.mLastLocation = location;
                                LatLng indexUbication = new LatLng(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude());
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(indexUbication, DEFAULT_ZOOM));
                                pacienteLocation = indexUbication;
                                DbRef_TB_AVAILABLE_DOCTOR
                                        .addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                mMap.setMyLocationEnabled(true);
                                                Log.e(TAG, " onDataChange() : " + dataSnapshot);
                                                loadDoctorAvailableOnMap(pacienteLocation);
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                                Log.d(TAG, "databaseError" + databaseError.toString());
                                            }
                                        });
                                loadDoctorAvailableOnMap(pacienteLocation);
                            } else {
                                Log.e(TAG, "Current location is null. Using defaults.");
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "addOnFailureListener :" + e.getMessage());
                        }
                    });

        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    // .
    private void loadDoctorAvailableOnMap(final LatLng pacienteLocation) {
        //.
        mMap.clear();
        GeoFire gf = new GeoFire(DbRef_TB_AVAILABLE_DOCTOR);
        //.
        GeoLocation pacienetGeo = new GeoLocation(pacienteLocation.latitude, pacienteLocation.longitude);
        GeoQuery geoQuery = gf.queryAtLocation(pacienetGeo, distance);
        geoQuery.removeAllListeners();
        geoQuery
                .addGeoQueryEventListener(new GeoQueryEventListener() {
                    @Override
                    public void onKeyEntered(String key, final GeoLocation location) {
                        //use key to get email from table users
                        //table users is table when driver register account and update infomation
                        // just open your driver to check this table name
                        DbRef_TB_INFO_DOCTOR
                                .child(key)
                                .addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        // because doctor_info and user model is same properties
                                        // so we can user Rider model to get user here
                                        Log.e(TAG, "==========================================");
                                        Log.e(TAG, "        onDataChange        ");
                                        DoctorPerfil doctor_info = dataSnapshot.getValue(DoctorPerfil.class);
                                        if (doctor_info != null) {
                                            Log.e(TAG, " doctor_info.getFirstname()  " + doctor_info.getFirstname());
                                            Log.e(TAG, " doctor_info.getLastname()  " + doctor_info.getLastname());
                                            Log.e(TAG, " doctor_info.getUid()  " + doctor_info.getUid());
                                            Log.e(TAG, " doctor_info.getDni()  " + doctor_info.getDni());
                                            mMap
                                                    .addMarker(new MarkerOptions()
                                                            .position(new LatLng(location.latitude, location.longitude))
                                                            .flat(true)
                                                            .title(doctor_info.getFirstname() + " " + doctor_info.getLastname())
                                                            .snippet(doctor_info.getUid())
                                                            .icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.ic_doctorapp))
                                                    );
                                        }


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
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth() + 0, vectorDrawable.getIntrinsicHeight() + 0);
        Bitmap bitmap = Bitmap.createBitmap(background.getIntrinsicWidth(), background.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        background.draw(canvas);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }


}
