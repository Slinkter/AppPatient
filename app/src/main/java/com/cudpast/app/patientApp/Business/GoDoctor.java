package com.cudpast.app.patientApp.Business;

import android.Manifest;
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
import android.widget.Toast;

import com.cudpast.app.patientApp.Common.Common;
import com.cudpast.app.patientApp.Model.DoctorPerfil;
import com.cudpast.app.patientApp.R;
import com.cudpast.app.patientApp.Remote.IFCMService;
import com.cudpast.app.patientApp.Remote.IGoogleAPI;
import com.cudpast.app.patientApp.Soporte.DirectionJSONParser;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.cudpast.app.patientApp.Common.Common.mLastLocation;

public class GoDoctor extends FragmentActivity implements OnMapReadyCallback,
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

    private Marker marketDoctorCurrent;
    public Marker pacienteMarker;

    private int distance = 5;   // 3km

    Polyline direction;
    IGoogleAPI mService;
    IFCMService mFCMService;
    GeoFire geoFire;

    private DatabaseReference DatabaseReference_doctorAvailable;


    String requestApi = null;


    private FusedLocationProviderClient ubicacion;
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_go_doctor);
        //
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapGoDoctor);
        mapFragment.getMapAsync(this);

        ubicacion = LocationServices.getFusedLocationProviderClient(this);


        //.Recibir de la notificacion
        if (getIntent() != null) {
            firebaseDoctorUID = getIntent().getStringExtra("firebaseDoctorUID");
            Log.e("firebaseDoctorUID", "-------------->" + firebaseDoctorUID);
        }

        mService = Common.getGoogleService();
        mFCMService = Common.getIFCMService();
        setUpLocation();

        DatabaseReference_doctorAvailable = FirebaseDatabase.getInstance().getReference(Common.TB_SERVICIO_DOCTOR_PACIENTE);
        DatabaseReference_doctorAvailable.keepSynced(true);
        geoFire = new GeoFire(DatabaseReference_doctorAvailable);


    }

    private void setUpLocation() {
        if (checkPlayService()) {
            builGoogleApiClient();
            createLocationRequest();
            displayLocation();
        }

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
    private void displayLocation2() {

//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
//                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
//        ) {
//            return;
//        }
        if (
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(GoDoctor.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_READ_CONTACTS);


            return;
        }

        ubicacion
                .getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {

                        if (location != null) {
                            final double latitude = location.getLatitude();
                            final double longitud = location.getLongitude();


                                if (pacienteMarker != null && direction != null) {
                                    pacienteMarker.remove();
                                    direction.remove();
                                }
                                if (direction != null) {
                                    direction.remove();//remote old direction
                                }

                                LatLng pacientelatlng = new LatLng(latitude, longitud);
                                MarkerOptions pacienteMO = new MarkerOptions()
                                        .position(pacientelatlng)
                                        .title("USTED")
                                        .icon(BitmapDoctorApp(GoDoctor.this, R.drawable.ic_client));

                                pacienteMarker = mMap.addMarker(pacienteMO);

                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pacientelatlng, 17.0f));


                                Log.e(TAG, "displayLocation() :  Common.mLastLocation :" + longitud + " , " + latitude);


                            getDirection(latitude, longitud);




                        }
                    }
                });







    }


    //.DisplayLocation
    private void displayLocation() {
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

            DatabaseReference_doctorAvailable.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.e(TAG, "319 : displayLocation() --> DatabaseReference_doctorAvailable --> pacienteLocation  " + pacienteLocation);
                    //cada evento de cambio vuelve a llamar a loadDoctorAvailableOnMap
                    loadDoctorAvailableOnMap(pacienteLocation);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.d(TAG, "ERROR : " + "Cannot get your location");
                }
            });
            loadDoctorAvailableOnMap(pacienteLocation);
        } else {
            Log.d("ERROR", "Cannot get your location");
        }
    }



    // . loadAllAvailableDriver - loadDoctorAvailableOnMap
    private void loadDoctorAvailableOnMap(final LatLng pacienteLocation) {
        //.
        mMap.clear();
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pacienteLocation, 14.99f));

        //.Obtener a todos los doctores desde Firebase

        GeoFire gf = new GeoFire(DatabaseReference_doctorAvailable);
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
                                //  Log.e(TAG, "onKeyEntered " + dataSnapshot.toString());

                                DoctorPerfil rider = dataSnapshot.getValue(DoctorPerfil.class);
                                //add Driver to map

                                mMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(location.latitude, location.longitude))
                                        .flat(true)
                                        .title(rider.getFirstname() + " " + rider.getLastname())
                                        .snippet(rider.getUid())
                                        .icon(bitmapDescriptorFromVector(GoDoctor.this, R.drawable.ic_doctoraapp))
                                );

                                //  mMap.setInfoWindowAdapter(new CustomInfoWindow(getApplicationContext()));
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






    private void getDirection(double latitude, double longitud) {
        Log.e(TAG, "=============================================================");
        Log.e(TAG, "                     getDirection()                          ");

        final LatLng currentPosition = new LatLng(latitude, longitud);
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

                        requestApi = "https://maps.googleapis.com/maps/api/directions/json?" +
                                "mode=driving&" +
                                "transit_routing_preference=less_driving&" +
                                "origin=" + doctorLat + "," + doctorLng + "&" +
                                "destination=" + currentPosition.latitude + "," + currentPosition.longitude + "&" +
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
