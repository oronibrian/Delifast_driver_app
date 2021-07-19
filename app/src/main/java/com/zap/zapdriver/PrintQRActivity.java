package com.zap.zapdriver;

import android.app.Activity;
import android.app.Dialog;
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
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.dantsu.escposprinter.EscPosPrinter;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections;
import com.dantsu.escposprinter.exceptions.EscPosBarcodeException;
import com.dantsu.escposprinter.exceptions.EscPosConnectionException;
import com.dantsu.escposprinter.exceptions.EscPosEncodingException;
import com.dantsu.escposprinter.exceptions.EscPosParserException;
import com.dantsu.escposprinter.textparser.PrinterTextParserImg;
import com.zap.zapdriver.service.UnicodeFormatter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class PrintQRActivity extends AppCompatActivity {
    DriverApplication app;
    TextView content;
    Button acceptBtn, acceptBtnContinue;
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

    BluetoothAdapter bluetoothAdapter;
    BluetoothSocket socket;
    BluetoothDevice bluetoothDevice;
    OutputStream outputStream;
    InputStream inputStream;

    String value = "";

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
        acceptBtnContinue = findViewById(R.id.acceptBtnContinue);


        findBT();


        acceptBtnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                createPdf();
//                showDialog(PrintQRActivity.this);
                startActivity(new Intent(PrintQRActivity.this, TurnNavigation2.class));
                finish();

            }
        });

        acceptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String mydate = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());


                bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                try {
                    if (!bluetoothAdapter.isEnabled()) {
                        Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBluetooth, 0);
                    }

                    Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

                    if (pairedDevices.size() > 0) {
                        for (BluetoothDevice device : pairedDevices) {
                            if (device.getName().equals("T12 BT Printer")) //Note, you will need to change this to match the name of your device
                            {
                                bluetoothDevice = device;
                                Toast.makeText(PrintQRActivity.this, bluetoothDevice.getName(), Toast.LENGTH_LONG).show();


                                try {

                                    EscPosPrinter printer = new EscPosPrinter(BluetoothPrintersConnections.selectFirstPaired(), 203, 48f, 32);
                                    printer
                                            .printFormattedText(
                                                    "[C]<img>" + PrinterTextParserImg.bitmapToHexadecimalString(printer, PrintQRActivity.this.getResources().getDrawableForDensity(R.drawable.zap_cnvte, DisplayMetrics.DENSITY_140)) + "</img>\n" +
                                                            "[L]\n" +
                                                            "[C]<u><font size='big'>Zap Logistics</font></u>\n" +
                                                            "[L]\n" +
                                                            "[C]\n" + mydate +
                                                            "[L]\n" +
                                                            "[C]================================\n" +
                                                            "[L]\n" +

                                                            "[L]From: \n" + app.getPackage_from() +
                                                            "[L]\n" +
                                                            "[L]\n" +
                                                            "[C]--------------------------------\n" +
                                                            "[L]To:\n" + app.getPackage_to() +
                                                            "[R]\n" +
                                                            "[L]\n" +
                                                            "[C]================================\n" +
                                                            "[L]\n" +
//
//                                                            "[C]<barcode type='ean13' height='10'>000000000000" + app.getPackage_id() + "</barcode>\n" +
//                                                            "[L]\n" +

                                                            "[C]<qrcode size='20'>https://zaplogistics.co.ke/</qrcode>"
                                            );


                                } catch (EscPosConnectionException | EscPosBarcodeException | EscPosEncodingException | EscPosParserException e) {
                                    e.printStackTrace();
                                }


//                                break;
                            }
                        }

                        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard SerialPortService ID
                        Method m = bluetoothDevice.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
                        socket = (BluetoothSocket) m.invoke(bluetoothDevice, 1);
//                        bluetoothAdapter.cancelDiscovery();

//                beginListenForData();
                    } else {
                        value += "No Devices found";
                        Toast.makeText(PrintQRActivity.this, value, Toast.LENGTH_LONG).show();
                        return;
                    }
                } catch (Exception ex) {
                    value += ex.toString() + "\n" + " InitPrinter \n";
                    Toast.makeText(PrintQRActivity.this, value, Toast.LENGTH_LONG).show();
                }


