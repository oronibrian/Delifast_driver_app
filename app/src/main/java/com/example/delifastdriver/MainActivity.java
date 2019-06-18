package com.example.delifastdriver;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.delifastdriver.API.Urls;
import com.example.delifastdriver.Modules.DirectionFinder;
import com.example.delifastdriver.Modules.DirectionFinderListener;
import com.example.delifastdriver.Modules.Route;
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
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, DirectionFinderListener {

    Button btnEndRide;
    ArrayList<LatLng> markerPoints;
    private GoogleMap mMap;
    String to,from="";
    EditText source_location,destination_location;
    TextView txtFare,txtcustomer_name;
    ImageButton btncall;


    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarker = new ArrayList<>();
    private List<Polyline> polyLinePaths = new ArrayList<>();
    private ProgressDialog progressDialog;
    String pacakge;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnEndRide = findViewById(R.id.btnEndRide);
        source_location=findViewById(R.id.source_location);
        destination_location=findViewById(R.id.destination_location);
        txtFare=findViewById(R.id.txtFare);
        txtcustomer_name=findViewById(R.id.txtcustomer_name);
        btncall=findViewById(R.id.btncall);





        markerPoints = new ArrayList<>();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        loadDeliveryRoute();




        btncall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:"+from));

                if (ActivityCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                startActivity(callIntent);
            }
        });



        btnEndRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), SignatureActivity.class));
                update_package();
                finish();


            }
        });


    }


    private void loadDeliveryRoute() {



        StringRequest stringRequest = new StringRequest(Request.Method.GET, Urls.ApiUrls,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Log.e("Resp", response);


                        try {
                            JSONObject obj = new JSONObject(response);

                            String package_id = obj.getString("id");
                            String receiver_phone = obj.getString("receiver_phone");
                            String pickup_name = obj.getString("pickup_name");
                            String title = obj.getString("title");
                            String dropoff_name = obj.getString("dropoff_name");
                            String amount = obj.getString("cost");

                            String distance = obj.getString("distance");


                            Log.e("id:", package_id);
                            Log.e("receiver_phone:", receiver_phone);
                            Log.e("pickup_name:", pickup_name);
                            Log.e("title:", title);
                            Log.e("dropoff_name:", dropoff_name);

                            to=pickup_name;
                            from=receiver_phone;

                            destination_location.setText(dropoff_name +", kenya");
                            source_location.setText(pickup_name+ ", kenya");
                            txtFare.setText("Ksh "+amount);
                            txtcustomer_name.setText("Package: "+title +"\n Receiver: "+receiver_phone+" \nDistance: "+distance +"km");

                            pacakge=title;


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //displaying the error in toast if occurrs
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });



        //creating a request queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        //adding the string request to request queue
        requestQueue.add(stringRequest);

        requestQueue.addRequestFinishedListener(new RequestQueue.RequestFinishedListener<Object>() {

            @Override
            public void onRequestFinished(Request<Object> request) {
               sendRequest();

            }
        });

    }





    @Override
    public void onMapReady(GoogleMap googleMap) {
        final CameraUpdate zoom = CameraUpdateFactory.zoomTo(12);

        mMap = googleMap;
        final MarkerOptions mp = new MarkerOptions();
        final MarkerOptions mp2 = new MarkerOptions();

        final Geocoder geocoder = new Geocoder(this);



        int reqCode = 1;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, reqCode);
            return;
        }
        mMap.setMyLocationEnabled(true);





    }


    private void sendRequest() {
        String origin = source_location.getText().toString();
        String destination = destination_location.getText().toString();

        if (origin.isEmpty()) {
            Toast.makeText(this, "Please enter origin!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (destination.isEmpty()){
            Toast.makeText(this, "Please enter destination!", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            new DirectionFinder(this, origin, destination).execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }





    private void update_package(){

        RequestQueue queue = Volley.newRequestQueue(this); // this = context

        String url = "http://206.81.0.212/api/packages/1/";

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
                Map<String, String> params = new HashMap<String, String> ();
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


            txtcustomer_name.setText("Package: "+pacakge +"\n Distance: "+route.distance.text+" \nTime: "+route.duration.text );


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


}