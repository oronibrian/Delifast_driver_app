package com.zap.zapdriver;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.zap.zapdriver.API.Urls;
import com.zap.zapdriver.Adapter.PackageAdapter;
import com.zap.zapdriver.Model.PackageModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class My_Deliveries extends AppCompatActivity {
    ArrayList<PackageModel> videoModelArrayList = new ArrayList<>();
    private RecyclerView rvVideos, rlAudio;
    private PackageAdapter mVideoNewAdapter;
    private NestedScrollView scrollView;
    DriverApplication app;
    Boolean asigned = false;

    TextView txt_rider_amount, txt_empty;
    int total_sum = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my__deliveries);

        rvVideos = findViewById(R.id.rv_packages);
//        scrollView = findViewById(R.id.nested_scroll_view);
//
//
//        scrollView.setSmoothScrollingEnabled(true);
//        rvVideos.setNestedScrollingEnabled(true);
        app = (DriverApplication) getApplicationContext();
        txt_rider_amount = findViewById(R.id.txt_rider_amount);
        txt_empty = findViewById(R.id.txt_empty);


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

                            getPackages();
                            getfinancesummery();


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

    private void getfinancesummery() {

        Log.e("url", Urls.finace + "" + app.getUserid().toString());


        total_sum = 0;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, Urls.finace + "" + app.getUserid(),
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {


                        try {

                            Log.e("Package", response.toString());

                            JSONArray jsonArray = new JSONArray(response);

                            for (int i = 0; i < jsonArray.length(); i++) {

                                JSONObject obj = jsonArray.getJSONObject(i);

                                String total_rider_amount = obj.getString("balance");

//                                total_sum = total_sum + Integer.parseInt(total_rider_amount);

                            }
//                            txt_rider_amount.setText("" + total_sum);


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

    private void getPackages() {

        final ProgressDialog progressDialog = new ProgressDialog(My_Deliveries.this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        videoModelArrayList.clear();

        //if everything is fine
        StringRequest postRequest = new StringRequest(Request.Method.GET, Urls.driverassignedpackages + "" + app.getUserid(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {


                        String message = "";
                        progressDialog.dismiss();
                        try {

                            JSONArray jsonArray;
                            try {
                                jsonArray = new JSONArray(response);
                                Log.e("listdata", jsonArray.toString(4));


                                if (jsonArray.length() > 0) {

                                    passModuleData(jsonArray);


                                } else {
                                    txt_empty.setVisibility(View.VISIBLE);
                                    rvVideos.setVisibility(View.GONE);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } catch (Exception e) {
                            progressDialog.dismiss();
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        // Toast.makeText(VideoListNewActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
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

        requestQueue.add(postRequest);
    }


    private void passModuleData(JSONArray data) {
        for (int i = 0; i < data.length(); i++) {
            JSONObject jsonObject = data.optJSONObject(i);
            PackageModel videoModel = new PackageModel();

            videoModel.setDate(jsonObject.optString("date"));
            videoModel.setTitle(jsonObject.optString("title"));
            videoModel.setPickup_name(jsonObject.optString("pickup_name"));
            videoModel.setDropoff_name(jsonObject.optString("dropoff_name"));
            videoModel.setDistance(jsonObject.optString("distance"));

            videoModel.setStatus(jsonObject.optString("status"));

            videoModel.setCost(jsonObject.optString("rider_amount"));
            videoModel.setPay_now(jsonObject.optBoolean("pay_now"));

            videoModelArrayList.add(videoModel);


            if (jsonObject.optString("status").equals("delivered")) {

                total_sum = total_sum + Integer.parseInt(jsonObject.optString("rider_amount"));
            }

        }
        txt_rider_amount.setText("" + total_sum);


//        if (videoModelArrayList.size() > 0) {
//            tv_error.setVisibility(View.GONE);
//        } else {
//            tv_error.setVisibility(View.VISIBLE);
//        }
        GridLayoutManager gridLayoutManager = new GridLayoutManager(My_Deliveries.this, 1, LinearLayoutManager.VERTICAL, false);
        rvVideos.setLayoutManager(gridLayoutManager); // set LayoutManager to RecyclerView
        mVideoNewAdapter = new PackageAdapter(My_Deliveries.this, videoModelArrayList);
        rvVideos.setAdapter(mVideoNewAdapter);


    }
}