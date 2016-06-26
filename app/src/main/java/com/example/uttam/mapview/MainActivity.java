package com.example.uttam.mapview;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import java.util.List;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,GoogleMap.OnMarkerDragListener {


    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String key = "saveLocation";
    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private Button bSave,bSearch;
    private EditText eText;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;


    private GoogleMap mMap;
    private LatLng MyLocation=null;
    static final int ZOOM_LEVEL = 15;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bSave = (Button)findViewById(R.id.bSave);
        bSearch = (Button)findViewById(R.id.bSearch);
        eText = (EditText)findViewById(R.id.searchView1);
        if(checkPlayServices())
            buildGoogleApiClient();

        bSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = sharedPreferences.edit();
                String location = MyLocation.latitude+" "+MyLocation.longitude;
                editor.putString(key,location);
                editor.commit();
                onBackPressed();
            }
        });

        bSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String g = eText.getText().toString();

                Geocoder geocoder = new Geocoder(getBaseContext());
                List<Address> addresses = null;

                try {
                    // Getting a maximum of 3 Address that matches the input
                    // text
                    addresses = geocoder.getFromLocationName(g, 3);
                    if (addresses != null && !addresses.equals(""))
                        search(addresses);
                    else
                        Toast.makeText(getApplicationContext(),"Sorry Could Not Find Location",Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),"Sorry Could Not Find Location",Toast.LENGTH_LONG).show();
                }

            }
        });
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

        if (mLastLocation != null) {
            double latitude = mLastLocation.getLatitude();
            double longitude = mLastLocation.getLongitude();
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String location = sharedPreferences.getString(key,null);
          //  Log.v("latitude and longitude",location);
            if(location==null)
                MyLocation = new LatLng(latitude,longitude);
            else
                MyLocation = new LatLng(Double.parseDouble(location.split(" ",2)[0]),Double.parseDouble(location.split(" ",2)[1]));

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

    protected void search(List<Address> addresses) {

        Address address = (Address) addresses.get(0);
        MyLocation = new LatLng(address.getLatitude(), address.getLongitude());

      /*  addressText = String.format(
                "%s, %s",
                address.getMaxAddressLineIndex() > 0 ? address
                        .getAddressLine(0) : "", address.getCountryName());*/

        MarkerOptions markerOptions = new MarkerOptions();

        markerOptions.position(MyLocation);
       // markerOptions.title(addressText);
        markerOptions.draggable(true);
        mMap.clear();
        mMap.addMarker(markerOptions);
        mMap.setOnMarkerDragListener(this);

        mMap.moveCamera(CameraUpdateFactory.newLatLng(MyLocation));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(ZOOM_LEVEL));
       // locationTv.setText("Latitude:" + address.getLatitude() + ", Longitude:"
       //         + address.getLongitude());


    }

    @Override
    public void onMarkerDragStart(Marker marker) {

        Log.v("My location", String.valueOf(MyLocation));
    }

    @Override
    public void onMarkerDrag(Marker marker) {

        Log.v("My location", String.valueOf(MyLocation));
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        MyLocation = marker.getPosition();
        Log.v("My location", String.valueOf(MyLocation));
        Toast.makeText(getApplicationContext(),"Marker Dragged",Toast.LENGTH_LONG).show();
    }

}


