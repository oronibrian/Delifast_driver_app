package com.zap.zapdriver

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.zap.zapdriver.API.Urls.register
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_join.*
import org.json.JSONObject

class JoinActivity : AppCompatActivity() {

    private var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join)


        checkBox.setOnCheckedChangeListener { _, isChecked ->
            btn_register.isEnabled = isChecked
        }
        btn_register.setOnClickListener {

            if (reg_password_et.text.toString() != reg_pass_confirm.text.toString()) {
                Toasty.error(
                    applicationContext, "Password do not match",
                    Toast.LENGTH_SHORT, true
                ).show();

            } else {
                Registeraccount()
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this@JoinActivity, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun Registeraccount() {

        progressDialog = ProgressDialog.show(this, "Please wait", "Signing up....", true)

        val params = HashMap<String, String>()
        params["email"] = reg_name_et.text.toString()
        params["username"] = reg_name_et.text.toString()
        params["password"] = reg_password_et.text.toString()
        params["first_name"] = reg_firstname_et.text.toString()
        params["last_name"] = reg_last_name.text.toString()
        params["client_phone"] = reg_client_phone.text.toString()
        params["is_active"] = true.toString()
        params["is_staff"] = true.toString()


        val jsonObject = JSONObject(params as Map<*, *>)

        val url = register



        Log.e("params", params.toString())
        // Volley post request with parameters
        val request = JsonObjectRequest(
            Request.Method.POST,
            url,
            jsonObject,
            { response ->
                // Process the json
                progressDialog!!.dismiss()


                try {
                    var res = "Response: $response"
                    Log.e("response", response.toString())


                    Toasty.success(this@JoinActivity, "Success ", Toast.LENGTH_SHORT, true)
                        .show();


                    progressDialog!!.dismiss()


                    val intent = Intent(this@JoinActivity, RiderRegSuccessActivity::class.java)
                    startActivity(intent)
                    finish()


                } catch (e: Exception) {
                    var exc = "Exception: $e"
                    Log.e("response", e.toString())
                    progressDialog!!.dismiss()

                    Toasty.error(this@JoinActivity, "Error: " + exc, Toast.LENGTH_SHORT, true)
                        .show();


                }

            },
            {
                // Error in request
                var err = "Volley error: $it"
//                Toast.makeText(this@LoginActivity, err, Toast.LENGTH_SHORT).show()
                progressDialog!!.dismiss()

                Toasty.error(
                    applicationContext, "Wrong credentials or check connection",
                    Toast.LENGTH_SHORT, true
                ).show();


            })


        // Volley request policy, only one time request to avoid duplicate transaction
        request.retryPolicy = DefaultRetryPolicy(
            DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
            // 0 means no retry
            3, // DefaultRetryPolicy.DEFAULT_MAX_RETRIES = 2
            1f // DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        val requestQueue = Volley.newRequestQueue(this)

        requestQueue.add<JSONObject>(request)

        // Add the volley post request to the request queue
//        VolleySingleton.getInstance(this).addToRequestQueue(request)

    }


}