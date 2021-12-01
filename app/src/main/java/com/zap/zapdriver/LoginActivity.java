package com.zap.zapdriver;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.zap.zapdriver.API.Urls;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    Button btnlogn;

    ProgressDialog progressDoalog;
    EditText edittextusername, editextpassword;
    TextView txt_join_us;
    String ACCESS_TOKEN;

    DriverApplication app;


    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (DriverApplication) getApplicationContext();

        setContentView(R.layout.activity_login);

        progressDoalog = new ProgressDialog(this);


        progressDoalog.setMax(10);
        progressDoalog.setMessage("Login in....");
        progressDoalog.setTitle("Driver Login");
        progressDoalog.setProgressStyle(ProgressDialog.STYLE_SPINNER);


        btnlogn = findViewById(R.id.save_button);

        editextpassword = findViewById(R.id.editextpassword);

        edittextusername = findViewById(R.id.edittextusername);

        txt_join_us = findViewById(R.id.txt_join_us);

        btnlogn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Request_token();
//                Authorize_token();

            }

        });


        txt_join_us.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, JoinActivity.class);
                startActivity(intent);
                finish();

            }

        });


        TextView imageLogo = findViewById(R.id.forgotPassword);
        imageLogo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                String url = "https://zaplogistics.co.ke/accounts/password_reset/";

                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });
    }


    private void Request_token() {


        final String email = edittextusername.getText().toString();
        final String password = editextpassword.getText().toString();


        //validating inputs
        if (TextUtils.isEmpty(email)) {
            edittextusername.setError("Please enter your email address or phone");
            edittextusername.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            editextpassword.setError("Please enter your password");
            editextpassword.requestFocus();
            return;
        } else {

            progressDoalog.show();


            StringRequest postRequest = new StringRequest(Request.Method.POST, Urls.Auth,
                    response -> {
                        // response
                        Log.e("Response", response);

                        parseData(response);

                        progressDoalog.dismiss();
                    },
                    error -> {
                        // error
                        Log.d("Error.Response", error.toString());


                        progressDoalog.dismiss();

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
    }


    public void parseData(String response) {

        try {
            JSONObject data = new JSONObject(response);


            if (!data.getString("access").isEmpty()) {

                String token = data.getString("access");


                app.setToken_key(token);

                String id = data.getString("id");

                String name = data.getString("first_name");
                String username = data.getString("username");
                String last_name = data.getString("last_name");
                String email = data.getString("username");
                String phone_no = data.getString("phone_no");


                app.setFirstName(name);
                app.setLast_name(last_name + "\n" + email);


                app.setUsername(username);
                app.setUserid(id);
                app.setPhone_no(phone_no);

                app.setPassword(editextpassword.getText().toString());

                SharedPreferences preferences = getSharedPreferences("PREFS_NAME",
                        Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("username", username);

                editor.putString("name", name);
                editor.putString("last_name", last_name);
                editor.putString("email", email);
                editor.putString("phone_no", phone_no);

                editor.putString("password", editextpassword.getText().toString());
                editor.putString("id", id);
                editor.apply();


                onLine(id);

                Log.e("name", name);
            }


            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();

        } catch (JSONException jsonException) {
            jsonException.printStackTrace();
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


}
