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
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.Manifest;
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


public class UbicacionActivity extends AppCompatActivity implements
        OnMyLocationButtonClickListener,
        OnMyLocationClickListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String TAG = UbicacionActivity.class.getSimpleName();
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    LatLng pacienteLocation;
    private CameraPosition mCameraPosition;
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

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        DatabaseReference_TB_AVAILABLE_DOCTOR = FirebaseDatabase.getInstance().getReference(Common.TB_AVAILABLE_DOCTOR);
        DatabaseReference_TB_INFO_DOCTOR = FirebaseDatabase.getInstance().getReference(Common.TB_INFO_DOCTOR);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;

        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        enableMyLocation();


        try {
            boolean isSuccess = mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.my_map_style));
            if (!isSuccess) {
                Log.e("ERROR", "El estilo de google map no carga");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                Double doctorLatitude = marker.getPosition().latitude;
                Double doctorLongitud = marker.getPosition().longitude;


                Double pacienteLatitude = Common.mLastLocation.getLatitude();
                Double pacienteLongitud = Common.mLastLocation.getLongitude();

                String title = marker.getTitle();
                String doctorUID = marker.getSnippet();//pasar el uid

                Log.e(TAG, "title " + title);
                Log.e(TAG, "doctorUID " + doctorUID);

                Log.e(TAG, "doctorLatitude " + doctorLatitude);
                Log.e(TAG, "doctorLongitud " + doctorLongitud);

                Log.e(TAG, "pacienteLatitude " + pacienteLatitude);
                Log.e(TAG, "pacienteLongitud " + pacienteLongitud);


                BSRFDoctor mBottomSheet = BSRFDoctor.newInstance(title, doctorUID, true, doctorLatitude, doctorLongitud, pacienteLatitude, pacienteLongitud);

                mBottomSheet.show(getSupportFragmentManager(), mBottomSheet.getTag());
//                marker.showInfoWindow();
                return true;
            }
        });

        getDeviceLocation();

    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
        LatLng pacienteGPS = new LatLng(location.getLatitude(),location.getLongitude());
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pacienteGPS, DEFAULT_ZOOM));

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

    // My code

    private void getDeviceLocation() {
        /**
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {

                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult
                        .addOnCompleteListener(this, new OnCompleteListener<Location>() {
                            @Override
                            public void onComplete(@NonNull Task<Location> task) {
                                if (task.isSuccessful()) {


                                    Common.mLastLocation = task.getResult();
                                    Log.e(TAG, "fusedLocationClient : Common.mLastLocation.getLatitude() " + Common.mLastLocation.getLatitude());
                                    Log.e(TAG, "fusedLocationClient : Common.mLastLocation.getLongitude()" + Common.mLastLocation.getLongitude());
                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng( Common.mLastLocation .getLatitude(),  Common.mLastLocation .getLongitude()), DEFAULT_ZOOM));
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
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pacienteLocation, 14.99f));
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

                                mMap.addMarker(new MarkerOptions()
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
