package com.zap.zapdriver;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.NotificationCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.zap.zapdriver.API.Urls;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SignatureActivity extends AppCompatActivity {
    private Button mClearButton;
    private Button mSaveButton, btn_return, btncall_owner;
    DriverApplication app;
    TextView txt_from;
    EditText receiver_phone, receiver_name;
    CheckBox receiver, no_receiver;
    private RadioGroup radioSexGroup;
    private RadioButton radioSexButton;
    private boolean receiver_available = true;

    AlertDialog mpesaalertDialog, paybillalertDialog;
    CardView card_details;
    String number = "";
    String cash_payment = "mpesa";
    String email, pass, user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signature);

//        mSignaturePad = (SignaturePad) findViewById(R.id.signature_pad);
        app = (DriverApplication) getApplicationContext();

//        mClearButton = (Button) findViewById(R.id.clear_button);
        mSaveButton = (Button) findViewById(R.id.save_button);
        txt_from = findViewById(R.id.txt_from);
        receiver_phone = findViewById(R.id.receiver_phone);
        receiver_name = findViewById(R.id.receiver_name);
        btn_return = findViewById(R.id.btn_return);
        btncall_owner = findViewById(R.id.btncall_owner);

        card_details = findViewById(R.id.card_details);
        card_details.setVisibility(View.VISIBLE);
        mSaveButton.setVisibility(View.VISIBLE);
        SharedPreferences sharedPreferences = getSharedPreferences("PREFS_NAME", Context.MODE_PRIVATE);
        user = sharedPreferences.getString("username", "");
        String id = sharedPreferences.getString("id", "");


        pass = sharedPreferences.getString("password", "");

        String name = sharedPreferences.getString("name", "");

        String last_name = sharedPreferences.getString("last_name", "");
        email = sharedPreferences.getString("email", "");
        String phone_no = sharedPreferences.getString("phone_no", "");



        if( getIntent().hasExtra("cash_payment")) {
            cash_payment = getIntent().getStringExtra("cash_payment");

            if (cash_payment.equals("")) {
                Log.e("method: ", "empty method");

            } else {
                Toast.makeText(SignatureActivity.this, cash_payment, Toast.LENGTH_SHORT).show();

            }
        }


        if (user.equals("")) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();

        } else {
            app.setUsername(email);
            app.setUserid(id);
            app.setPassword(pass);
            app.setPhone_no(phone_no);

            Request_token(user, pass);
        }


        radioSexGroup = (RadioGroup) findViewById(R.id.radioGroup);
        radioSexGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // find which radio button is selected
                if (checkedId == R.id.radioButton) {
                    card_details.setVisibility(View.VISIBLE);
                    mSaveButton.setVisibility(View.VISIBLE);
                    receiver_available = true;

                } else {
                    card_details.setVisibility(View.GONE);
                    mSaveButton.setVisibility(View.VISIBLE);
                    receiver_available = false;

                }
            }

        });


        if (user == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();

        } else {
            app.setUsername(user);
            app.setUserid(id);
        }


        Log.e("Name: ", app.getUsername());

        checkAssigned();


//        mSignaturePad.setOnSignedListener(new SignaturePad.OnSignedListener() {
//
//            @Override
//            public void onStartSigning() {
//                //Event triggered when the pad is touched
//
//                Toast.makeText(SignatureActivity.this, "Signing", Toast.LENGTH_SHORT).show();
//
//            }

//            @Override
//            public void onSigned() {
//                //Event triggered when the pad is signed
//
//                mSaveButton.setEnabled(true);
//                mClearButton.setEnabled(true);
//            }
//
//            @Override
//            public void onClear() {
//                //Event triggered when the pad is cleared
//            }
//        });


