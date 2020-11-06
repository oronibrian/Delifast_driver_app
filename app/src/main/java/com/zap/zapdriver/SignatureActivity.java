package com.zap.zapdriver;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.github.gcacace.signaturepad.views.SignaturePad;

public class SignatureActivity extends AppCompatActivity {
    SignaturePad mSignaturePad;
    private Button mClearButton;
    private Button mSaveButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signature);

        mSignaturePad = (SignaturePad) findViewById(R.id.signature_pad);

        mClearButton = (Button) findViewById(R.id.clear_button);
        mSaveButton = (Button) findViewById(R.id.save_button);

        mSignaturePad.setOnSignedListener(new SignaturePad.OnSignedListener() {

            @Override
            public void onStartSigning() {
                //Event triggered when the pad is touched

                Toast.makeText(SignatureActivity.this, "Signing", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onSigned() {
                //Event triggered when the pad is signed

                mSaveButton.setEnabled(true);
                mClearButton.setEnabled(true);
            }

            @Override
            public void onClear() {
                //Event triggered when the pad is cleared
            }
        });



        mClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSignaturePad.clear();
            }
        });

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    Toast.makeText(SignatureActivity.this, "Delivery confirmed", Toast.LENGTH_SHORT).show();

            }
        });
    }
}
