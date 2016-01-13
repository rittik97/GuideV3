package com.example.rittik.guide;

import android.Manifest;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
//import android.location.LocationListener;
import android.os.Build;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
//import android.widget.Toolbar;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener, OnMapReadyCallback,GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,LocationListener {
    private DrawerLayout mDrawer;
    private Toolbar toolbar;
    private NavigationView nvDrawer;
    private FragmentManager fragmentManager;
    private GoogleMap map;
    private MapFragment mapfragment = null;
    TextToSpeech tts;
    private GoogleApiClient mGoogleApiClient;
    private static final String TAG = "Logger is Activity";
    private Location mLastLocation;
    private LocationRequest mLocationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //mGoogleApiClient.connect(); In OnStart()
        buildGoogleApiClient();



        // Set a Toolbar to replace the ActionBar.
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //spoken.setText("hi");
        // Find our drawer view


        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        nvDrawer = (NavigationView) findViewById(R.id.nvView);
        // Setup drawer view
        setupDrawerContent(nvDrawer);
        Class fragmentClass;
        fragmentClass = Start.class;
        Fragment fragment = null;
        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {

        }
        tts = new TextToSpeech(this, this);

        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().add(R.id.flContent, fragment).commit();

    }
    @Override
    protected void onStart() {
        super.onStart();



        //{tv2.setText("here");}
        //tv23.setText("Started");
        try {
            mGoogleApiClient.connect();
            //if(mGoogleApiClient.isConnected()) {
            //createLocationRequest();
            //startLocationUpdates();

            //}else
            //  Toast.makeText(this,"Point", Toast.LENGTH_SHORT).show();

        }catch (Exception e)
        {
            //alertexception(e);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawer.openDrawer(GravityCompat.START);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Make sure this is the method with just `Bundle` as the signature
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }


    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
    }

    public void selectDrawerItem(MenuItem menuItem) {
        // Create a new fragment and specify the planet to show based on
        // position
        Fragment fragment = null;

        int flag = 0;
        Class fragmentClass = null;
        switch (menuItem.getItemId()) {
            case R.id.nav_first_fragment:
                fragmentClass = Start.class;
                break;
            case R.id.nav_second_fragment:
                fragmentClass = user_details.class;
                break;
            case R.id.nav_third_fragment:
                mapfragment = MapFragment.newInstance();
                //fragmentClass = navigate.class;
                flag = 1;
                break;
            default:
                fragmentClass = user_details.class;
        }

        try {
            if (flag == 0) {
                fragment = (Fragment) fragmentClass.newInstance();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Insert the fragment by replacing any existing fragment


        if (flag == 0) {
            fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();
        } else {
            FragmentTransaction fragmentTransaction =
                    getFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.flContent, mapfragment);
            fragmentTransaction.commit();
            mapfragment.getMapAsync(this);

        }
        // Highlight the selected item, update the title, and close the drawer
        menuItem.setChecked(true);
        setTitle(menuItem.getTitle());
        mDrawer.closeDrawers();
    }

    public void speak_Queue_Add(String s) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts.speak(s, TextToSpeech.QUEUE_ADD, null, null);
        } else {
            HashMap<String, String> param = new HashMap<String, String>();
            tts.speak(s, TextToSpeech.QUEUE_ADD, param);
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {

            int result = tts.setLanguage(Locale.US);
        }
    }
    private void setupmap(){
        map.setMyLocationEnabled(true);
        map.setBuildingsEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(true);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = mapfragment.getMap();
        setupmap();
        map.addMarker(new MarkerOptions()
                .position(new LatLng(0, 0))
                .title("Marker"));
    }

    @Override
    public void onConnected(Bundle bundle) {
        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);
        speak_Queue_Add( String.valueOf(permissionCheck));
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            //Execute location service call if user has explicitly granted ACCESS_FINE_LOCATION..
            createLocationRequest();
            startLocationUpdates();

        }
        if(mLastLocation != null) {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
            speak_Queue_Add(mLastLocation.toString());
        }


    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    protected synchronized void buildGoogleApiClient() {
        Log.i(TAG, "Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(this, "Connection to Google API client Fail", Toast.LENGTH_SHORT).show();
    }
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(2000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    }

    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, MainActivity.this)
                ;
    }

    public void makecameragotocurrentlocation(Location loc){
        map.clear();
        Location here = loc;double lat=here.getLatitude();double lon = here.getLongitude();
        LatLng lng=new LatLng(lat,lon);
        map.addMarker(new MarkerOptions().position(lng));
        map.moveCamera(CameraUpdateFactory.newLatLng(lng));
        map.animateCamera(CameraUpdateFactory.zoomTo(17));
    }


    @Override
    public void onLocationChanged(Location location) {
        Location mCurrentLocation = location;

        Toast.makeText(this, mCurrentLocation.toString(), Toast.LENGTH_SHORT).show();
        makecameragotocurrentlocation(mCurrentLocation);
        speak_Queue_Add(mCurrentLocation.toString());
    }


}



