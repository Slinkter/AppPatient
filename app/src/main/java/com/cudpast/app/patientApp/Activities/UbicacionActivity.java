package com.cudpast.app.patientApp.Activities;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
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
import android.widget.TextView;
import android.widget.Toast;

import com.cudpast.app.patientApp.Common.Common;
import com.cudpast.app.patientApp.Model.DoctorPerfil;
import com.cudpast.app.patientApp.R;
import com.cudpast.app.patientApp.Remote.IFCMService;
import com.cudpast.app.patientApp.Adapter.BSRFDoctor;
import com.cudpast.app.patientApp.helper.Token;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
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
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import org.w3c.dom.Text;

public class UbicacionActivity extends FragmentActivity implements
        OnMapReadyCallback {
    private static final String TAG = UbicacionActivity.class.getSimpleName();
    private GoogleMap mMap;
    SupportMapFragment mapFragment;

    // -->LocationRequest
    private static int UPDATE_INTERVAL = 5000;
    private static int FATEST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;
    //<---

    private static final int MY_PERMISSION_REQUEST_CODE = 7192;
    private static final int PLAY_SERVICE_RES_REQUEST = 300193;


    IFCMService mService;

    private DatabaseReference DatabaseReference_TB_AVAILABLE_DOCTOR;
    private DatabaseReference DatabaseReference_TB_INFO_DOCTOR;
    private Marker mUserMarker;
    private Button btnPickupRequest;
    private boolean isDriverFound = false;
    private String driverID = "";
    private int radius = 3;     // 1km
    private int distance = 5;   // 3km
    private static final int LIMIT = 10;


    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient mFusedLocationProviderClient;
    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;
    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location mLastKnownLocation;
    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";
    private CameraPosition mCameraPosition;
    LatLng pacienteLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }
        //Construct a FusedLocationProviderClient.
        setContentView(R.layout.activity_ubicacion);
        //.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Construct a FusedLocationProviderClient.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mService = Common.getIFCMService();
        DatabaseReference_TB_AVAILABLE_DOCTOR = FirebaseDatabase.getInstance().getReference(Common.TB_AVAILABLE_DOCTOR);
        DatabaseReference_TB_INFO_DOCTOR = FirebaseDatabase.getInstance().getReference(Common.TB_INFO_DOCTOR);

        //.
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            super.onSaveInstanceState(outState);
        }
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


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        try {
            boolean isSuccess = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.my_map_style));
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

        // Prompt the user for permission.
        getLocationPermission();

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();


    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }


    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getDeviceLocation() {
        /**
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult
                        .addOnCompleteListener(this, new OnCompleteListener<Location>() {
                            @Override
                            public void onComplete(@NonNull Task<Location> task) {
                                if (task.isSuccessful()) {
                                    // Set the map's camera position to the current location of the device.
                                    mLastKnownLocation = task.getResult();
                                    Common.mLastLocation = mLastKnownLocation;
                                    Log.e(TAG, "fusedLocationClient : Common.mLastLocation.getLatitude() " + Common.mLastLocation.getLatitude());
                                    Log.e(TAG, "fusedLocationClient : Common.mLastLocation.getLongitude()" + Common.mLastLocation.getLongitude());
                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
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
            }
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


}
