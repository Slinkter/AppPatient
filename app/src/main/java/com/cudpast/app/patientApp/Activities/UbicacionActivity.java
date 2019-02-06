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
import android.widget.ImageView;
import android.widget.Toast;

import com.cudpast.app.patientApp.Common.Common;
import com.cudpast.app.patientApp.Model.Rider;
import com.cudpast.app.patientApp.R;
import com.cudpast.app.patientApp.Remote.IFCMService;
import com.cudpast.app.patientApp.helper.CustomInfoWindow;
import com.cudpast.app.patientApp.helper.FCMResponse;
import com.cudpast.app.patientApp.helper.Notification;
import com.cudpast.app.patientApp.helper.Sender;
import com.cudpast.app.patientApp.helper.Token;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.google.maps.android.SphericalUtil;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UbicacionActivity extends FragmentActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private GoogleMap mMap;
    SupportMapFragment mapFragment;

    // -->LocationRequest
    private static int UPDATE_INTERVAL = 5000;
    private static int FATEST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;
    //<---

    private static final int MY_PERMISSION_REQUEST_CODE = 7192;
    private static final int PLAY_SERVICE_RES_REQUEST = 300193;

    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiCliente;
    Location mLastLocation;
    IFCMService mService;

    private DatabaseReference driversAvailable;
    private DatabaseReference ref;
    private GeoFire geoFire;
    private Marker mUserMarker;
    private Marker markerDestionation;
    private Button btnPickupRequest;
    private boolean isDriverFound = false;
    private String driverID = "";
    public int radius = 1;// 1km
    public int distance = 3;// 3km
    private static final int LIMIT = 3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ubicacion);
        //-->
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        //<--

        driversAvailable = FirebaseDatabase.getInstance().getReference(Common.tb_Business_Doctor);
        mService = Common.getIFCMService();

        btnPickupRequest = findViewById(R.id.btnPickupRequest);
        btnPickupRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isDriverFound) {
                    requestPickUpHere(FirebaseAuth.getInstance().getCurrentUser().getUid());
                } else {
                    sendRequestToDriver(driverID);
                }
            }
        });

        setUpLocation();

        updateFirebaseToken();

    }

    //.
    private void setUpLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_REQUEST_CODE);
        } else {
            if (checkPlayService()) {
                builGoogleApiClient();
                createLocationRequest();
                displayLocation();
            }
        }
    }

    //.
    private void updateFirebaseToken() {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tokens = db.getReference(Common.token_tbl);
        Token token = new Token(FirebaseInstanceId.getInstance().getToken());
        tokens.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(token);
    }
    //.
    private void createLocationRequest() {
        //Los objetos LocationRequest se utilizan para solicitar una calidad de servicio para actualizaciones de ubicación desde FusedLocationProviderApi.
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);

    }

    private void builGoogleApiClient() {
        //El principal punto de entrada para la integración de servicios de Google Play.
        mGoogleApiCliente = new GoogleApiClient.Builder(this).addConnectionCallbacks(this)
                                                                     .addOnConnectionFailedListener(this)
                                                                     .addApi(LocationServices.API)
                                                                     .build();
        mGoogleApiCliente.connect();
    }

    private boolean checkPlayService() {
        //los servicios de Google Play está disponible y actualizado en este dispositivo
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
            boolean isSuccess = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.my_map_style));
            if (!isSuccess) {
                Log.e("ERROR", "El map no carga");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }



        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        mMap = googleMap;

        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);

        mMap.setInfoWindowAdapter(new CustomInfoWindow(this));






    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiCliente.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        mGoogleApiCliente.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        displayLocation();

    }


    //Metodo Auxiliar
    //.Imagen SVG
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


            driversAvailable = FirebaseDatabase
                    .getInstance()
                    .getReference(Common.tb_Info_Doctor);

            driversAvailable.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Double latitud = mLastLocation.getLatitude();
                    Double longitude = mLastLocation.getLongitude();
                    loadAllAvailableDriver(new LatLng(latitud, longitude));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            final double latitude = mLastLocation.getLatitude();
            final double longitud = mLastLocation.getLongitude();


            loadAllAvailableDriver(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));


        } else {
            Log.d("ERROR", "Cannot get your location");
        }


    }

    // . loadAllAvailableDriver
    private void loadAllAvailableDriver(final LatLng location) {
        //1. Dibujar al usuario
        mMap.clear();
        mUserMarker = mMap
                        .addMarker(new MarkerOptions()
                        .position(location)
                        .icon(bitmapDescriptorFromVector(UbicacionActivity.this, R.drawable.ic_client))
                        .title(String.format("Usted")));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 16.0f));

        //2.Obtener a todos los doctores desde Firebase
        DatabaseReference driverLocation = FirebaseDatabase.getInstance().getReference(Common.tb_Business_Doctor);
        GeoFire gf = new GeoFire(driverLocation);
        GeoLocation pacienetGeo = new GeoLocation(location.latitude, location.longitude);
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
                        .getReference(Common.tb_Business_Doctor)
                        .child(key)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                // because rider and user model is same properties
                                // so we can user Rider model to get user here
                                Rider rider = dataSnapshot.getValue(Rider.class);
                                //add Driver to map

                                mMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(location.latitude, location.longitude))
                                        .flat(true)
                                        .title(rider.getName())
                                        .snippet(rider.getPhone())
                                        .icon(bitmapDescriptorFromVector(UbicacionActivity.this, R.drawable.ic_doctoraapp))
                                );
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
                    loadAllAvailableDriver(location);
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });

    }

    //.
    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            return;
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiCliente, mLocationRequest, this);
    }

    //.
    private void requestPickUpHere(String s) {

        DatabaseReference dbRequest = FirebaseDatabase.getInstance().getReference(Common.pickup_request_tbl);
        GeoFire mGeoFire = new GeoFire(dbRequest);

        mGeoFire.setLocation(s, new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()),
                new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {
                        // error
                    }
                });

        if (mUserMarker.isVisible())
            mUserMarker.remove();

        mUserMarker = mMap.addMarker(new MarkerOptions()
                .title("Paciente")
                .snippet("")
                .position(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
        );

        mUserMarker.showInfoWindow();
        btnPickupRequest.setText("Getting your Driver....");

        findDriver();

    }
    //.
    private void findDriver() {

        final DatabaseReference drivers = FirebaseDatabase.getInstance().getReference(Common.tb_Business_Doctor);
        GeoFire gfDrivers = new GeoFire(drivers);

        GeoQuery geoQuery = gfDrivers.queryAtLocation(new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()), radius);

        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {

                if (!isDriverFound) {
                    isDriverFound = true;
                    driverID = key;
                    btnPickupRequest.setText("Llamar al Doctor");
                    //   Toast.makeText(HomeActivity.this, "" + key, Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                //aumetar el km si no doctor hay disponble
                if (!isDriverFound && radius < LIMIT) {
                    radius++;
                    findDriver();
                } else {
                    Toast.makeText(UbicacionActivity.this, "No hay doctores en tu zona", Toast.LENGTH_SHORT).show();
                    btnPickupRequest.setText("BUSCAR DE NUEVO");
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });

    }

    //.
    private void sendRequestToDriver(String driverID) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(Common.token_tbl);
        tokens.orderByKey().equalTo(driverID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapShot : dataSnapshot.getChildren()) {
                            //convert to LatLng to json.
                            Token token = postSnapShot.getValue(Token.class); // Get toke object drom datbase with key
                            String json_lat_lng = new Gson().toJson(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
                            String riderToken = FirebaseInstanceId.getInstance().getToken();
                            Notification data = new Notification(riderToken, json_lat_lng);// envia la ubicacion lat y lng  hacia Doctor APP
                            Sender mensaje = new Sender(token.getToken(), data);
                            //enviar al appDOCTOR
                            mService.sendMessage(mensaje)
                                    .enqueue(new Callback<FCMResponse>() {
                                        @Override
                                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                                            Log.e("CustomerCallActivity", "response :--------->" + response);
                                            Log.e("CustomerCallActivity", "response.body().success:--------->" + response.body().success);
                                            if (response.body().success == 1) {
                                                Toast.makeText(UbicacionActivity.this, "Llamando al doctor !", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(UbicacionActivity.this, "Failed ! ", Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<FCMResponse> call, Throwable t) {
                                            Log.e("ERROR", t.getMessage());
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }


}
