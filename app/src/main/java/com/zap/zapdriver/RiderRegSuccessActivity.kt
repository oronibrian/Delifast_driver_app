package com.zap.zapdriver

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_rider_reg_success.*

class RiderRegSuccessActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rider_reg_success)

        btn_login.setOnClickListener {
            val intent = Intent(this@RiderRegSuccessActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}