//                createPdf();
//                showDialog(PrintQRActivity.this);


            }
        });

    }


    public void showDialog(Activity activity) {
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.custom_dialog_direction);


        Button dialogButton = (Button) dialog.findViewById(R.id.btndirection);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

                Uri.Builder builder = new Uri.Builder();
                builder.scheme("https")
                        .authority("www.google.com")
                        .appendPath("maps")
                        .appendPath("dir")
                        .appendPath("")
                        .appendQueryParameter("api", "1")
                        .appendQueryParameter("destination", app.getDestination().latitude + "," + app.getDestination().longitude);
                String url = builder.build().toString();
                Log.d("Directions", url);
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
                finish();


            }
        });

        Button dialogButton2 = (Button) dialog.findViewById(R.id.btnhome);
        dialogButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                startActivity(new Intent(PrintQRActivity.this, MainActivity.class));
                finish();
            }
        });

        dialog.show();

    }


    public static byte intToByteArray(int value) {
        byte[] b = ByteBuffer.allocate(4).putInt(value).array();

        for (int k = 0; k < b.length; k++) {
            System.out.println("Selva  [" + k + "] = " + "0x"
                    + UnicodeFormatter.byteToHex(b[k]));
        }

        return b[3];
    }


    public void InitPrinter() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        try {
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBluetooth, 0);
            }

            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    if (device.getName().equals("T12 BT Printer")) //Note, you will need to change this to match the name of your device
                    {
                        bluetoothDevice = device;
                        Toast.makeText(this, bluetoothDevice.getName(), Toast.LENGTH_LONG).show();

                        break;
                    }
                }

                UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard SerialPortService ID
                Method m = bluetoothDevice.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
                socket = (BluetoothSocket) m.invoke(bluetoothDevice, 1);
                bluetoothAdapter.cancelDiscovery();
                socket.connect();
                outputStream = socket.getOutputStream();
                inputStream = socket.getInputStream();
//                beginListenForData();
            } else {
                value += "No Devices found";
                Toast.makeText(this, value, Toast.LENGTH_LONG).show();
                return;
            }
        } catch (Exception ex) {
            value += ex.toString() + "\n" + " InitPrinter \n";
            Toast.makeText(this, value, Toast.LENGTH_LONG).show();
        }
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

    void beginListenForData() {
        try {
            final Handler handler = new Handler();

            // this is the ASCII code for a newline character
            final byte delimiter = 10;

            stopWorker = false;
            readBufferPosition = 0;
            readBuffer = new byte[1024];

            workerThread = new Thread(new Runnable() {
                public void run() {

                    while (!Thread.currentThread().isInterrupted() && !stopWorker) {

                        try {

                            int bytesAvailable = inputStream.available();

                            if (bytesAvailable > 0) {

                                byte[] packetBytes = new byte[bytesAvailable];
                                inputStream.read(packetBytes);

                                for (int i = 0; i < bytesAvailable; i++) {

                                    byte b = packetBytes[i];
                                    if (b == delimiter) {

                                        byte[] encodedBytes = new byte[readBufferPosition];
                                        System.arraycopy(
                                                readBuffer, 0,
                                                encodedBytes, 0,
                                                encodedBytes.length
                                        );

                                        // specify US-ASCII encoding
                                        final String data = new String(encodedBytes, "US-ASCII");
                                        readBufferPosition = 0;

                                        // tell the user data were sent to bluetooth printer device
                                        handler.post(new Runnable() {
                                            public void run() {
                                                Log.e("eeee", data);
                                            }
                                        });

                                    } else {
                                        readBuffer[readBufferPosition++] = b;
                                    }
                                }
                            }

                        } catch (IOException ex) {
                            stopWorker = true;
                        }

                    }
                }
            });

            workerThread.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void createPdf() {


        Bitmap b, b2 = null, b3 = null, scled, scled3, scled2;
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

        scled2 = Bitmap.createScaledBitmap(b2, 130, 130, false);
        canvas.drawBitmap(scled2, 30, 280, paint);


        scled3 = Bitmap.createScaledBitmap(b3, 450, 130, false);
        canvas.drawBitmap(scled3, 20, 350, paint);


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

//            displayPdf(fileopen);
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
                    if (device.getName().equals("T12 BT Printer")) {
                        mmDevice = device;
                        Log.e("device:", mmDevice.getName().toString());
//                        InitPrinter();
                        acceptBtn.setEnabled(true);
                        acceptBtnContinue.setEnabled(true);

                        break;
                    }
                }
            } else {
                myLabel.setText("Printer not Found ");
                acceptBtnContinue.setVisibility(View.VISIBLE);
                acceptBtn.setVisibility(View.GONE);

            }

            myLabel.setText("Printer  Found " + mmDevice.getName());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}