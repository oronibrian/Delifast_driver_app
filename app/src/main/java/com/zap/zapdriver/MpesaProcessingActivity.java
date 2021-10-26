package com.zap.zapdriver;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.zap.zapdriver.API.Urls;

import org.json.JSONException;
import org.json.JSONObject;

import timber.log.Timber;

public class MpesaProcessingActivity extends AppCompatActivity {

    DriverApplication app;

    ProgressDialog progressDialog ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mpesa_processing);

        app = (DriverApplication) getApplicationContext();

        progressDialog = new ProgressDialog(this);



        progressDialog.setMessage("Please wait..."); // Setting Message
        progressDialog.setTitle("Confirming Payment"); // Setting Title
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); // Progress Dialog Style Spinner
        progressDialog.show(); // Display Progress Dialog
        progressDialog.setCancelable(false);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                // Actions to do after 10 seconds
                checkPaid();
                handler.removeCallbacksAndMessages(null);

            }
        }, 20000);


    }

    private void stkPushMethod() {

        RequestQueue queue = Volley.newRequestQueue(this); // this = context


        String url = Urls.mpesacallback;
        Log.e("callback--url", url);

        StringRequest putRequest = new StringRequest(Request.Method.GET, url,

                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.e("mpesa", response);


//                        try {
//                            JSONObject data = response;
//
//                            Toast.makeText(MpesaProcessingActivity.this, data.getString("msg"), Toast.LENGTH_SHORT).show();
//                            startActivity(new Intent(getApplicationContext(), SignatureActivity.class));
//
//
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//
//                        }


                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.e("Error.Response", error.toString());

                    }
                }
        );

        putRequest.setRetryPolicy(new DefaultRetryPolicy(
                5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(putRequest);


    }


    private void checkPaid() {

        Timber.e(Urls.checkPaid + "" + app.getPackage_id());
        Log.e("mpesa_check", Urls.checkPaid + "" + app.getPackage_id());


        StringRequest stringRequest = new StringRequest(Request.Method.GET, Urls.checkPaid + "" + app.getPackage_id(),
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                        Log.e("mp", response);


                        try {

                            Log.e("mp", response);

                            JSONObject jsonArray = new JSONObject(response);

                            String paid = jsonArray.getString("msg");
                            Log.e("paid", paid);

                            if(paid.equalsIgnoreCase("True")) {

                                Intent i = new Intent(getApplicationContext(), SignatureActivity.class);
                                i.putExtra("cash_payment", "mpesa");
                                app.setPayment_method("mpesa");
                                startActivity(i);
                                finish();


                            } else {
                                Toast.makeText(getApplicationContext(), "Payment has not been received", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                finish();



                            }
                            progressDialog.dismiss();


                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e("mperror", e.toString());
                            progressDialog.dismiss();



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


}