//        mClearButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                mSignaturePad.clear();
//            }
//        });

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (receiver_available) {


                    String strUserName = receiver_phone.getText().toString();

                    if (strUserName.trim().equals("")) {
                        Toast.makeText(SignatureActivity.this, "Phone number required ", Toast.LENGTH_SHORT).show();
                        return;
                    } else {

                        if (strUserName.startsWith("0")) {
                            strUserName = strUserName.replaceFirst("0", "+254");

                            generateCode(strUserName);
                        } else {
                            generateCode(strUserName);

                        }

                    }

                } else {


//                    update_package_Complete(app.getPackage_id(), app.getUserid(), app.getPhone_no());
                    update_package_Complete(app.getPackage_id(), app.getUserid(), receiver_phone.getText().toString(), receiver_name.getText().toString());


                }


            }
        });


        btn_return.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), ScanReturnActivity.class));
                finish();

            }
        });


        btncall_owner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + number));
                startActivity(callIntent);
            }
        });
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

    private void ComfirmCode() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(SignatureActivity.this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.custom_complete, null);
        EditText code = dialogView.findViewById(R.id.editextcode);

        dialogBuilder.setView(dialogView);


        Button btncomplete = dialogView.findViewById(R.id.btncon);

        btncomplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                update_package_Complete(app.getPackage_id(), app.getUserid(), receiver_phone.getText().toString(), receiver_name.getText().toString());


            }
        });

        paybillalertDialog = dialogBuilder.create();
        paybillalertDialog.show();


    }

    private void checkAssigned() {

        Log.e("url", Urls.Delivery + "" + app.getUserid().toString());

        StringRequest stringRequest = new StringRequest(Request.Method.GET, Urls.Delivery + "" + app.getUserid(),
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                        Log.e("response", response.toString());

                        try {


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


                                    String phn = obj.getString("sender_phone");


                                    number = "+254" + phn.substring(1);


                                    app.setPackage_id(id);

                                    Log.e("number", number.toString());


                                    txt_from.setText("Package: " + title + "\n Receiver: " + receiver_phone + " \nDistance: " + distance + "km");


                                }


                            } else {

                                Log.e("Empty", "Empty");


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


    public void generateCode(String phone) {


        HashMap<String, String> params = new HashMap<String, String>();
        params.put("phones", phone);
        params.put("is_confirmed", "false");
        params.put("package", app.getPackage_id());

        Log.e("generate", params.toString());

        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, Urls.generate_receiver_code, new JSONObject(params),

                (JSONObject response) -> {
                    // response
                    Log.e("Response", response.toString());

                    Toast.makeText(SignatureActivity.this, "Code Generated", Toast.LENGTH_SHORT).show();
                    ComfirmCode();

                },
                error -> {
                    // error
                    Log.e("Error.Response", error.toString());

                    Toast.makeText(SignatureActivity.this, "" + error.toString(), Toast.LENGTH_SHORT).show();

                    Request_token(user, pass);


                }
        ) {


            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");

                String auth = "Bearer " + app.getAuttoken();
                headers.put("Authorization", auth);
                return headers;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);

        requestQueue.add(postRequest);
    }


    public void update_package_Complete(String package_id, String rider_id, String recevere_phone_id, String name) {
        RequestQueue queue = Volley.newRequestQueue(this); // this = context

        String url = Urls.complete_delivery_request + "/" + package_id + "/" + rider_id + "/" + recevere_phone_id + "/" + name + "/" + cash_payment;
        Log.e("URL", url);

        StringRequest postRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // response
                        JSONObject jsonArray = null;
                        try {
                            jsonArray = new JSONObject(response);

                            String paid = jsonArray.getString("msg");

                            Log.e("Response", response);
                            Toast.makeText(SignatureActivity.this, "" + paid, Toast.LENGTH_SHORT).show();
                            sendNotification("Amount");

                            if (paid.equals("Success")) {
                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                finish();
                            }


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
        );

        queue.add(postRequest);
    }


    private void sendNotification(String amount) {
        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(notificationIntent);
        PendingIntent notificationPendingIntent = stackBuilder.getPendingIntent(Integer.parseInt("0"), PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
        bigText.bigText("Amount Credited");
        bigText.setBigContentTitle("Amount Credited");

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.zap)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.zap))
                .setColor(getResources().getColor(R.color.black))
                .setContentTitle(("Amount"))
                .setContentIntent(notificationPendingIntent)
                .setContentText("Account: credited ")
                .setDefaults(Notification.DEFAULT_ALL)
                .setStyle(bigText);

        builder.setAutoCancel(true);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, builder.build());
    }
}
