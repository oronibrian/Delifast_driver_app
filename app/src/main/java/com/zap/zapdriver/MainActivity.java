package com.zap.zapdriver;

import static android.app.Notification.DEFAULT_SOUND;
import static android.app.Notification.DEFAULT_VIBRATE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
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
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.agrawalsuneet.dotsloader.loaders.AllianceLoader;
import com.alespero.expandablecardview.ExpandableCardView;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
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
import com.github.ybq.android.spinkit.SpinKitView;
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
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import tech.gusavila92.websocketclient.WebSocketClient;
import timber.log.Timber;


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
    AlertDialog mpesaalertDialog, paybillalertDialog;

    private WebSocketClient webSocketClient;

    TextView btnEndRide;
    ArrayList<LatLng> markerPoints;
    String to, from = "";
    TextView source_location, destination_location, tvmenu, textView_details;
    TextView txtFare, txtcustomer_name, tvscan;
    ImageView btncall;
    String pacakge;
    DriverApplication app;
    private GoogleMap mMap;
    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarker = new ArrayList<>();
    private List<Polyline> polyLinePaths = new ArrayList<>();
    private ProgressDialog progressDialog;
    LabeledSwitch labeledSwitch;
    RelativeLayout map_id;
    LinearLayout ll_straight, ll_to_from, ll_call, ll_buttons;
    LinearLayout card_id_package, card_id_package_serach;
    RelativeLayout ll_main;
    MarkerOptions markerOptions;

    String my_current_location = "";
    private String savePath = Environment.getExternalStorageDirectory().getPath() + "/QRCode/";
    private Bitmap bitmap, bitmap2;
    private QRGEncoder qrgEncoder;
    private ImageView qrImage;

    LatLng rider_location;

    SpinKitView spin_kit;
    TextView ll_navigation;
    ExpandableCardView profile;

    Boolean reprint = false;

    View v;
    private static final int PERMISSIONS_REQUEST = 1;
    String accepted = "";

    protected LocationManager locationManager;
    protected LocationListener locationListener;
    AllianceLoader allianceLoader;
    Bitmap smallMarker;

    Handler handler = new Handler();
    Runnable runnable;
    int delay = 30 * 1000; //Delay for 30 seconds.  One second = 1000 milliseconds.
    ArrayList<LatLng> formerlocations;

    private String androidIdd;

    Boolean asigned = false;
    Boolean offline_payment = false;

    int amout_cost = 0;


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
        tvmenu = findViewById(R.id.tvmenu);
        spin_kit = findViewById(R.id.spin_kit);
        ll_buttons = findViewById(R.id.ll_buttons);
        profile = findViewById(R.id.profile);


        ll_navigation = findViewById(R.id.ll_navigation);

        //get the values of the settings options

        tvmenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(MainActivity.this, My_Deliveries.class));

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

        String name = sharedPreferences.getString("name", "");

        String last_name = sharedPreferences.getString("last_name", "");
        String email = sharedPreferences.getString("email", "");
        String phone_no = sharedPreferences.getString("phone_no", "");


        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);

        TextView inf = header.findViewById(R.id.textView_details);
        inf.setText(name + " " + last_name + "\n" + email);


        markerPoints = new ArrayList<>();


        if (user.equals("")) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();

        } else {
            app.setUsername(email);
            app.setUserid(id);
            app.setPassword(pass);
            app.setPhone_no(phone_no);

            Request_token(user, pass);

            Log.e("email: ", String.valueOf(email));


        }


        ll_navigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                startActivity(new Intent(getApplicationContext(), TurnNavigation2.class));


                Uri gmmIntentUri = Uri.parse("google.navigation:q=" + source_location.getText().toString() + "o,+" + destination_location.getText().toString());
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);


