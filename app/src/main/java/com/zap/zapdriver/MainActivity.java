package com.zap.zapdriver;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.agrawalsuneet.dotsloader.loaders.AllianceLoader;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.angads25.toggle.interfaces.OnToggledListener;
import com.github.angads25.toggle.model.ToggleableView;
import com.github.angads25.toggle.widget.LabeledSwitch;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.squareup.picasso.Picasso;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;
import com.zap.zapdriver.API.Urls;
import com.zap.zapdriver.LocationUtils.LocationUtil;
import com.zap.zapdriver.LocationUtils.PicassoMarker;
import com.zap.zapdriver.Modules.DirectionFinder;
import com.zap.zapdriver.Modules.DirectionFinderListener;
import com.zap.zapdriver.Modules.Route;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;


public class MainActivity extends AppCompatActivity implements LocationUtil.GetLocationListener, DirectionFinderListener, NavigationView.OnNavigationItemSelectedListener {

    private GoogleMap googleMapHomeFrag;
    private double[] latLng = new double[2];
    LatLng driverLatLng;
    private PicassoMarker marker;
    private LocationUtil locationUtilObj;
    private final int REQUEST_CODE_PERMISSION_MULTIPLE = 123;
    private boolean isDeninedRTPs = true;       // initially true to prevent anim(2)
    private boolean showRationaleRTPs = false;
    private float start_rotation;


    Button btnEndRide;
    ArrayList<LatLng> markerPoints;
    String to, from = "";
    TextView source_location, destination_location,tvmenu;
    TextView txtFare, txtcustomer_name, tvscan;
    TextView btncall;
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
    MarkerOptions markerOptions;

    private String inputValue;
    private String savePath = Environment.getExternalStorageDirectory().getPath() + "/QRCode/";
    private Bitmap bitmap,bitmap2;
    private QRGEncoder qrgEncoder;
    private ImageView qrImage;


    View v;
    private static final int PERMISSIONS_REQUEST = 1;
    String accepted = "";

    private DatabaseReference databaseReference;
    protected LocationManager locationManager;
    protected LocationListener locationListener;
    AllianceLoader allianceLoader;
    Bitmap smallMarker;

    Handler handler = new Handler();
    Runnable runnable;
    int delay = 30 * 1000; //Delay for 30 seconds.  One second = 1000 milliseconds.
    ArrayList<LatLng> formerlocations;

