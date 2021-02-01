package com.zap.zapdriver;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

public class My_Deliveries extends AppCompatActivity {
    ArrayList<PackageModel> videoModelArrayList = new ArrayList<>();
    private RecyclerView rvVideos, rlAudio;
    private PackageAdapter mVideoNewAdapter;
    private NestedScrollView scrollView;
    DriverApplication app;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my__deliveries);

        rvVideos = findViewById(R.id.rv_videos);
        scrollView = findViewById(R.id.nested_scroll_view);


        scrollView.setSmoothScrollingEnabled(true);
        rvVideos.setNestedScrollingEnabled(false);
        app = (DriverApplication) getApplicationContext();


        getPackages();


    }

    private void getPackages() {

        final ProgressDialog progressDialog = new ProgressDialog(My_Deliveries.this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();

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
                                Log.e("lis", jsonArray.toString(4));


                                if (jsonArray.length() > 0) {

                                    passModuleData(jsonArray);


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

            videoModel.setCost(jsonObject.optString("cost"));
            videoModel.setPay_now(jsonObject.optBoolean("pay_now"));


            videoModelArrayList.add(videoModel);


        }


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