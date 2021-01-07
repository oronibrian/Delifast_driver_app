package com.zap.zapdriver;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.agrawalsuneet.dotsloader.loaders.AllianceLoader;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.angads25.toggle.interfaces.OnToggledListener;
import com.github.angads25.toggle.model.ToggleableView;
import com.github.angads25.toggle.widget.LabeledSwitch;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;
import com.zap.zapdriver.API.Urls;
import com.zap.zapdriver.Modules.DirectionFinder;
import com.zap.zapdriver.Modules.DirectionFinderListener;
import com.zap.zapdriver.Modules.Route;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, DirectionFinderListener, LocationListener,
        NavigationView.OnNavigationItemSelectedListener {

    Button btnEndRide;
    ArrayList<LatLng> markerPoints;
    String to, from = "";
    TextView source_location, destination_location;
    TextView txtFare, txtcustomer_name;
    ImageButton btncall;
    String pacakge;
    DriverApplication app;
    private GoogleMap mMap;
    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarker = new ArrayList<>();
    private List<Polyline> polyLinePaths = new ArrayList<>();
    private ProgressDialog progressDialog;
    LabeledSwitch labeledSwitch;
    RelativeLayout map_id;
    LinearLayout ll_straight, ll_to_from, ll_call;
    LinearLayout card_id_package, card_id_package_serach;
    RelativeLayout ll_main;

    View v;
    private static final int PERMISSIONS_REQUEST = 1;

    private DatabaseReference databaseReference;
    protected LocationManager locationManager;
    protected LocationListener locationListener;
    AllianceLoader allianceLoader;
    Bitmap smallMarker;

    Handler handler = new Handler();
    Runnable runnable;
    int delay = 30 * 1000; //Delay for 30 seconds.  One second = 1000 milliseconds.


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        btnEndRide = findViewById(R.id.btnEndRide);
        source_location = findViewById(R.id.source_location);
        destination_location = findViewById(R.id.destination_location);
        txtFare = findViewById(R.id.txtFare);
        txtcustomer_name = findViewById(R.id.txtcustomer_name);
        btncall = findViewById(R.id.btncall);

        map_id = findViewById(R.id.map_id);
        ll_straight = findViewById(R.id.ll_straight);

        v = (View) findViewById(R.id.bottom);

        ll_to_from = findViewById(R.id.ll_to_from);
        ll_call = findViewById(R.id.ll_call);
        card_id_package = findViewById(R.id.card_id_package);
        card_id_package_serach = findViewById(R.id.card_id_package_serach);
        ll_main = findViewById(R.id.ll_main);


        app = (DriverApplication) getApplicationContext();

        int height = 100;
        int width = 100;
        BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.riderremove);
        Bitmap b = bitmapdraw.getBitmap();
        smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

        SharedPreferences sharedPreferences = getSharedPreferences("PREFS_NAME", Context.MODE_PRIVATE);
        String user = sharedPreferences.getString("username", "");
        String id = sharedPreferences.getString("id", "");

        if (user == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();

        } else {
            app.setUsername(user);
            app.setUserid(id);
        }


        Log.e("Name: ", app.getUsername());


        markerPoints = new ArrayList<>();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        databaseReference = FirebaseDatabase.getInstance().getReference("Locations");
        DatabaseReference usersRef = databaseReference.child(app.getUsername());

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    String databaseLatitudeString = dataSnapshot.child("latitude").getValue().toString().substring(1, dataSnapshot.child("latitude").getValue().toString().length() - 1);
                    String databaseLongitudedeString = dataSnapshot.child("longitude").getValue().toString().substring(1, dataSnapshot.child("longitude").getValue().toString().length() - 1);

                    String[] stringLat = databaseLatitudeString.split(", ");
                    Arrays.sort(stringLat);
                    String latitude = stringLat[stringLat.length - 1].split("=")[1];

                    String[] stringLong = databaseLongitudedeString.split(", ");
                    Arrays.sort(stringLong);
                    String longitude = stringLong[stringLong.length - 1].split("=")[1];
//                    mMap.clear();




                } catch (Exception e) {
                    e.printStackTrace();
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


//        loadDeliveryRoute();
        checkAssigned();

        handler.postDelayed(runnable = new Runnable() {
            public void run() {
                //do something
                checkAssigned();

                handler.postDelayed(runnable, delay);
            }
        }, delay);


        // Check GPS is enabled
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "Please enable location services", Toast.LENGTH_SHORT).show();
//            finish();
        }

        // Check location permission is granted - if it is, start
        // the service, otherwise request the permission
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permission == PackageManager.PERMISSION_GRANTED) {

        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST);
        }


        btncall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + from));

                if (ActivityCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                startActivity(callIntent);
            }
        });


        labeledSwitch = findViewById(R.id.swithch);
        labeledSwitch.setOnToggledListener(new OnToggledListener() {
            @Override
            public void onSwitched(ToggleableView toggleableView, boolean isOn) {

                if (!isOn) {
                    // The toggle is enabled
                    showDialog();


                }


            }


        });

        btnEndRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), SignatureActivity.class));
                update_package();


            }
        });