    private String androidIdd;
    DatabaseReference usersRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);
        checkAndRequestRunTimePermissions();
        getCurrentLocation();
        //locationUtilObj = new LocationUtil(this, this);
        initMap();


        btnEndRide = findViewById(R.id.btnEndRide);
        source_location = findViewById(R.id.source_location);
        destination_location = findViewById(R.id.destination_location);
        txtFare = findViewById(R.id.txtFare);
        txtcustomer_name = findViewById(R.id.txtcustomer_name);
        btncall = findViewById(R.id.tvcallOrder);

        map_id = findViewById(R.id.map_id);
        ll_straight = findViewById(R.id.ll_straight);

        v = (View) findViewById(R.id.bottom);

        ll_to_from = findViewById(R.id.ll_to_from);
        ll_call = findViewById(R.id.ll_call);
        card_id_package = findViewById(R.id.card_id_package);
        card_id_package_serach = findViewById(R.id.card_id_package_serach);
        ll_main = findViewById(R.id.ll_main);

        qrImage = findViewById(R.id.qr_image);
        tvscan = findViewById(R.id.tvscan);
        tvmenu=findViewById(R.id.tvmenu);

        tvmenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(MainActivity.this,My_Deliveries.class));

            }
        });

        app = (DriverApplication) getApplicationContext();
        androidIdd = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        int height = 100;
        int width = 100;
        BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.riderremove);
        Bitmap b = bitmapdraw.getBitmap();
        smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

        SharedPreferences sharedPreferences = getSharedPreferences("PREFS_NAME", Context.MODE_PRIVATE);
        String user = sharedPreferences.getString("username", "");
        String id = sharedPreferences.getString("id", "");
        String pass = sharedPreferences.getString("password", "");





        markerPoints = new ArrayList<>();


        databaseReference = FirebaseDatabase.getInstance().getReference("Locations");


        if (user.equals("")) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();

        } else {
            app.setUsername(user);
            app.setUserid(id);
            app.setPassword(pass);

            Log.e("Name: ", app.getUsername());
        }


        int smallerDimension = width < height ? width : height;
        smallerDimension = 1000;
        Log.e("Size: ",String.valueOf(smallerDimension));




        tvscan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    try {
//                        boolean save = new QRGSaver().save(savePath, "qr", bitmap, QRGContents.ImageType.IMAGE_JPEG);
//                        String result = save ? "Image Saved" : "Image Not Saved";
                        saveToInternalSorage(bitmap);
                        saveToInternalSorageBarcode(bitmap2);
//                        Toast.makeText(MainActivity.this, "generated", Toast.LENGTH_LONG).show();


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                }


            }
        });


        checkAssigned();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w("TAG", "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();

                        // Log and toast
                        Log.e("TAG", "FCM Found: " + token);
                        Log.e("Name", ": " + app.getUserid());
                        app.setFcm_device_token(token);
                        Authorize_token();


                    }
                });
        btncall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(getApplicationContext(), "call " + from, Toast.LENGTH_SHORT).show();


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


            }
        });


    }


    private String saveToInternalSorage(Bitmap bitmapImage) {
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir("picture", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath = new File(directory, "qr.jpg");

        FileOutputStream fos = null;
        try {

            fos = new FileOutputStream(mypath);

            // Use the compress method on the BitMap object to write image to
            // the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();

            startActivity(new Intent(MainActivity.this,PrintQRActivity.class));


        } catch (Exception e) {
            e.printStackTrace();
        }
        return directory.getAbsolutePath();
    }


    private String saveToInternalSorageBarcode(Bitmap bitmapImage) {
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir("picture", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath = new File(directory, "barcode.jpg");

        FileOutputStream fos = null;
        try {

            fos = new FileOutputStream(mypath);

            // Use the compress method on the BitMap object to write image to
            // the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();

            startActivity(new Intent(MainActivity.this,PrintQRActivity.class));


        } catch (Exception e) {
            e.printStackTrace();
        }
        return directory.getAbsolutePath();
    }

    @Override
    protected void onResume() {
        super.onResume();

        getCurrentLocation();
        if (locationUtilObj != null /*&& !locationUtilObj.isGoogleAPIConnected()*/) {
            locationUtilObj.checkLocationSettings();
            locationUtilObj.restart_location_update();
        }
    }

    private void initMap() {
        SupportMapFragment mSupportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mSupportMapFragment.getMapAsync(new OnMapReadyCallback() {
            @SuppressLint("MissingPermission")
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;

                if (googleMap != null) {
                    googleMapHomeFrag = googleMap;
                    googleMapHomeFrag.getUiSettings().setAllGesturesEnabled(true);
                    googleMapHomeFrag.getUiSettings().setScrollGesturesEnabled(true);
                    googleMapHomeFrag.getUiSettings().setCompassEnabled(false);
                    googleMapHomeFrag.getUiSettings().setMapToolbarEnabled(false);

                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }

                    googleMapHomeFrag.setMyLocationEnabled(false);
                    googleMapHomeFrag.getUiSettings().setMyLocationButtonEnabled(true);

                    if (driverLatLng != null) {
                        if (googleMapHomeFrag != null) {
                            googleMapHomeFrag.moveCamera(CameraUpdateFactory.newLatLngZoom(driverLatLng, 12.0f));
                            googleMapHomeFrag.getUiSettings().setZoomControlsEnabled(false);
                        }
                    }


                }
            }
        });
    }

    private void getCurrentLocation() {
        if (locationUtilObj == null) {
            locationUtilObj = new LocationUtil(this, this);
        } else {
            locationUtilObj.checkLocationSettings();
        }
    }

    @Override
    public void updateLocation(Location location) {
        latLng[0] = location.getLatitude();
        latLng[1] = location.getLongitude();

        if (marker == null) {
            marker = new PicassoMarker(googleMapHomeFrag.addMarker(new MarkerOptions().position(new LatLng(latLng[0], latLng[1]))));
            Picasso.get().load(R.drawable.riderremove).resize(80, 80)
                    .into(marker);
            googleMapHomeFrag.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latLng[0], latLng[1]), 12.0f));
        }

        if ((latLng[0] != -1 && latLng[0] != 0) && (latLng[1] != -1 && latLng[1] != 0)) {
            //googleMapHomeFrag.moveCamera(CameraUpdateFactory.newLatLngZoom(driverLatLng, 12.0f));
            //float bearing = (float) bearingBetweenLocations(driverLatLng, new LatLng(location.getLatitude(), location.getLongitude()));
            if (marker != null) {
                moveVechile(marker.getmMarker(), location);
//                rotateMarker(marker.getmMarker(), location.getBearing(), start_rotation);
            }
            driverLatLng = new LatLng(latLng[0], latLng[1]);
        } else {
            Toast.makeText(this, "Location Not Found", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void location_Error(String error) {
        Log.e("LOCATIO_ERROR", error);
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_send) {


            new LovelyStandardDialog(this, LovelyStandardDialog.ButtonLayout.VERTICAL)
                    .setTopColorRes(R.color.colorPrimary)
                    .setButtonsColorRes(R.color.colorPrimary)
                    .setIcon(R.drawable.scooter)
                    .setTitle("Logout Confirmation")
                    .setMessage("Are you sure you want to log out?")
                    .setPositiveButton(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(new Intent(MainActivity.this, LoginActivity.class));
                            finish();
                        }
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .show();
            return true;

        } else if (id == R.id.nav_my_delivery) {

            startActivity(new Intent(this, My_Deliveries.class));

        } else if (id == R.id.nav_scan_return) {

            startActivity(new Intent(this, ScanReturnActivity.class));

        }
        return true;
    }

    private double bearingBetweenLocations(LatLng latLng1, LatLng latLng2) {

        double PI = 3.14159;
        double lat1 = latLng1.latitude * PI / 180;
        double long1 = latLng1.longitude * PI / 180;
        double lat2 = latLng2.latitude * PI / 180;
        double long2 = latLng2.longitude * PI / 180;

        double dLon = (long2 - long1);

        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1)
                * Math.cos(lat2) * Math.cos(dLon);

        double brng = Math.atan2(y, x);

        brng = Math.toDegrees(brng);
        brng = (brng + 360) % 360;

        return brng;
    }

    /**
     * custom method to check and request for run time permissions
     * if not granted already
     */
    private void checkAndRequestRunTimePermissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            // Marshmallow+

            if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        REQUEST_CODE_PERMISSION_MULTIPLE);

            }
        }


        onRunTimePermissionGranted();
    }
    /******************************************************/
    /**
     * custom method to execute after run time permissions
     * granted or if it run time permission no required at all
     */
    private void onRunTimePermissionGranted() {

        isDeninedRTPs = false;
        getCurrentLocation();
    }
    /******************************************************/

    /**
     * predefined method to check run time permissions list call back
     *
     * @param requestCode   : to handle the corresponding request
     * @param permissions:  contains the list of requested permissions
     * @param grantResults: contains granted and un granted permissions result list
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSION_MULTIPLE) {
            if (grantResults.length > 0) {
                for (int i = 0; i < permissions.length; i++) {
                    String permission = permissions[i];
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        isDeninedRTPs = true;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            showRationaleRTPs = shouldShowRequestPermissionRationale(permission);
                        }

                        break;
                    }

                }
                onRunTimePermissionDenied();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /******************************************************/

    private void onRunTimePermissionDenied() {
        if (isDeninedRTPs) {
            if (!showRationaleRTPs) {
                //goToSettings();
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                            REQUEST_CODE_PERMISSION_MULTIPLE);
                }
            }
        } else {
            onRunTimePermissionGranted();
        }
    }

    public void moveVechile(final Marker myMarker, final Location finalPosition) {

        final LatLng startPosition = myMarker.getPosition();

        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final Interpolator interpolator = new AccelerateDecelerateInterpolator();
        final float durationInMs = 3000;
        final boolean hideMarker = false;

        handler.post(new Runnable() {
            long elapsed;
            float t;
            float v;

            @Override
            public void run() {
                // Calculate progress using interpolator
                elapsed = SystemClock.uptimeMillis() - start;
                t = elapsed / durationInMs;
                v = interpolator.getInterpolation(t);

                LatLng currentPosition = new LatLng(
                        startPosition.latitude * (1 - t) + (finalPosition.getLatitude()) * t,
                        startPosition.longitude * (1 - t) + (finalPosition.getLongitude()) * t);
                myMarker.setPosition(currentPosition);
                // myMarker.setRotation(finalPosition.getBearing());


                // Repeat till progress is completeelse
                if (t < 1) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                    // handler.postDelayed(this, 100);
                } else {
                    if (hideMarker) {
                        myMarker.setVisible(false);
                    } else {
                        myMarker.setVisible(true);
                    }
                }
            }
        });


    }


    public void rotateMarker(final Marker marker, final float toRotation, final float st) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final float startRotation = marker.getRotation();
        final long duration = 1555;

        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed / duration);

                float rot = t * toRotation + (1 - t) * startRotation;


                marker.setRotation(-rot > 180 ? rot / 2 : rot);
                start_rotation = -rot > 180 ? rot / 2 : rot;
                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                }
            }
        });
    }


    private void Authorize_token() {


        StringRequest postRequest = new StringRequest(Request.Method.POST, Urls.Token,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // response

                        try {
                            JSONObject jsonArray = new JSONObject(response);

                            String token = jsonArray.getString("access");

                            Log.e("acceess", token);
                            app.setAuttoken((token));

                            Post_Device_fcm(token);


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


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
                params.put("username", app.getUsername());
                params.put("password", app.getPassword());

                return params;
            }


        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);

        requestQueue.add(postRequest);

    }

    private void checkAssigned() {

        Log.e("url", Urls.Delivery + "" + app.getUserid().toString());

        StringRequest stringRequest = new StringRequest(Request.Method.GET, Urls.Delivery + "" + app.getUserid(),
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {


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

                                    accepted = obj.getString("status");
                                    app.setPackage_from(pickup_name);
                                    app.setPackage_to(dropoff_name);

                                    to = pickup_name;
                                    from = receiver_phone;
                                    app.setPackage_id(id);

                                    destination_location.setText(dropoff_name + ", kenya");
                                    source_location.setText(pickup_name + ", kenya");
                                    txtFare.setText("Ksh " + cost);
                                    txtcustomer_name.setText("Package: " + title + "\n Receiver: " + receiver_phone + " \nDistance: " + distance + "km");

                                    pacakge = title;

                                    if (!accepted.equalsIgnoreCase("Picked")) {
                                        assignedDialog();
                                    } else {
                                        sendRequest();
                                    }


                                }


                                QRGEncoder qrgEncoder = new QRGEncoder("Delivery Package identity: " + app.getPackage_id(), null, QRGContents.Type.TEXT, 1000);
                                // Getting QR-Code as Bitmap
                                bitmap = qrgEncoder.getBitmap();
                                // Setting Bitmap to ImageView
                                qrImage.setImageBitmap(bitmap);


                                try {
                                    BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                                    bitmap2 = barcodeEncoder.encodeBitmap("Delivery Package identity: " + app.getPackage_id(), BarcodeFormat.CODE_128, 600, 400);
                                    ImageView imageViewQrCode = (ImageView) findViewById(R.id.barcode_image);
                                    imageViewQrCode.setImageBitmap(bitmap2);
                                } catch (Exception e) {
                                    e.printStackTrace();

                                }


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
            new DirectionFinder(MainActivity.this, origin, destination).execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
            mMap.moveCamera(CameraUpdateFactory.newLatLng(route.startLocation));


            txtcustomer_name.setText("Package: " + pacakge + "\n Distance: " + route.distance.text + " \nTime: " + route.duration.text);


            originMarkers.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.scooter))
                    .title(route.startAddress)
                    .position(route.startLocation)));

            destinationMarker.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.user))
                    .title(route.endAddress)
                    .position(route.endLocation)));

            app.setDestination(new LatLng(route.endLocation.latitude, route.endLocation.longitude));

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
                .setMessage(app.getUsername() + " You are currently offline\nYou will not be able to receive any \ndelivery request")
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

    private void Post_Device_fcm(String token) {


        HashMap<String, String> params = new HashMap<String, String>();
        params.put("registration_id", token);
        params.put("type", "android");
        params.put("name", app.getUsername());
        params.put("user", app.getUserid());
        params.put("device_id", androidIdd);

        Log.e("param: ", params.toString());

        Log.e("params", params.toString());

        JsonObjectRequest req = new JsonObjectRequest(Urls.FCM_URL, new JSONObject(params),
                (JSONObject response) -> {
                    try {


                        Log.e("JsonResponse", response.toString(4));


                    } catch (JSONException e) {


                        e.printStackTrace();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Log.e("Eror", error.toString());


            }
        }) {


            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");

                String auth = "Bearer " + app.getAuttoken();
                headers.put("Authorization", auth);
                return headers;
            }


        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        requestQueue.add(req);

    }


}
