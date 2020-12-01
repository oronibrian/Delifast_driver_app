package com.zap.zapdriver;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;
import com.zap.zapdriver.API.Urls;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    MaterialButton btnlogn;

    ProgressDialog progressDoalog;
    EditText edittextusername,editextpassword;

    String ACCESS_TOKEN;

    DriverApplication app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app=(DriverApplication) getApplicationContext();

        setContentView(R.layout.activity_login);

        progressDoalog = new ProgressDialog(this);


        progressDoalog.setMax(10);
        progressDoalog.setMessage("Login in....");
        progressDoalog.setTitle("Driver Login");
        progressDoalog.setProgressStyle(ProgressDialog.STYLE_SPINNER);


        btnlogn =findViewById(R.id.save_button);

        editextpassword=findViewById(R.id.editextpassword);

        edittextusername=findViewById(R.id.edittextusername);

        btnlogn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Request_token();
//                Authorize_token();

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
                protected Map<String, String> getParams()
                {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("username", email);
                    params.put("email", "");
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
            JSONObject jsonObject = new JSONObject(response);

            JSONObject data = jsonObject.getJSONObject("user");

            if (!jsonObject.getString("token").isEmpty()) {

                String  token = jsonObject.getString("token");


                app.setToken_key(token);


                for (int i = 0; i < data.length(); i++) {

                    String id = data.getString("pk");

                    String name = data.getString("first_name");
                    String username = data.getString("username");

                    app.setUsername(username);
                    app.setUserid(id);


                    SharedPreferences preferences = getSharedPreferences("PREFS_NAME",
                            Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("username", username);
                    editor.putString("password", editextpassword.getText().toString());
                    editor.putString("id", id);
                    editor.apply();




                    Log.e("name",name);
                }


                Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                startActivity(intent);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void Authorize_token() {


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
                    new Response.Listener<String>()
                    {
                        @Override
                        public void onResponse(String response) {
                            // response
                            Log.e("Response", response);


                            startActivity(new Intent(getApplicationContext(),MainActivity.class));
                            finish();
                            progressDoalog.dismiss();
                        }
                    },
                    new Response.ErrorListener()
                    {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // error
                            Log.d("Error.Response", error.toString());

                            progressDoalog.dismiss();

                        }
                    }
            ) {
                @Override
                protected Map<String, String> getParams()
                {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("username", email);
                    params.put("email", "");
                    params.put("password", password);

                    return params;
                }


                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("Content-Type", "application/x-www-form-urlencoded");
                    params.put("token", ACCESS_TOKEN);

                    return params;
                }
            };

            RequestQueue requestQueue = Volley.newRequestQueue(this);

            requestQueue.add(postRequest);

        }
    }



}
