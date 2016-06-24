package com.example.uttam.mapview;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,GoogleMap.OnMarkerDragListener, GoogleMap.OnMapClickListener {

    private final int MY_PERMISSIONS_LOCATION = 10;
    private static final String TAG = MainActivity.class.getSimpleName();

    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;


    private GoogleMap mMap;
    private LatLng MyLocation;
    static final int ZOOM_LEVEL = 15;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(checkPlayServices())
            buildGoogleApiClient();

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
        Log.v("My location", String.valueOf(MyLocation));
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        checkPlayServices();
        Log.v("My location", String.valueOf(MyLocation));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(MyLocation)
                .draggable(true));


        mMap.moveCamera(CameraUpdateFactory.newLatLng(MyLocation));
        mMap.setOnMarkerDragListener(this);
        mMap.setOnMapClickListener(this);
        Log.v("zoom level", String.valueOf(mMap.getMaxZoomLevel()));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(ZOOM_LEVEL));
    }


    private void getUserLocation() throws IOException {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi
                .getLastLocation(mGoogleApiClient);
        Log.v("latitude and longitude","hey");
        if (mLastLocation != null) {
            double latitude = mLastLocation.getLatitude();
            double longitude = mLastLocation.getLongitude();
            MyLocation = new LatLng(latitude,longitude);
            Log.v("latitude and longitude", latitude + " " + longitude);
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        if(MyLocation!=null)
            mapFragment.getMapAsync(this);

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        try {
            getUserLocation();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }


    private boolean checkPlayServices()
    {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();
                finish();
            }
            return false;
        }
        return true;
    }
    protected synchronized void buildGoogleApiClient()
    {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();

    }

    @Override
    public void onMarkerDragStart(Marker marker) {

        Log.v("My location", String.valueOf(MyLocation)+" s");
    }

    @Override
    public void onMarkerDrag(Marker marker) {

        Log.v("My location", String.valueOf(MyLocation)+" cv");
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        MyLocation = marker.getPosition();
        Log.v("My location", String.valueOf(MyLocation)+" xx");
        Toast.makeText(getApplicationContext(),"Marker Dragged",Toast.LENGTH_LONG).show();
    }
    @Override
    public void onMapClick(LatLng arg0) {
        // TODO Auto-generated method stub
        mMap.animateCamera(CameraUpdateFactory.newLatLng(arg0));
    }
}


