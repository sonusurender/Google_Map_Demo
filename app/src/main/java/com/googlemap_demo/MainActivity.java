package com.googlemap_demo;

import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, GoogleMap.OnMyLocationButtonClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    private static GoogleApiClient mGoogleApiClient;
    private static final int ACCESS_FINE_LOCATION_INTENT_ID = 3;

    private AppCompatTextView normalMap, hybridMap, satelliteMap, terrainMap, noneMap;
    private AppCompatTextView locationLabel;

    /*  Required for Google Maps  */
    private GoogleMap googleMap;
    private LocationRequest mLocationRequest;
    private Marker mMarker;
    private Location mLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpToolbar();
        setUpGoogleMap();//Set Up Map
        findViews();
        setClickListeners();
    }

    //Set Up toolbar
    private void setUpToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    //find all views
    private void findViews() {
        normalMap = (AppCompatTextView) findViewById(R.id.normal_map);
        hybridMap = (AppCompatTextView) findViewById(R.id.hybrid_map);
        satelliteMap = (AppCompatTextView) findViewById(R.id.satellite_map);
        terrainMap = (AppCompatTextView) findViewById(R.id.terrain_map);
        noneMap = (AppCompatTextView) findViewById(R.id.none_map);
        locationLabel = (AppCompatTextView) findViewById(R.id.current_location_label);
    }

    //set click listeners over required textviews
    private void setClickListeners() {
        normalMap.setOnClickListener(this);
        hybridMap.setOnClickListener(this);
        satelliteMap.setOnClickListener(this);
        terrainMap.setOnClickListener(this);
        noneMap.setOnClickListener(this);
    }


    /* Set Up google map if Google Play Services Exists */
    private void setUpGoogleMap() {
        if (new GooglePlayServiceCheck().isGooglePlayInstalled(MainActivity.this)) {
            SupportMapFragment map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map));
            map.getMapAsync(this);//after getting map call async method, this method will call onMapReady(GoogleMap map) method
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        //When map ready
        googleMap = map;
        googleMap.getUiSettings().setZoomControlsEnabled(true);//enable zoom controls
        googleMap.getUiSettings().setAllGesturesEnabled(true);//enable gestures

        /*The below setting is used for showing navigation and map toolbar on marker click, set false if you don't want show map toolbar else leave it
        by default it is true  */
        googleMap.getUiSettings().setMapToolbarEnabled(true);

        //Enable current Location buttons
        enableCurrentLocationButton();

        /* Set Click listener for Marker and MyLocationButton Click event*/
        googleMap.setOnMarkerClickListener(this);
        googleMap.setOnMyLocationButtonClickListener(this);

        //By default set Normal Map
        setMapType(MapType.NORMAL);
    }

    private void enableCurrentLocationButton() {
        //before further proceed check if google map is null or not because this method is calling after giving permission
        if (googleMap != null) {
            if (ContextCompat.checkSelfPermission(MainActivity.this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED)
                requestLocationPermission();
            else {
                googleMap.getUiSettings().setMyLocationButtonEnabled(true);//enable Location button, if you don't want MyLocationButton set it false
                googleMap.setMyLocationEnabled(true);//enable blue dot
            }
        }
    }


    /* Initiate Google API Client  */
    private void initGoogleAPIClient() {
        Log.e(TAG, "Init google api client");
        //Without Google API Client Auto Location Dialog will not work
        mGoogleApiClient = new GoogleApiClient.Builder(MainActivity.this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    /* Check Location Permission for Marshmallow Devices */
    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(MainActivity.this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED)
                requestLocationPermission();
            else
                showSettingDialog();
        } else
            showSettingDialog();

    }

    /*  Show Popup to access Location Permission  */
    private void requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    ACCESS_FINE_LOCATION_INTENT_ID);

        } else {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    ACCESS_FINE_LOCATION_INTENT_ID);
        }
    }

    /* Show Location Access Dialog */
    private void showSettingDialog() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);//Setting priotity of Location request to high
        mLocationRequest.setInterval(30 * 1000);//after 30sec the location will update
        mLocationRequest.setFastestInterval(5 * 1000);//5 sec Time interval
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        builder.setAlwaysShow(true); //this is the key ingredient to show dialog always when GPS is off

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates state = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location
                        // requests here.
                        //getOriginLatLng();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        break;
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        //On Resume check if google api is not null and is connected so that location update should start again
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            Log.e(TAG, "onResume api client connect");
            startLocationUpdates();
        } else {
            //else if not connected or null initiate api client
            initGoogleAPIClient();
            Log.e(TAG, "onResume init api client");
        }

        //If location is not null set the last know location
        if (mLocation != null) {
            Log.e(TAG, "onResume update marker if location not null");
            updateCurrentMarker(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()));
        }

        //Check Location permission
        checkPermissions();
    }

    protected void onStop() {
        //On Stop disconnect Api Client
        if (mGoogleApiClient != null) {
            Log.e(TAG, "onStop disconnect api client");
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }


    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.normal_map:
                setMapType(MapType.NORMAL);
                break;
            case R.id.hybrid_map:
                setMapType(MapType.HYBRID);
                break;
            case R.id.satellite_map:
                setMapType(MapType.SATELLITE);
                break;
            case R.id.terrain_map:
                setMapType(MapType.TERRAIN);
                break;
            case R.id.none_map:
                setMapType(MapType.NONE);
                break;
        }
    }

    /*  Set Map Type on basis of MapType Enum and highlight selected TextView */
    private void setMapType(MapType mapType) {
        switch (mapType) {
            case NORMAL:
                googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                normalMap.setTextColor(getResources().getColor(R.color.teal));
                setDefaultColorBack(new AppCompatTextView[]{hybridMap, satelliteMap, terrainMap, noneMap});
                break;
            case HYBRID:
                googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                hybridMap.setTextColor(getResources().getColor(R.color.teal));
                setDefaultColorBack(new AppCompatTextView[]{normalMap, satelliteMap, terrainMap, noneMap});
                break;
            case SATELLITE:
                googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                satelliteMap.setTextColor(getResources().getColor(R.color.teal));
                setDefaultColorBack(new AppCompatTextView[]{hybridMap, normalMap, terrainMap, noneMap});
                break;
            case TERRAIN:
                googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                terrainMap.setTextColor(getResources().getColor(R.color.teal));
                setDefaultColorBack(new AppCompatTextView[]{hybridMap, satelliteMap, normalMap, noneMap});
                break;
            case NONE:
                googleMap.setMapType(GoogleMap.MAP_TYPE_NONE);
                noneMap.setTextColor(getResources().getColor(R.color.teal));
                setDefaultColorBack(new AppCompatTextView[]{hybridMap, satelliteMap, terrainMap, normalMap});
                break;
        }
    }

    /*  Turn Selected TextView color back to Black when MapType changes  */
    private void setDefaultColorBack(AppCompatTextView[] views) {
        for (AppCompatTextView v : views)
            v.setTextColor(getResources().getColor(android.R.color.black));
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        //On API Client Connection check if location permission is granted or not
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
            requestLocationPermission();
        else {
            //If granted then get last know location and update the location label if last know location is not null
            Log.e(TAG, "onConnected get last know location");
            mLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
            if (mLocation != null) {
                Log.e(TAG, "onConnected get last know location not null");
                locationLabel.setText(String.format(getResources().getString(R.string.lat_lng), mLocation.getLatitude(), mLocation.getLongitude()));
            } else
                Log.e(TAG, "onConnected get last know location null");
        }

        //Start Location Update on successful connection
        startLocationUpdates();
    }

    /*  Start Location Update */
    protected void startLocationUpdates() {
        Log.e(TAG, "startLocationUpdates");
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
            requestLocationPermission();
        else
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
    }

    //Handle API Client Connection Suspended
    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG, "onConnectionSuspended");
    }

    //Handle API Client Connection Failed
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed");
    }

    //Handle Marker click event
    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.e(TAG, "Marker Click: " + marker.getTitle() + "\nLocation : " + marker.getPosition());
        return false;
    }


    //On Location change method
    @Override
    public void onLocationChanged(Location location) {
        Log.e(TAG, "Location Changed : " + location.getLatitude() + ", " + location.getLongitude());

        mLocation = location;

        //Update Location label text
        locationLabel.setText(String.format(getResources().getString(R.string.lat_lng), location.getLatitude(), location.getLongitude()));

        //Update map marker on location change
        updateCurrentMarker(new LatLng(location.getLatitude(), location.getLongitude()));
    }


    //Update current marker of map
    private void updateCurrentMarker(LatLng latLng) {
        if (mMarker == null) {
            Log.e(TAG, "updateCurrentMarker marker null");
            if (googleMap != null) {
                Log.e(TAG, "updateCurrentMarker google map not null");

                //Set Marker position and default Icon
                mMarker = googleMap.addMarker(new MarkerOptions().position(latLng)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));

                //If you want to set Custom Icon then uncomment below line and comment above lines
                //  mMarker =  googleMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_user_location)));

                mMarker.setTitle("You are here");//set Marker title
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17.0f));//animate camera to 17 float value
            } else
                Log.e(TAG, "updateCurrentMarker google map null");

        } else {
            //If marker is already there just update position
            Log.e(TAG, "updateCurrentMarker marker not null");
            mMarker.setPosition(latLng);

            //if you want to animate camera to updated location, then uncomment below line
            // googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17.0f));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case RESULT_OK:
                        //If result is OK then start location Update
                        startLocationUpdates();
                        break;
                    case RESULT_CANCELED:
                        //else show toast
                        Toast.makeText(this, R.string.location_access_denied, Toast.LENGTH_SHORT).show();
                        break;
                }
                break;
        }
    }

    /* On Request permission method to check the permisison is granted or not for Marshmallow+ Devices  */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case ACCESS_FINE_LOCATION_INTENT_ID: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //If permission granted show location dialog if APIClient is not null
                    if (mGoogleApiClient == null) {
                        initGoogleAPIClient();
                        showSettingDialog();
                    } else
                        showSettingDialog();

                    //enable MyLocationButton
                    enableCurrentLocationButton();


                } else {
                    Toast.makeText(MainActivity.this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
                    // permission denied
                }
                return;
            }
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        //On Pause stop Location Update
        Log.e(TAG, "onPause stopLocationUpdates");
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected())
            stopLocationUpdates();
    }

    //Stop Location Update Method
    protected void stopLocationUpdates() {
        Log.e(TAG, "stopLocationUpdates");
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    //Handle MyLocationButton click event
    @Override
    public boolean onMyLocationButtonClick() {
        Log.e(TAG, "onMyLocationButtonClick");
        return false;
    }
}
