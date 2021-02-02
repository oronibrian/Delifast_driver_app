package com.zap.zapdriver;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

public class PrintQRActivity extends AppCompatActivity {
    DriverApplication app;
    TextView content;
    Button acceptBtn;
    // will show the statuses like bluetooth open, close or data sent
    TextView myLabel;

    // will enable user to enter any text to be printed
    EditText myTextbox;

    // android built in classes for bluetooth operations
    BluetoothAdapter mBluetoothAdapter;
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice;
    OutputStream mmOutputStream;
    InputStream mmInputStream;
    Thread workerThread;

    byte[] readBuffer;
    int readBufferPosition;
    volatile boolean stopWorker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print_q_r);
        content = findViewById(R.id.content);

        loadImageFromStorage("");
        app = (DriverApplication) getApplicationContext();
        content.setText(app.getPackage_from() + "\n " + app.getPackage_to());
        acceptBtn = findViewById(R.id.acceptBtn);
        myLabel = (TextView) findViewById(R.id.label);


        findBT();

        acceptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                createPdf();

            }
        });

    }

    private void loadImageFromStorage(String path) {

        try {
            ContextWrapper cw = new ContextWrapper(getApplicationContext());
            File path1 = cw.getDir("picture", Context.MODE_PRIVATE);
            File f = new File(path1, "qr.jpg");
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
            ImageView img = (ImageView) findViewById(R.id.viewImage);
            img.setImageBitmap(b);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }


    private void createPdf() {


        Bitmap b, b2 = null,b3 = null, scled;
        PdfDocument document = new PdfDocument();

        // crate a page description
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(450, 600, 1).create();
        // start a page
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        Paint titlepaint = new Paint();


        try {
            ContextWrapper cw = new ContextWrapper(getApplicationContext());
            File path1 = cw.getDir("picture", Context.MODE_PRIVATE);
            File f = new File(path1, "qr.jpg");
            b2 = BitmapFactory.decodeStream(new FileInputStream(f));
            ImageView img = (ImageView) findViewById(R.id.viewImage);
            img.setImageBitmap(b2);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        try {
            ContextWrapper cw = new ContextWrapper(getApplicationContext());
            File path1 = cw.getDir("picture", Context.MODE_PRIVATE);
            File f = new File(path1, "barcode.jpg");
             b3 = BitmapFactory.decodeStream(new FileInputStream(f));
            ImageView img = (ImageView) findViewById(R.id.viewImage);
            img.setImageBitmap(b3);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        b = BitmapFactory.decodeResource(getResources(), R.drawable.zap);
        scled = Bitmap.createScaledBitmap(b, 130, 90, false);
        canvas.drawBitmap(scled, 160, 0, paint);


        paint.setColor(Color.BLACK);
        titlepaint.setColor(Color.BLACK);
        titlepaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        titlepaint.setTextSize(14);


        canvas.drawText("Zaplogistic ", 180, 100, titlepaint);
        canvas.drawText("" + new SimpleDateFormat("d/M/yyyy", Locale.getDefault()).format(new Date())
                , 180, 135, paint);

        canvas.drawText("|FROM:                                                                       ", 30, 175, titlepaint);

        canvas.drawText("|                                                                       ", 30, 185, paint);
        canvas.drawText("" + app.getPackage_from(), 40, 200, paint);
        canvas.drawText("|                                                                       ", 30, 210, paint);

        canvas.drawText("                                                                      ", 40, 214, paint);

        canvas.drawText("|TO:                                                                       ", 30, 230, titlepaint);
        canvas.drawText("|                                                                       ", 30, 240, paint);

        canvas.drawText("" + app.getPackage_to(), 40, 255, paint);
        canvas.drawText("|                                                                       ", 30, 265, paint);
        canvas.drawText("                                                                      ", 40, 270, paint);

        scled = Bitmap.createScaledBitmap(b2, 130, 130, false);
        canvas.drawBitmap(scled, 30, 290, paint);



        scled = Bitmap.createScaledBitmap(b3, 350, 130, false);
        canvas.drawBitmap(scled, 30, 320, paint);



        document.finishPage(page);


        String targetPdf = new SimpleDateFormat("d-M-yyyy hh:mm", Locale.getDefault()).format(new Date()) + "-qr.pdf";


        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/zap");
        myDir.mkdirs();
        String fileopen = myDir.getAbsolutePath() + "/" + targetPdf;


        File file = new File(myDir, targetPdf);
        if (file.exists()) {

            file.delete();

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Files.deleteIfExists(Paths.get(fileopen));
                }
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        try {


            document.writeTo(new FileOutputStream(file));
            Toast.makeText(this, "Done", Toast.LENGTH_LONG).show();

            displayPdf(fileopen);
        } catch (IOException e) {
            Log.e("main", "error " + e.toString());
            Toast.makeText(this, "Something wrong: " + e.toString(), Toast.LENGTH_LONG).show();
        }


        document.close();

    }

    private void displayPdf(String mFileName) {


        Intent intent = new Intent(Intent.ACTION_VIEW);
        File file = new File(mFileName);
        Uri data = FileProvider.getUriForFile(PrintQRActivity.this, BuildConfig.APPLICATION_ID + ".provider", file);
        intent.setDataAndType(data, "application/pdf");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);


    }

    // this will find a bluetooth printer device
    void findBT() {

        try {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            if (mBluetoothAdapter == null) {
                myLabel.setText("No bluetooth printer available");
            }

            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBluetooth, 0);
            }

            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {

                    // RPP300 is the name of the bluetooth printer device
                    // we got this name from the list of paired devices
                    if (device.getName().equals("Nokia 5.1 Plus")) {
                        mmDevice = device;
                        Log.e("device:", mmDevice.getName().toString());
                        acceptBtn.setEnabled(true);
                        break;
                    }
                }
            }

            myLabel.setText("Printer  Found " + mmDevice.getName());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}