//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//
//        DrawerLayout drawer = findViewById(R.id. drawer_layout ) ;
//        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
//                this, drawer , toolbar , R.string.navigation_drawer_open ,
//                R.string.navigation_drawer_close ) ;
//        drawer.addDrawerListener(toggle) ;
//        toggle.syncState() ;
        NavigationView navigationView = findViewById(R.id. nav_view ) ;
        navigationView.setNavigationItemSelectedListener( this ) ;


    }


    private void checkAssigned() {

        Log.e("url", Urls.Delivery + "" + app.getUserid().toString());

        StringRequest stringRequest = new StringRequest(Request.Method.GET, Urls.Delivery + "" + app.getUserid(),
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                        Log.e("response", response.toString());

                        try {

                            Log.e("response", response.toString());

                            JSONArray jsonArray = new JSONArray(response);

                            if (jsonArray.length() > 0) {
                                for (int i = 0; i < jsonArray.length(); i++) {

                                    JSONObject obj = jsonArray.getJSONObject(i);
                                    String id = obj.getString("id");

                                    String pickup_name = obj.getString("pickup_name");
                                    String dropoff_name = obj.getString("dropoff_name");
                                    String title = obj.getString("title");

                                    String distance = obj.getString("distance");
                                    String cost = obj.getString("cost");
                                    String receiver_phone = obj.getString("receiver_phone");

                                    to = pickup_name;
                                    from = receiver_phone;
                                    app.setPackage_id(id);

                                    destination_location.setText(dropoff_name + ", kenya");
                                    source_location.setText(pickup_name + ", kenya");
                                    txtFare.setText("Ksh " + cost);
                                    txtcustomer_name.setText("Package: " + title + "\n Receiver: " + receiver_phone + " \nDistance: " + distance + "km");

                                    pacakge = title;


                                }
                                assignedDialog();


                            } else {
                                v.setVisibility(View.VISIBLE);

                                Log.e("Empty", "Empty");

                                card_id_package_serach.setVisibility(View.VISIBLE);
                                card_id_package.setVisibility(View.GONE);
//                                ll_call.setVisibility(View.VISIBLE);

                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e("error", e.toString());


                        }



                    }

                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //displaying the error in toast if occurrs
                        Log.e("Error", "Error: " + error
                                + "\nCause " + error.getCause()
                                + "\nmessage" + error.getMessage());
//                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                }


        );


        //creating a request queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        //adding the string request to request queue
        requestQueue.add(stringRequest);

//        requestQueue.addRequestFinishedListener(new RequestQueue.RequestFinishedListener<Object>() {
//
//            @Override
//            public void onRequestFinished(Request<Object> request) {
////                sendRequest();
//
//            }
//        });

    }
    @Override
    public void onBackPressed () {
        DrawerLayout drawer = findViewById(R.id. drawer_layout ) ;
        if (drawer.isDrawerOpen(GravityCompat. START )) {
            drawer.closeDrawer(GravityCompat. START ) ;
        } else {
            super .onBackPressed() ;
        }
    }



    private void updateLocation(String location) {

        RequestQueue queue = Volley.newRequestQueue(this); // this = context


        String url =  Urls.location_update+""+app.getUserid();
        StringRequest putRequest = new StringRequest(Request.Method.PUT, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.d("Response", response);
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("Error.Response", error.toString());
                    }
                }
        ) {

            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String> ();
                params.put("location", location);
                return params;
            }

        };

        queue.add(putRequest);


    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        final CameraUpdate zoom = CameraUpdateFactory.zoomTo(12);

        mMap = googleMap;
        final MarkerOptions mp = new MarkerOptions();
        final MarkerOptions mp2 = new MarkerOptions();

        final Geocoder geocoder = new Geocoder(this);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);


        int reqCode = 1;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {


            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, reqCode);
            return;
        }

        try {
            long MIN_DIST = 0;
            long MIN_TIME = 2000;
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DIST, this);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DIST, this);

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("error loc: ", String.valueOf(e.getMessage()));
        }
