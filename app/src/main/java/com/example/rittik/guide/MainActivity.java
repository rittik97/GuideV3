package com.example.rittik.guide;

import android.Manifest;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
//import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
//import android.widget.Toolbar;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener, OnMapReadyCallback,GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,LocationListener {
    private DrawerLayout mDrawer;
    private Toolbar toolbar;
    private NavigationView nvDrawer;
    private FragmentManager fragmentManager; //For Fragments
    private Animator animator = new Animator();
    private GoogleMap map;
    private MapFragment mapfragment = null;
    TextToSpeech tts;
    private GoogleApiClient mGoogleApiClient;
    private static final String TAG = "Logger is Activity";
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private static int requestfinelocation = 0;
    private Location mCurrentLocation;
    private boolean isnavigating;
    ArrayList instructions = null;
    //ArrayList <String> turns = null;
    private int flagforcoordinates=0;
    private int flagforinstructions=0;
    private int flagforendturns=0;
    private int flagforturns=0;
    private JSONObject objr;
    private boolean walkingnavigating;
    private boolean animatenavigating;
    private turns turnarray;
    private final Handler mHandler = new Handler();
    private List<Marker> markers = new ArrayList<Marker>();
    private Marker selectedMarker;

    ArrayList <String> turns = null;


    private Marker marker;
    private int totalturns;
    private int currentturn=0;
    private int nexttturn=0;
    private ParserTask parserTask;
    private getinstructions gi;
    private endpoints endloc;
    private boolean lastpoint=false;



    ArrayList<LatLng> points;
    ArrayList <LatLng> endlocations=null;
    private LatLng destinationloc;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //mGoogleApiClient.connect(); In OnStart()
        String [] options={"Demo","Guide"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Mode")
                .setItems(options, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) animatenavigating = true;
                        else walkingnavigating = true;
                        System.out.println("hereherehere" + walkingnavigating);
                    }
                });
        builder.create();
        builder.show();

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

        } catch (Exception e) {
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

    public void removeenavigation(MapFragment mapfragment) {
        FragmentTransaction fragmentTransaction =
                getFragmentManager().beginTransaction();
        fragmentTransaction.remove(mapfragment);
        fragmentTransaction.commit();

        isnavigating = false;
    }

    public void selectDrawerItem(MenuItem menuItem) {
        // Create a new fragment and specify the planet to show based on
        // position
        Fragment fragment = null;
        if (isnavigating) {
            removeenavigation(mapfragment);
        }

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
                isnavigating = true;
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
            fragmentTransaction.replace(R.id.flContent, mapfragment);
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

    private void setupmap() {
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

    public boolean haspermission(String permission) {
        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), permission);
        speak_Queue_Add(String.valueOf(permissionCheck));
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            return true;

        } else {
            return false;

        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);
        speak_Queue_Add(String.valueOf(permissionCheck));
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            //Execute location service call if user has explicitly granted ACCESS_FINE_LOCATION..
            createLocationRequest();
            startLocationUpdates();

        } else {
            requestpermission();

        }
        if (mLastLocation != null) {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
            // speak_Queue_Add(mLastLocation.toString());
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

    public void makecameragotocurrentlocation(Location loc) {
        map.clear();
        Location here = loc;
        double lat = here.getLatitude();
        double lon = here.getLongitude();
        LatLng lng = new LatLng(lat, lon);
        map.addMarker(new MarkerOptions().position(lng));
        map.moveCamera(CameraUpdateFactory.newLatLng(lng));
        map.animateCamera(CameraUpdateFactory.zoomTo(17));
    }


    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;

        //Toast.makeText(this, mCurrentLocation.toString(), Toast.LENGTH_SHORT).show();
        //makecameragotocurrentlocation(mCurrentLocation);
        //speak_Queue_Add(mCurrentLocation.toString());
    }



    public void requestsmspermission() {
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.SEND_SMS},
                    requestfinelocation);

        }

    }

    public void requestpermission() {
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            //if (ActivityCompat.shouldShowRequestPermissionRationale(thisActivity,
            //      Manifest.permission.READ_CONTACTS)) {

            // Show an expanation to the user *asynchronously* -- don't block
            // this thread waiting for the user's response! After the user
            // sees the explanation, try again to request the permission.

            //} else {

            // No explanation needed, we can request the permission.

            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    requestfinelocation);

            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant. The callback method gets the
            // result of the request.
            //          }
        }

    }

    public void sendsms(Location Loc) {

        SmsManager smsManager = SmsManager.getDefault();
        //String here=reversegeocoder(Loc);
        String here = "Here";
        smsManager.sendTextMessage("+14047898139", null, "I'm at " + String.valueOf(Loc.getLatitude()) + " " + String.valueOf(Loc.getLongitude()), null, null);
        smsManager.sendTextMessage("+7187107474", null, "I'm at " + String.valueOf(Loc.getLatitude()) + " " + String.valueOf(Loc.getLongitude()), null, null);
        //smsManager.sendTextMessage("+7187107474",null,"work",null,null);

    }

    public void listens() {
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.ENGLISH);
        i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say Something!");

        try {
            startActivityForResult(i, 100);
        } catch (Exception e) {
            //Toast.makeText()
        }

    }

    public void onActivityResult(int request_code, int result_code, Intent i) {
        super.onActivityResult(request_code, result_code, i);

        switch (request_code) {
            case 100: {
                if (result_code == -1 && i != null) {
                    ArrayList<String> result = i.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    //spoken.setText(result.get(0));
                    speak_Queue_Add(result.get(0));
                    Toast.makeText(this, result.get(0).toString(), Toast.LENGTH_SHORT).show();
                    understand(result.get(0));

                }
            }
            break;
        }

    }

    public void understand(String s) {
        if (s.indexOf("text") > 0) {
            if (haspermission(Manifest.permission.SEND_SMS)) {
                sendsms(mCurrentLocation);
            } else {
                requestsmspermission();
            }
        }

    }

    public void alertexception(Exception e) {
        StringWriter errors = new StringWriter();
        e.printStackTrace(new PrintWriter(errors));

        // Show the stack trace on Logcat.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Add the buttons
        builder.setMessage(errors.toString());
        // Set other dialog properties

        // Create the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    public String reversegeocoder(Location loc) {
        Geocoder geocoder = new Geocoder(this, Locale.ENGLISH);

        try {

            //Place your latitude and longitude
            List<Address> addresses = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);

            if (addresses != null) {

                Address fetchedAddress = addresses.get(0);
                StringBuilder strAddress = new StringBuilder();

                for (int i = 0; i < fetchedAddress.getMaxAddressLineIndex(); i++) {
                    strAddress.append(fetchedAddress.getAddressLine(i)).append("\n");
                }

                Toast.makeText(this, strAddress.toString(), Toast.LENGTH_SHORT).show();
                return strAddress.toString();

            } else
                Toast.makeText(this, "No address", Toast.LENGTH_SHORT).show();


        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Could not get address..!", Toast.LENGTH_LONG).show();
        }
        return "Can't Find Address";
    }

    public LatLng geocoder(String destination) {
        Geocoder geocoder = new Geocoder(this, Locale.ENGLISH);

        try {


            List<Address> addresses = geocoder.getFromLocationName(destination, 10
                    , mLastLocation.getLatitude() - 0.015, mLastLocation.getLongitude() - 0.009, mLastLocation.getLatitude() + 0.015, mLastLocation.getLongitude() + 0.009
            );
            if (!addresses.isEmpty() && addresses != null) {
                /* Displaying Returned Addresses
                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                builder.setMessage(addresses.toString());

                AlertDialog dialog = builder.create();
                dialog.show();
                */
                int distances[] = new int[addresses.size()];
                for (int i = 0; i < addresses.size(); i++) {
                    float result[] = new float[3];
                    Location.distanceBetween(mLastLocation.getLatitude(), mLastLocation.getLongitude()
                            , addresses.get(i).getLatitude(), addresses.get(i).getLongitude(), result
                    );
                    distances[i] = (int) result[0];
                }
                //talkthis("I found " + String.valueOf(addresses.size()) + "options");
                //if(distances[0]null)
                int temp1 = 0;
                for (int i = 0; i < distances.length; i++) {
                    if (distances[i] < temp1) {
                        temp1 = i;
                    }


                }
                //talkthis("The closest one is" + distances[temp1] + "meters away, I'll take you there");


                if (addresses != null) {

                    Address address = addresses.get(temp1);

                    double lat = address.getLatitude();
                    double lng = address.getLongitude();

                    Toast.makeText(this, Double.toString(lat) + "  " + Double.toString(lng), Toast.LENGTH_SHORT).show();
                    LatLng returnlatlng = new LatLng(lat, lng);
                    return returnlatlng;
                }

            } else {
                Toast.makeText(this, "No address", Toast.LENGTH_SHORT).show();
                speak_Queue_Add("Couldn't find what you were looking for");
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Could not get address..!", Toast.LENGTH_LONG).show();
        }
        return new LatLng(0, 0);


    }

    public void startclick(View v) {
        //map.clear();


        LatLng fromPosition=null;// = new LatLng(40.798506, -73.964577);
        LatLng toPosition=null;// = new LatLng(40.798204, -73.952304);//40.8079639, -73.9630146);
        try {
            EditText destination = (EditText) findViewById(R.id.textView);
            String dest = destination.getText().toString();
            //if(dest==null || dest ==""){AlertDialog }else{}


            //talkthis(dest);

            toPosition = geocoder(dest);
            if (toPosition.latitude == 0 && toPosition.longitude == 0) return;
            Double la = mLastLocation.getLatitude();
            Double ln = mLastLocation.getLongitude();
            fromPosition = new LatLng(la, ln);

        } catch (Exception e) {
            Log.e(TAG, "ERRRRRRRRRRRRRRR", e);
            alertexception(e);
        }
        //geocoder(dest);
        // String urltext = "http://maps.googleapis.com/maps/api/directions/json?origin=40.798506,-73.964577&destination=(40.8079639,-73.9630146&sensor=false&units=metric&mode=walking";
        String urltext = "http://maps.googleapis.com/maps/api/directions/json?"
                + "origin=" + fromPosition.latitude + "," + fromPosition.longitude
                + "&destination=" + toPosition.latitude + "," + toPosition.longitude
                + "&sensor=false&units=metric&mode=" + "walking";
        // Replace JSON to XML to make above string return xml

        System.out.println(urltext);

        try {


            DownloadTask downloadTask = new DownloadTask();

            // Start downloading json data from Google Directions API
            downloadTask.execute(urltext);

        }
            catch(Exception e){
                Log.e(TAG, "ERRRRRRRRRRRRRRR", e);
                alertexception(e);
            }
            //while (parserTask.getStatus().equals(AsyncTask.Status.FINISHED) ==false && gi.getStatus().equals(AsyncTask.Status.FINISHED)==false)

        }
    private class DownloadTask extends AsyncTask<String, Void, String> { //////GETTING JSON

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url1) {

            // For storing data from web service

            String data = "";
            URL url;

            HttpURLConnection urlConnection;
            try{
                // Fetching the data from web service
                url = new URL(url1[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                //Toast.makeText(getActivity(),"Start",Toast.LENGTH_SHORT).show();
                urlConnection.connect();

                InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                BufferedReader streamReader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                StringBuilder responseStrBuilder = new StringBuilder();

                String inputStr;
                while ((inputStr = streamReader.readLine()) != null)
                    responseStrBuilder.append(inputStr);
                objr=new JSONObject(responseStrBuilder.toString());

                data=objr.toString();
            }catch(Exception e){
                Log.d("Background Task",e.toString());

            }




            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            parserTask = new ParserTask();
            gi=new getinstructions();
            endloc=new endpoints();
            turnarray=new turns();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
            gi.execute(result);
            endloc.execute(result);
            turnarray.execute(result);


        }
    }
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> >{//////Getting Polyline Coordinates

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try{
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            }catch(Exception e){
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            flagforcoordinates=1;

            points=null;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes
            for(int i=0;i<result.size();i++){
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for(int j=0;j<path.size();j++){
                    HashMap<String,String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(4);
                lineOptions.color(Color.rgb(14, 210, 232));
            }

            // Drawing polyline in the Google Map for the i-th route
            //Toast.makeText(getApplicationContext(),points.toString(), Toast.LENGTH_SHORT).show();
            map.addPolyline(lineOptions);
            addDefaultLocations();
            //talkthis("finish one");
            if(flagforcoordinates==1 && flagforendturns==1 && flagforinstructions==1 && flagforendturns==1) {

                if(animatenavigating)animator.startAnimation(true);
                //talkthis(instructions.get(currentturn).toString());
                //currentturn++;


            }

        }
    }


    private class getinstructions extends AsyncTask<String, Integer, ArrayList >{


        // Parsing the data in non-ui thread
        @Override
        protected ArrayList doInBackground(String... jsonData) {

            JSONObject jObject;


            try{
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                instructions = parser.parsehtml(jObject);
            }catch(Exception e) {
                e.printStackTrace();
            }
            return instructions;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(ArrayList result) {
            flagforinstructions=1;

            // Toast.makeText(getApplicationContext(),String.valueOf(instructions.size()), Toast.LENGTH_SHORT).show();
            //Toast.makeText(getApplicationContext(),instructions.toString(), Toast.LENGTH_LONG).show();

            if(flagforcoordinates==1 && flagforendturns==1 && flagforinstructions==1 && flagforendturns==1) {
                if(animatenavigating)animator.startAnimation(true);
            }

        }
    }
    private class endpoints extends AsyncTask<String, Integer, ArrayList >{

        // Parsing the data in non-ui thread
        @Override
        protected ArrayList doInBackground(String... jsonData) {

            JSONObject jObject;


            try{

                // Starts parsing data
                DirectionsJSONParser parser = new DirectionsJSONParser();
                jObject = new JSONObject(jsonData[0]);
                endlocations = parser.parseendpoints(jObject);
                destinationloc=parser.destination;
            }catch(Exception e){
                e.printStackTrace();
            }
            return endlocations;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(ArrayList result) {
            flagforendturns=1;

            totalturns=endlocations.size();
            //Toast.makeText(getApplicationContext(),destinationloc.toString(), Toast.LENGTH_SHORT).show();

            // navigating=true;
            map.addMarker(new MarkerOptions()
                    .position(destinationloc)
                    .title("Destination"));

            if(flagforcoordinates==1 && flagforendturns==1 && flagforinstructions==1 && flagforendturns==1) {
                if(animatenavigating)animator.startAnimation(true);

            }


        }
    }
    private class turns extends AsyncTask<String, Integer, ArrayList >{



        @Override
        protected ArrayList doInBackground(String... params) {
            JSONObject jObject;


            try{
                jObject = new JSONObject(params[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();


                turns = parser.parsemaneuver(jObject);
            }catch(Exception e) {
                alertexception(e);
            }
            return turns;
        }

        @Override
        protected void onPostExecute(ArrayList arrayList) {

            flagforendturns=1;
            //Toast.makeText(getApplicationContext(),turns.toString(), Toast.LENGTH_SHORT).show();
            if(flagforcoordinates==1 && flagforendturns==1 && flagforinstructions==1 && flagforendturns==1) {
                if(animatenavigating)animator.startAnimation(true);

            }
        }
    }

    public class Animator implements Runnable {

        private static final int ANIMATE_SPEEED = 1500;
        private static final int ANIMATE_SPEEED_TURN = 1000;
        private static final int BEARING_OFFSET = 20;

        private final Interpolator interpolator = new LinearInterpolator();

        int currentIndex = 0;

        float tilt = 90;
        float zoom = 15.5f;
        boolean upward=true;

        long start = SystemClock.uptimeMillis();

        LatLng endLatLng = null;
        LatLng beginLatLng = null;

        boolean showPolyline = false;

        private Marker trackingMarker;

        public void reset() {
            resetMarkers();
            start = SystemClock.uptimeMillis();
            currentIndex = 0;
            endLatLng = getEndLatLng();
            beginLatLng = getBeginLatLng();

        }

        public void stop() { ///Stop Navigating navigation animation
            trackingMarker.remove();
            mHandler.removeCallbacks(animator);
            //Intent i=new Intent(getApplicationContext(), SplashActivity.class);
            //startActivity(i);

        }

        public void initialize(boolean showPolyLine) {
            reset();
            this.showPolyline = showPolyLine;

            highLightMarker(0);

            if (showPolyLine) {
                polyLine = initializePolyLine();
            }

            // We first need to put the camera in the correct position for the first run (we need 2 markers for this).....
            LatLng markerPos = markers.get(0).getPosition();
            LatLng secondPos = markers.get(1).getPosition();

            setupCameraPositionForMovement(markerPos, secondPos);

        }

        private void setupCameraPositionForMovement(LatLng markerPos,
                                                    LatLng secondPos) {

            float bearing = bearingBetweenLatLngs(markerPos,secondPos);

            trackingMarker = map.addMarker(new MarkerOptions().position(markerPos)
                    .title("title")
                    .snippet("snippet"));

            CameraPosition cameraPosition =
                    new CameraPosition.Builder()
                            .target(markerPos)
                            .bearing(bearing + BEARING_OFFSET)
                            .tilt(90)
                            .zoom(map.getCameraPosition().zoom >=16 ? map.getCameraPosition().zoom : 16)
                            .build();

            map.animateCamera(
                    CameraUpdateFactory.newCameraPosition(cameraPosition),
                    ANIMATE_SPEEED_TURN,
                    new GoogleMap.CancelableCallback() {

                        @Override
                        public void onFinish() {
                            System.out.println("finished camera");
                            animator.reset();
                            Handler handler = new Handler();
                            handler.post(animator);
                        }

                        @Override
                        public void onCancel() {
                            System.out.println("cancelling camera");
                        }
                    }
            );
        }

        private Polyline polyLine;
        private PolylineOptions rectOptions = new PolylineOptions();


        private Polyline initializePolyLine() {
            //polyLinePoints = new ArrayList<LatLng>();
            rectOptions.add(markers.get(0).getPosition());
            return map.addPolyline(rectOptions);
        }

        /**
         * Add the marker to the polyline.
         */
        private void updatePolyLine(LatLng latLng) {
            List<LatLng> points = polyLine.getPoints();
            points.add(latLng);
            polyLine.setPoints(points);
        }


        public void stopAnimation() {
            animator.stop();
        }

        public void startAnimation(boolean showPolyLine) {
            if (markers.size()>2) {
                animator.initialize(showPolyLine);
            }
        }


        @Override
        public void run() {

            long elapsed = SystemClock.uptimeMillis() - start;
            double t = interpolator.getInterpolation((float)elapsed/ANIMATE_SPEEED);

//			LatLng endLatLng = getEndLatLng();
//			LatLng beginLatLng = getBeginLatLng();

            double lat = t * endLatLng.latitude + (1-t) * beginLatLng.latitude;
            double lng = t * endLatLng.longitude + (1-t) * beginLatLng.longitude;
            LatLng newPosition = new LatLng(lat, lng);

            trackingMarker.setPosition(newPosition);

            if (showPolyline) {
                updatePolyLine(newPosition);
            }

            // It's not possible to move the marker + center it through a cameraposition update while another camerapostioning was already happening.
            //navigateToPoint(newPosition,tilt,bearing,currentZoom,false);
            //navigateToPoint(newPosition,false);

            if (t< 1) {
                mHandler.postDelayed(this, 16);
            } else {

                System.out.println("Move to next marker.... current = " + currentIndex + " and size = " + markers.size());
                // imagine 5 elements -  0|1|2|3|4 currentindex must be smaller than 4
                if (currentIndex<markers.size()-2) {

                    currentIndex++;

                    endLatLng = getEndLatLng();
                    beginLatLng = getBeginLatLng();


                    start = SystemClock.uptimeMillis();

                    LatLng begin = getBeginLatLng();
                    LatLng end = getEndLatLng();

                    float bearingL = bearingBetweenLatLngs(begin, end);

                    highLightMarker(currentIndex);

                    CameraPosition cameraPosition =
                            new CameraPosition.Builder()
                                    .target(end) // changed this...
                                    .bearing(bearingL  + BEARING_OFFSET)
                                    .tilt(tilt)
                                    .zoom(map.getCameraPosition().zoom)
                                    .build();


                    map.animateCamera(
                            CameraUpdateFactory.newCameraPosition(cameraPosition),
                            ANIMATE_SPEEED_TURN,
                            null
                    );

                    start = SystemClock.uptimeMillis();
                    mHandler.postDelayed(animator, 16);

                } else {
                    currentIndex++;
                    highLightMarker(currentIndex);
                    stopAnimation();
                }

            }
        }




        private LatLng getEndLatLng() {

            return markers.get(currentIndex+1).getPosition();
        }

        private LatLng getBeginLatLng() {
            if(animatenavigating) {
                float results[] = new float[3];
                double mylat;
                double mylong;
                Location loc = convertLatLngToLocation(points.get(currentIndex));

                Location.distanceBetween(loc.getLatitude(), loc.getLongitude()
                        , endlocations.get(currentturn).latitude
                        , endlocations.get(currentturn).longitude,
                        results
                );

                if (results[0] < 15) {
                    //talkthis("turn");
                    speak_Queue_Add(instructions.get(currentturn).toString());
                    /*
                    if(nexttturn<turns.size() && currentturn!=0) {
                        int type=turns.get(nexttturn).toString().indexOf("right");
                        try {
                            if(type!=-1){mwBoard.getModule(Haptic.class).startMotor(75.f, (short) 1500);}
                            else {mwBoard.getModule(Haptic.class).startMotor(50.f, (short) 500);}
                        } catch (UnsupportedModuleException e) {
                            e.printStackTrace();
                        }
                        nexttturn++;
                    }
                    */

                    currentturn++;
                }
                if (currentturn == totalturns) {//You have reached destination
                    animatenavigating= false;
                    lastpoint=true;
                }


            } else if(lastpoint){
                float results[] = new float[3];
                double mylat;
                double mylong;
                Location loc = convertLatLngToLocation(points.get(currentIndex));

                Location.distanceBetween(loc.getLatitude(), loc.getLongitude()
                        , destinationloc.latitude
                        , destinationloc.longitude,
                        results
                );

                if (results[0] < 5) {

                    speak_Queue_Add("You have arrived");
                    lastpoint=false;
                }

            }
            return markers.get(currentIndex).getPosition();
        }

        private void adjustCameraPosition() {
            //System.out.println("tilt = " + tilt);
            //System.out.println("upward = " + upward);
            //System.out.println("zoom = " + zoom);
            if (upward) {

                if (tilt<90) {
                    tilt ++;
                    zoom-=0.01f;
                } else {
                    upward=false;
                }

            } else {
                if (tilt>0) {
                    tilt --;
                    zoom+=0.01f;
                } else {
                    upward=true;
                }
            }
        }
    };

    /**
     * Allows us to navigate to a certain point.
     */
    public void navigateToPoint(LatLng latLng,float tilt, float bearing, float zoom,boolean animate) {
        CameraPosition position =
                new CameraPosition.Builder().target(latLng)
                        .zoom(zoom)
                        .bearing(bearing)
                        .tilt(tilt)
                        .build();

        changeCameraPosition(position, animate);

    }

    public void navigateToPoint(LatLng latLng, boolean animate) {
        CameraPosition position = new CameraPosition.Builder().target(latLng).build();
        changeCameraPosition(position, animate);
    }

    private void changeCameraPosition(CameraPosition cameraPosition, boolean animate) {
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);

        if (animate) {
            map.animateCamera(cameraUpdate);
        } else {
            map.moveCamera(cameraUpdate);
        }

    }

    private Location convertLatLngToLocation(LatLng latLng) {
        Location loc = new Location("someLoc");
        loc.setLatitude(latLng.latitude);
        loc.setLongitude(latLng.longitude);
        return loc;
    }

    private float bearingBetweenLatLngs(LatLng begin,LatLng end) {
        Location beginL= convertLatLngToLocation(begin);
        Location endL= convertLatLngToLocation(end);

        return beginL.bearingTo(endL);
    }

    public void toggleStyle() {
        if (GoogleMap.MAP_TYPE_NORMAL == map.getMapType()) {
            map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        } else {
            map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }
    }


    /**
     * Adds a marker to the map.
     */
    public void addMarkerToMap(LatLng latLng) {
        Marker marker = map.addMarker(new MarkerOptions().position(latLng));
        markers.add(marker);

    }

    /**
     * Clears all markers from the map.
     */
    public void clearMarkers() {
        map.clear();
        markers.clear();
    }

    /**
     * Remove the currently selected marker.
     */
    public void removeSelectedMarker() {
        this.markers.remove(this.selectedMarker);
        this.selectedMarker.remove();
    }

    /**
     * Highlight the marker by index.
     */
    private void highLightMarker(int index) {
        highLightMarker(markers.get(index));
    }

    /**
     * Highlight the marker by marker.
     */
    private void highLightMarker(Marker marker) {

		/*
		for (Marker foundMarker : this.markers) {
			if (!foundMarker.equals(marker)) {
				foundMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
			} else {
				foundMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
				foundMarker.showInfoWindow();
			}
		}
		*/
        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        marker.showInfoWindow();

        //Utils.bounceMarker(googleMap, marker);

        this.selectedMarker=marker;
    }

    private void resetMarkers() {
        for (Marker marker : this.markers) {
            marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        }
    }
    private void addDefaultLocations() {

        for(int i=0;i<points.size();i++){
            addMarkerToMap(points.get(i));
        }

    }

































}