//                Uri.Builder builder = new Uri.Builder();
//                builder.scheme("https")
//                        .authority("www.google.com")
//                        .appendPath("maps")
//                        .appendPath("dir")
//                        .appendPath("")
//                        .appendQueryParameter("api", "1")
//                        .appendQueryParameter("destination", app.getDestination().latitude + "," + app.getDestination().longitude);
//                String url = builder.build().toString();
//                Log.d("Directions", url);
//                Intent i = new Intent(Intent.ACTION_VIEW);
//                i.setData(Uri.parse(url));
//                startActivity(i);


            }
        });


        int smallerDimension = width < height ? width : height;
        smallerDimension = 1000;
        Log.e("Size: ", String.valueOf(smallerDimension));


        tvscan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    try {
//                        boolean save = new QRGSaver().save(savePath, "qr", bitmap, QRGContents.ImageType.IMAGE_JPEG);
//                        String result = save ? "Image Saved" : "Image Not Saved";
                        saveToInternalSorage(bitmap, txtcustomer_name.getText().toString());
                        saveToInternalSorageBarcode(bitmap2);
//                        Toast.makeText(MainActivity.this, "generated", Toast.LENGTH_LONG).show();

                        if (reprint) {
                            PickPackage(app.getPackage_id(), app.getUserid());
                        } else {
                            PickPackage(app.getPackage_id(), app.getUserid());

                            Toast.makeText(MainActivity.this, "Re-printing", Toast.LENGTH_SHORT).show();

                        }


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                }


            }
        });


        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.e("TAG", "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();

                        // Log and toast
                        Log.i("TAG", "FCM Found: " + token);
                        Log.i("Name", ": " + app.getUserid());
                        Log.i("email", ": " + app.getUsername());

                        app.setFcm_device_token(token);
//                        Post_Device_fcm(token);

//                        Authorize_token();


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
                    offLine(app.getUserid());


                }


            }


        });

        btnEndRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (app.getIs_cooperate()) {
                    startActivity(new Intent(getApplicationContext(), SignatureActivity.class));

                    Toast.makeText(getApplicationContext(), "Cooperate package", Toast.LENGTH_SHORT).show();
                } else {
                    checkPaid();

                }


            }
        });


        //Websocket listening for request


        createWebSocketClient(app.getUserid(), "18.159.15.240");


    }


    private void checkPaid() {

        Timber.e(Urls.checkPaid + "" + app.getPackage_id().toString());

        StringRequest stringRequest = new StringRequest(Request.Method.GET, Urls.checkPaid + "" + app.getPackage_id(),
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                        Log.i("response", response.toString());

                        try {

                            Log.i("response", response.toString());

                            JSONObject jsonArray = new JSONObject(response);

                            String paid = jsonArray.getString("msg");

                            if (paid.equalsIgnoreCase("True")) {

                                startActivity(new Intent(getApplicationContext(), SignatureActivity.class));
                            } else {
                                Toast.makeText(MainActivity.this, "No payment found", Toast.LENGTH_SHORT).show();


                                stkPush();
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


    private void stkPush() {
        // custom dialog

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.stk_push_layout, null);
        dialogBuilder.setView(dialogView);

        CheckBox online = dialogView.findViewById(R.id.checkBox);
        CheckBox offline = dialogView.findViewById(R.id.checkBox2);

        EditText stk_number = dialogView.findViewById(R.id.stk_mpesanumber);
        LinearLayout ll_offlibe = dialogView.findViewById(R.id.ll_offlibe);
        LinearLayout ll_online = dialogView.findViewById(R.id.ll_online);

        TextView txt_offline = dialogView.findViewById(R.id.txt_offline);

        txt_offline.setText("Select Lipa na mpesa\nPaybill no 869032\nAmount: " + amout_cost);


        online.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isChecked()) {
                    ll_offlibe.setVisibility(View.GONE);
                    ll_online.setVisibility(View.VISIBLE);
                    offline.setChecked(false);
                    offline.setSelected(false);
                    offline_payment = false;
                }

            }

        });


        offline.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isChecked()) {
                    ll_online.setVisibility(View.GONE);
                    ll_offlibe.setVisibility(View.VISIBLE);

                    online.setChecked(false);
                    online.setSelected(false);
                    offline_payment = true;
                }

            }

        });


        Button btncomplete = dialogView.findViewById(R.id.buttontn);

        btncomplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (offline_payment) {
                    startActivity(new Intent(getApplicationContext(), SignatureActivity.class));
                    finish();

                } else {
                    String value = "254" + stk_number.getText().toString().substring(1);

                    stkPushMethod(value);
                }

            }
        });

        paybillalertDialog = dialogBuilder.create();
        paybillalertDialog.show();
    }


    private void Request_token(String email, String password) {


        StringRequest postRequest = new StringRequest(Request.Method.POST, Urls.Auth,
                response -> {
                    // response
                    Log.e("Auth", response);
                    try {
                        JSONObject data = new JSONObject(response);


                        if (!data.getString("access").isEmpty()) {

                            String token = data.getString("access");
                            String phone_no = data.getString("phone_no");

                            SharedPreferences preferences = getSharedPreferences("PREFS_NAME",
                                    Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString("token", token);

                            editor.apply();

                            app.setAuttoken(token);
                            app.setPhone_no(phone_no);


                            if (asigned = false) {


                                checkAssigned();
                                Post_Device_fcm(token);



                            } else {
                                Log.e("Do Nothing", "..........pull data..................");
                                pulldata();

                            }




                        }
                    } catch (JSONException e) {
                        e.printStackTrace();

                    }


                },
                error -> {
                    // error
                    Log.d("Error.Response", error.toString());

                    startActivity(new Intent(this, LoginActivity.class));
                    finish();


                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("email", email);
                params.put("password", password);

                return params;
            }


            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);

        requestQueue.add(postRequest);

    }


    private void stkPushMethod(String payment_phone) {

        RequestQueue queue = Volley.newRequestQueue(this); // this = context


        HashMap<String, String> params = new HashMap<String, String>();
        params.put("payment_phone", payment_phone);


        String url = Urls.onlinepayment + "" + amout_cost + "/" + payment_phone + "/" + app.getPackage_id();
        Log.e("payment_phone--url", url);
        Log.e("--param", payment_phone);

        JsonObjectRequest putRequest = new JsonObjectRequest(Request.Method.GET, url, null,

                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // response
                        Log.e("mpesa", response.toString());


                        try {
                            JSONObject data = response;


                            if (data.getString("ResultDesc").contains("successfully")) {

                                Toast.makeText(MainActivity.this, "Received for processing", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getApplicationContext(), SignatureActivity.class));


                            } else {
                                Toast.makeText(getApplicationContext(), "" + data.getString("ResultDesc"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();

                        }


                        paybillalertDialog.dismiss();

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.e("Error.Response", error.toString());
                        paybillalertDialog.dismiss();

                    }
                }
        ) {


            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");

                String auth = "Bearer " + app.getAuttoken();
                headers.put("Authorization", auth);
                return headers;
            }


        };

        putRequest.setRetryPolicy(new DefaultRetryPolicy(
                5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(putRequest);


    }

    private void updatePostLocation(String location) {

        RequestQueue queue = Volley.newRequestQueue(this); // this = context


        HashMap<String, String> params = new HashMap<String, String>();
        params.put("location", location);


        String url = Urls.location_update + "" + app.getUserid();
        Log.e("Location--url", url);
        Log.e("Location--param", location);

        JsonObjectRequest putRequest = new JsonObjectRequest(Request.Method.PUT, url, new JSONObject(params),

                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // response
                        Log.e("Location--updated", response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.e("Error.Response", error.toString());
                    }
                }
        ) {


            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");

                String auth = "Bearer " + app.getAuttoken();
                headers.put("Authorization", auth);
                return headers;
            }


        };

        queue.add(putRequest);


    }


    private String saveToInternalSorage(Bitmap bitmapImage, String data) {
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

            Intent i = new Intent(MainActivity.this, PrintQRActivity.class);
            i.putExtra("data", data);
            startActivity(i);


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


        } catch (Exception e) {
            e.printStackTrace();
        }
        return directory.getAbsolutePath();
    }

    @Override
    protected void onResume() {
        super.onResume();


        SharedPreferences sharedPreferences = getSharedPreferences("PREFS_NAME", Context.MODE_PRIVATE);
        String user = sharedPreferences.getString("username", "");
        String id = sharedPreferences.getString("id", "");
        String pass = sharedPreferences.getString("password", "");

        String name = sharedPreferences.getString("name", "");

        String last_name = sharedPreferences.getString("last_name", "");
        String email = sharedPreferences.getString("email", "");
        String phone_no = sharedPreferences.getString("phone_no", "");

        Log.e("IsASSIGNED", asigned.toString());
        app.setUsername(email);
        app.setUserid(id);
        app.setPassword(pass);
        app.setPhone_no(phone_no);

        Request_token(user, pass);

        getCurrentLocation();
        if (locationUtilObj != null /*&& !locationUtilObj.isGoogleAPIConnected()*/) {
            locationUtilObj.checkLocationSettings();
            locationUtilObj.restart_location_update();
        }

        if (asigned = false) {


            handler.postDelayed(runnable = new Runnable() {
                public void run() {
                    //do something

                    Log.e("checking", "............................");

                    checkAssigned();


                    handler.postDelayed(runnable, delay);
                }
            }, delay);


        } else {
            Log.e("Do Nothing", "............................");
            handler.removeMessages(0);


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

                    googleMapHomeFrag.setMyLocationEnabled(true);
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


        rider_location = new LatLng(location.getLatitude(), location.getLongitude());

        Log.e("rider_location", "rider_location-....." + rider_location);


        new Handler().postDelayed(new Runnable() {
            public void run() {
                // do something...
                Log.e("update", "updating loc-........");

                updatePostLocation(location.getLatitude() + "," + location.getLongitude());


            }
        }, 20000);

        float bearing = (float) bearingBetweenLocations(rider_location, rider_location);

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

                            offLine(app.getUserid());

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
        // Marshmallow+

        if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_CODE_PERMISSION_MULTIPLE);

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
                myMarker.setRotation(finalPosition.getBearing());


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


        StringRequest postRequest = new StringRequest(Request.Method.POST, Urls.Auth,
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

                            Log.e("Package", response.toString());

                            JSONArray jsonArray = new JSONArray(response);

                            if (jsonArray.length() > 0) {
                                spin_kit.setVisibility(View.GONE);
                                asigned = true;

                                btnEndRide.setVisibility(View.VISIBLE);
                                ll_straight.setVisibility(View.VISIBLE);
                                ll_to_from.setVisibility(View.VISIBLE);
                                ll_call.setVisibility(View.VISIBLE);
                                ll_buttons.setVisibility(View.VISIBLE);

                                for (int i = 0; i < jsonArray.length(); i++) {

                                    JSONObject obj = jsonArray.getJSONObject(i);
                                    String id = obj.getString("id");

                                    String pickup_name = obj.getString("pickup_name");
                                    String dropoff_name = obj.getString("dropoff_name");
                                    String title = obj.getString("title");

                                    String distance = obj.getString("distance");
                                    String cost = obj.getString("cost");
                                    String receiver_phone = obj.getString("receiver_phone");
                                    String receiver_name = obj.getString("receiver_name");
                                    String sendername = obj.getString("sendername");

                                    String rider_amount = obj.getString("rider_amount");
                                    String client_cost = obj.getString("client_cost");

                                    accepted = obj.getString("status");
                                    app.setPackage_from(pickup_name);
                                    app.setPackage_to(dropoff_name);

                                    app.setIs_cooperate(obj.getBoolean("is_cooperate"));

                                    to = pickup_name;
                                    from = receiver_phone;
                                    app.setPackage_id(id);

                                    amout_cost = Integer.parseInt(client_cost);

                                    destination_location.setText(dropoff_name + ", kenya");
                                    source_location.setText(pickup_name + ", kenya");
                                    txtFare.setText("Ksh " + rider_amount);
                                    txtcustomer_name.setText("Receiver phone: " + receiver_phone + "\nDist: " + distance + "km\nSender: " + sendername);

                                    pacakge = title;

                                    if (accepted.equalsIgnoreCase("Assigned")) {
                                        profile.setTitle("         Ksh " + rider_amount);


                                        // prepare intent which is triggered if the

                                        NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this);
                                        builder.setSmallIcon(android.R.drawable.ic_dialog_alert);
                                        Intent intent = new Intent(MainActivity.this, MainActivity.class);
                                        PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this, 0, intent, 0);
                                        builder.setContentIntent(pendingIntent);
                                        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
                                        builder.setContentTitle("Zap Delivery Request");
                                        builder.setContentText("You have an active delivery request.");
                                        builder.setSubText("Tap to view the request.");

                                        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

                                        // Will display the notification in the notification bar
                                        notificationManager.notify(1, builder.build());

                                        assignedDialog();

                                        handler.removeMessages(0);


                                    } else if (accepted.equalsIgnoreCase("accepted")) {

                                        profile.setTitle("         Ksh " + rider_amount);

                                        reprint = true;
                                        getPosition();


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


                            } else {
                                btnEndRide.setVisibility(View.GONE);
                                ll_straight.setVisibility(View.GONE);
                                ll_to_from.setVisibility(View.GONE);
                                ll_call.setVisibility(View.GONE);
                                ll_buttons.setVisibility(View.GONE);

                                ll_navigation.setVisibility(View.GONE);
                                profile.setTitle("         Waiting for nearest request...");


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
        ) {


            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");

                String auth = "Bearer " + app.getAuttoken();
                headers.put("Authorization", auth);
                return headers;
            }


        };

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
    private void pulldata() {

        Log.e("url", Urls.Delivery + "" + app.getUserid().toString());

        StringRequest stringRequest = new StringRequest(Request.Method.GET, Urls.Delivery + "" + app.getUserid(),
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {


                        try {

                            Log.e("Package", response.toString());

                            JSONArray jsonArray = new JSONArray(response);

                            if (jsonArray.length() > 0) {
                                spin_kit.setVisibility(View.GONE);

                                btnEndRide.setVisibility(View.VISIBLE);
                                ll_straight.setVisibility(View.VISIBLE);
                                ll_to_from.setVisibility(View.VISIBLE);
                                ll_call.setVisibility(View.VISIBLE);
                                ll_buttons.setVisibility(View.VISIBLE);

                                for (int i = 0; i < jsonArray.length(); i++) {

                                    JSONObject obj = jsonArray.getJSONObject(i);
                                    String id = obj.getString("id");

                                    String pickup_name = obj.getString("pickup_name");
                                    String dropoff_name = obj.getString("dropoff_name");
                                    String title = obj.getString("title");

                                    String distance = obj.getString("distance");
                                    String cost = obj.getString("cost");
                                    String receiver_phone = obj.getString("receiver_phone");
                                    String receiver_name = obj.getString("receiver_name");
                                    String sendername = obj.getString("sendername");

                                    String rider_amount = obj.getString("rider_amount");
                                    String client_cost = obj.getString("client_cost");

                                    accepted = obj.getString("status");
                                    app.setPackage_from(pickup_name);
                                    app.setPackage_to(dropoff_name);

                                    app.setIs_cooperate(obj.getBoolean("is_cooperate"));

                                    to = pickup_name;
                                    from = receiver_phone;
                                    app.setPackage_id(id);

                                    amout_cost = Integer.parseInt(client_cost);

                                    destination_location.setText(dropoff_name + ", kenya");
                                    source_location.setText(pickup_name + ", kenya");
                                    txtFare.setText("Ksh " + rider_amount);
                                    txtcustomer_name.setText("Receiver phone: " + receiver_phone + "\nDist: " + distance + "km\nSender: " + sendername);

                                    pacakge = title;

                                    if (accepted.equalsIgnoreCase("Assigned")) {
                                        profile.setTitle("         Ksh " + rider_amount);
                                        // prepare intent which is triggered if the

                                        handler.removeMessages(0);


                                    } else if (accepted.equalsIgnoreCase("accepted")) {

                                        profile.setTitle("         Ksh " + rider_amount);

                                        reprint = true;
                                        getPosition();


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


                            } else {
                                btnEndRide.setVisibility(View.GONE);
                                ll_straight.setVisibility(View.GONE);
                                ll_to_from.setVisibility(View.GONE);
                                ll_call.setVisibility(View.GONE);
                                ll_buttons.setVisibility(View.GONE);

                                ll_navigation.setVisibility(View.GONE);
                                profile.setTitle("         Waiting for nearest request...");


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
        ) {


            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");

                String auth = "Bearer " + app.getAuttoken();
                headers.put("Authorization", auth);
                return headers;
            }


        };

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


    public static String getAddressFromLatLng(Context context, LatLng latLng) {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(context, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            return addresses.get(0).getAddressLine(0);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
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


    private void sendRequestToPickup(String toPickupname) {

        String origin = toPickupname.toString();
        String destination = source_location.getText().toString();

        Log.e("Origin", origin);
        Log.e("Destination", destination);


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

    private void sendRequest() {
        String origin = source_location.getText().toString();
        String destination = destination_location.getText().toString();
        Log.e("Destination", destination);


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
            Log.e("direction erro", e.toString());

        }
    }


    public void onLine(String rider_id) {
        RequestQueue queue = Volley.newRequestQueue(this); // this = context

        String url = Urls.onlineRequest + "/" + rider_id;
        Log.d("Accepted", url);

        StringRequest postRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.d("Accepted", response);
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


    public void offLine(String rider_id) {
        RequestQueue queue = Volley.newRequestQueue(this); // this = context

        String url = Urls.offlineRequest + "/" + rider_id;
        Log.d("Accepted", url);

        StringRequest postRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.d("Accepted", response);
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


    public void acceptPackage(String package_id, String rider_id) {
        RequestQueue queue = Volley.newRequestQueue(this); // this = context

        String url = Urls.acceptrequest + "/" + package_id + "/" + rider_id;
        Log.d("Accepted", url);

        StringRequest postRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.d("Accepted", response);
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


    public void getPosition() {
        RequestQueue queue = Volley.newRequestQueue(this); // this = context

        String url = Urls.rider_location + "" + app.getUserid();

        Log.e("-----lo url", url);

        StringRequest postRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONArray jsonArray = new JSONArray(response);

                            for (int i = 0; i < jsonArray.length(); i++) {

                                JSONObject obj = jsonArray.getJSONObject(i);

                                String[] location = obj.getString("location").split(",");
                                Log.e("-----location", location[0]);

                                String toPickupname = getAddressFromLatLng(MainActivity.this, new LatLng(Double.parseDouble(location[0]), Double.parseDouble(location[1])));
                                sendRequestToPickup(toPickupname);

                                app.setDestination(new LatLng(Double.parseDouble(location[0]), Double.parseDouble(location[1])));

                                Log.e("toPickupname", toPickupname);


                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        // response
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


    public void PickPackage(String package_id, String rider_id) {
        RequestQueue queue = Volley.newRequestQueue(this); // this = context
        Log.e("Picking", "picking");


        String url = Urls.pickrequest + "/" + package_id + "/" + rider_id;
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
            progressDialog.dismiss();

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


            txtcustomer_name.setText("Distance: " + route.distance.text + " Time: " + route.duration.text);


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
                        Toast.makeText(getApplicationContext(), "Back Online", Toast.LENGTH_SHORT).show();

                        labeledSwitch.isOn();
                        labeledSwitch.setOn(true);
                        onLine(app.getUserid());

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

        Log.e("param: ", new JSONObject(params).toString());


        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, Urls.FCM_URL, new JSONObject(params),
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

    private void createWebSocketClient(String username, String address) {
        URI uri;


        try {
            uri = new URI("ws://18.159.15.240:8001/ws/notification/" + username);

            Log.e("url", uri.toString());

        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        webSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen() {
                System.out.println("onOpen");
//                webSocketClient.send("Hello, World!");
                Log.e("Websocket", "Opened");

            }

            @Override
            public void onTextReceived(String message) {
                System.out.println("onTextReceived");

                Log.e("Request", "Request received");
                Log.e("Request", message);


                if (asigned = false) {


                    MainActivity.this.runOnUiThread(() -> {
                        Log.d("UI thread", "I am the UI thread");

                        Toast.makeText(MainActivity.this, "Request received", Toast.LENGTH_SHORT).show();
                        addNotification();
//                        checkAssigned();


                    });



                } else {
                    Log.e("Do Nothing", "............................");
                    handler.removeMessages(0);

                }


            }

            @Override
            public void onBinaryReceived(byte[] data) {
                System.out.println("onBinaryReceived");
                Log.e("Request", "onBinaryReceived received");

            }

            @Override
            public void onPingReceived(byte[] data) {
                System.out.println("onPingReceived");
                Log.e("Request", "ping received");

            }

            @Override
            public void onPongReceived(byte[] data) {
                System.out.println("onPongReceived");
            }

            @Override
            public void onException(Exception e) {
                System.out.println(e.getMessage());
                Log.e("wserror", e.getMessage());

            }

            @Override
            public void onCloseReceived() {
                System.out.println("onCloseReceived");
            }
        };


//        webSocketClient.setConnectTimeout(10000);
//        webSocketClient.setReadTimeout(60000);
//        webSocketClient.addHeader("Origin", "http://developer.example.com");
        webSocketClient.enableAutomaticReconnection(5000);
        webSocketClient.connect();
    }

    private void addNotification() {

        String CHANNEL_ID = "MESSAGE";
        String CHANNEL_NAME = "MESSAGE";
        Intent intent = new Intent(this, MainActivity.class);

        NotificationManagerCompat manager = NotificationManagerCompat.from(MainActivity.this);
        PendingIntent pendingIntent = TaskStackBuilder.create(MainActivity.this)
                .addNextIntent(intent)
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);
        }

        Notification notification = new NotificationCompat.Builder(MainActivity.this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("New Request")
                .setStyle(new NotificationCompat.BigTextStyle())
                .setDefaults(DEFAULT_SOUND | DEFAULT_VIBRATE)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentText("You have been assigned a package\nTap to view details")
                .setContentIntent(pendingIntent)

                .build();
        manager.notify(getRandomNumber(), notification); //
    }

    private static int getRandomNumber() {
        Date dd = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("mmssSS");
        String s = ft.format(dd);
        return Integer.parseInt(s);
    }

}