//        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, (android.location.LocationListener) this);

        mMap.setMyLocationEnabled(true);


    }


    private void sendRequest() {
        String origin = source_location.getText().toString();
        String destination = destination_location.getText().toString();

        if (origin.isEmpty()) {
            Toast.makeText(this, "Please enter origin!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (destination.isEmpty()) {
            Toast.makeText(this, "Please enter destination!", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            new DirectionFinder(this, origin, destination).execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void update_package() {

        RequestQueue queue = Volley.newRequestQueue(this); // this = context

        String url = "http://206.81.0.212/api/packages/1/";

        StringRequest putRequest = new StringRequest(Request.Method.PUT, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.d("Response", response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("Error.Response", error.toString());
                    }
                }
        ) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("status", "Delivered");

                return params;
            }

        };

        queue.add(putRequest);
    }

    @Override
    public void onDirectionFinderStart() {

        progressDialog = ProgressDialog.show(this, "Please wait", "Finding direction", true);
        if (originMarkers != null) {
            for (Marker marker : originMarkers) {
                marker.remove();
            }
        }
        if (destinationMarker != null) {
            for (Marker marker : destinationMarker) {
                marker.remove();
            }
        }
        if (polyLinePaths != null) {
            for (Polyline polylinePath : polyLinePaths) {
                polylinePath.remove();
            }
        }

    }

    @Override
    public void onDirectionFinderSuccess(List<Route> routes) {

        polyLinePaths = new ArrayList<>();
        originMarkers = new ArrayList<>();
        destinationMarker = new ArrayList<>();

        progressDialog.dismiss();
        for (Route route : routes) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(route.startLocation, 13));


            txtcustomer_name.setText("Package: " + pacakge + "\n Distance: " + route.distance.text + " \nTime: " + route.duration.text);


            originMarkers.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.scooter))
                    .title(route.startAddress)
                    .position(route.startLocation)));

            destinationMarker.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.user))
                    .title(route.endAddress)
                    .position(route.endLocation)));

            PolylineOptions polylineOptions = new PolylineOptions()
                    .geodesic(true)
                    .color(Color.RED)
                    .width(10);

            for (int i = 0; i < route.points.size(); i++) {
                polylineOptions.add(route.points.get(i));
            }

            polyLinePaths.add(mMap.addPolyline(polylineOptions));
        }
    }


    public void showDialog() {

        card_id_package.setVisibility(View.GONE);
        card_id_package_serach.setVisibility(View.GONE);

        new LovelyStandardDialog(this, LovelyStandardDialog.ButtonLayout.VERTICAL)
                .setTopColorRes(R.color.red_600)
                .setButtonsColorRes(R.color.green)
                .setIcon(R.drawable.ic_location_tracking)
                .setTitle("Driver Offline")
                .setMessage(app.getUsername()+ " You are currently offline\nYou will not be able to receive any \ndelivery request")
                .setCancelable(false)
                .setPositiveButton("Go Online", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getApplicationContext(), "positive clicked", Toast.LENGTH_SHORT).show();

                        labeledSwitch.isOn();
                        labeledSwitch.setOn(true);
                    }
                })
                .show();

    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        Log.e("lat", Double.toString(location.getLatitude()));
        Log.e("long", Double.toString(location.getLongitude()));


        databaseReference.child(app.getUsername()).child("latitude").push().setValue(Double.toString(location.getLatitude()));
        databaseReference.child(app.getUsername()).child("longitude").push().setValue(Double.toString(location.getLongitude()));

        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title(app.getUsername());
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(smallMarker));
        mMap.addMarker(markerOptions);

        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
//        mMap.animateCamera(CameraUpdateFactory.zoomTo(12));


        String loc = location.getLatitude() + "," + location.getLongitude();


        updateLocation(loc);

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        showLocationDissabledDialog();
    }


    public void acceptPackage(String package_id, String rider_id) {
        RequestQueue queue = Volley.newRequestQueue(this); // this = context

        String url = Urls.acceptrequest + "/" + package_id + "/" + rider_id;
        StringRequest postRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.d("Response", response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("Error.Response", error.toString());
                    }
                }
        );

        queue.add(postRequest);
    }


    public void rejectPackage(String package_id, String rider_id) {
        RequestQueue queue = Volley.newRequestQueue(this); // this = context

        String url = Urls.rejectrequest + "/" + package_id + "/" + rider_id;
        StringRequest postRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.d("Response", response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("Error.Response", error.toString());
                    }
                }
        );

        queue.add(postRequest);
    }


    public void showLocationDissabledDialog() {

        card_id_package.setVisibility(View.GONE);
        card_id_package_serach.setVisibility(View.GONE);

        new LovelyStandardDialog(this, LovelyStandardDialog.ButtonLayout.VERTICAL)
                .setTopColorRes(R.color.red_600)
                .setButtonsColorRes(R.color.green)
                .setIcon(R.drawable.ic_location_tracking)
                .setTitle("Location Disabled")
                .setMessage("Your location is current disabled")
                .setCancelable(false)
                .setPositiveButton("Enable", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

//                        labeledSwitch.isOn();
//                        labeledSwitch.setOn(true);
                    }
                })
                .show();

    }


    public void assignedDialog() {
        new LovelyStandardDialog(this, LovelyStandardDialog.ButtonLayout.VERTICAL)
                .setTopColorRes(R.color.green_500)
                .setButtonsColorRes(R.color.black)
                .setIcon(R.drawable.scooter)
                .setCancelable(false)
                .setTitle("Package Request")
                .setMessage("You have been assigned a package")
                .setPositiveButton("Accept", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


                        card_id_package_serach.setVisibility(View.GONE);
                        card_id_package.setVisibility(View.VISIBLE);

                        acceptPackage(app.getPackage_id(), app.getUserid());

                        sendRequest();


                    }
                })
                .setNegativeButton("Reject", new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        rejectPackage(app.getPackage_id(), app.getUserid());
                        checkAssigned();

                    }
                })
                .show();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }
}