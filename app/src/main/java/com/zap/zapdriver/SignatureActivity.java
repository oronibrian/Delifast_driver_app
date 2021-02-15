package com.zap.zapdriver;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.gcacace.signaturepad.views.SignaturePad;
import com.zap.zapdriver.API.Urls;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SignatureActivity extends AppCompatActivity {
    SignaturePad mSignaturePad;
    private Button mClearButton;
    private Button mSaveButton;
    DriverApplication app;
    TextView txt_from;
    EditText receiver_phone;

    AlertDialog mpesaalertDialog, paybillalertDialog;

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

                String strUserName = receiver_phone.getText().toString();
                if (strUserName.trim().equals("")) {
                    Toast.makeText(SignatureActivity.this, "Phone number required ", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    generateCode(strUserName);

                }


            }
        });
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

//                startActivity(new Intent(getApplicationContext(), SignatureActivity.class));

                update_package_Complete(app.getPackage_id(), app.getUserid(), receiver_phone.getText().toString());


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

                                    app.setPackage_id(id);


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
        StringRequest postRequest = new StringRequest(Request.Method.POST, Urls.Auth,
                response -> {
                    // response
                    Log.e("Response", response);

                    Toast.makeText(SignatureActivity.this, "Code Generated", Toast.LENGTH_SHORT).show();
                    ComfirmCode();

                },
                error -> {
                    // error
                    Log.d("Error.Response", error.toString());


                    Toast.makeText(SignatureActivity.this, "" + error.toString(), Toast.LENGTH_SHORT).show();

                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("phones", phone);
                params.put("is_confirmed", "");
                params.put("package", app.getPackage_id());

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


    public void update_package_Complete(String package_id, String rider_id, String recevere_phone_id) {
        RequestQueue queue = Volley.newRequestQueue(this); // this = context

        String url = Urls.complete_delivery_request + "/" + package_id + "/" + rider_id + "/" + recevere_phone_id;
        Log.e("URL", url);

        StringRequest postRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.e("Response", response);
                        Toast.makeText(SignatureActivity.this, "Delivery confirmed", Toast.LENGTH_SHORT).show();

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


}
