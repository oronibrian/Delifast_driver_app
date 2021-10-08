package com.zap.zapdriver;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.google.zxing.Result;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.zap.zapdriver.API.Urls;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScanReturnActivity extends AppCompatActivity {
    private CodeScanner mCodeScanner;
    Button btnsubmit;
    DriverApplication app;

    TextView scanText;
    ImageView img_done;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_return);
        btnsubmit = findViewById(R.id.btnsubmit);

        app = (DriverApplication) getApplicationContext();

        btnsubmit = findViewById(R.id.btnsubmit);
        img_done = findViewById(R.id.img_done);

        scanText = findViewById(R.id.scanText);


        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.CAMERA
                ).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {/* ... */}

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {/* ... */}
        }).check();

        CodeScannerView scannerView = findViewById(R.id.scanner_view);
        mCodeScanner = new CodeScanner(this, scannerView);
        mCodeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull final Result result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ScanReturnActivity.this, result.getText(), Toast.LENGTH_SHORT).show();
                        SearchPackage(result.getText());
                        img_done.setVisibility(View.VISIBLE);
                    }
                });
            }
        });
        scannerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCodeScanner.startPreview();
            }
        });


        btnsubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ReturnPackage(app.getPackage_id(), app.getUserid());
            }
        });
    }

    protected void onResume() {
        super.onResume();
        mCodeScanner.startPreview();
    }

    @Override
    protected void onPause() {
        mCodeScanner.releaseResources();
        super.onPause();
    }


    public void SearchPackage(String bar_code) {
        RequestQueue queue = Volley.newRequestQueue(this); // this = context
        Log.e("returnrequest", "returnrequest");


        String url = Urls.search_by_qr + "" + bar_code;
        Log.e("search", url);

        StringRequest postRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.d("Response", response);
                        try {

                            JSONArray jsonArray;
                            try {
                                jsonArray = new JSONArray(response);


                                if (jsonArray.length() > 0) {

                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        JSONObject jsonObject = jsonArray.optJSONObject(i);

                                        String driver_assigned = jsonObject.getString("driver_assigned");
                                        if (driver_assigned.equals(app.getUserid())) {
                                            scanText.setText(jsonObject.getString("pickup_name") + "\n" + jsonObject.getString("dropoff_name"));

                                            app.setPackage_id(jsonObject.getString("id"));

                                        } else if (jsonObject.getString("status").equals("delivered")) {
                                            scanText.setText("Package was delivered");
                                        } else {
                                            scanText.setText("Not the rider assigned");
                                            btnsubmit.setVisibility(View.GONE);

                                        }


                                    }

                                    Log.e("data", jsonArray.toString(4));


                                } else {
                                    btnsubmit.setVisibility(View.GONE);
                                    scanText.setText("No Such A package");

                                }


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } catch (Exception e) {
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
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");

                String auth = "Bearer " + app.getAuttoken();
                headers.put("Authorization", auth);
                return headers;
            }


        };

        queue.add(postRequest);
    }


    public void ReturnPackage(String package_id, String rider_id) {
        RequestQueue queue = Volley.newRequestQueue(this); // this = context
        Log.e("returnrequest", "returnrequest");


        String url = Urls.returnrequest + "/" + package_id + "/" + rider_id;
        Log.e("returnurl", url);

        StringRequest postRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.d("Response", response);

                        Toast.makeText(ScanReturnActivity.this, "Return Confirmed", Toast.LENGTH_SHORT).show();

                        startActivity(new Intent(ScanReturnActivity.this, MainActivity.class));
                        finish();
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