package com.dragon.securitytracker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.app.AlertDialog;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;


public class MainActivity extends AppCompatActivity{
    private static final int REQUEST_READ_PHONE_STATE = 663;
    private static final int REQUEST_FINE_LOCATION = 149;
    private static final int REQUEST_COARSE_LOCATION = 541;
    private TextView imeiText;
    private TelephonyManager mngr;
    private boolean phone_state_enabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.linear_first);
//        setContentView(R.layout.main);

        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("ACTION_PREFS", Context.MODE_PRIVATE);
        String user_action = sharedPref.getString("action", "");
        if (!user_action.equals("")){
            Intent in = new Intent(getApplicationContext(), ThirdActivity.class);
            startActivity(in);
            finish();
        }


        imeiText = findViewById(R.id.textView2);

//        Button button = findViewById(R.id.button);

        final AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(MainActivity.this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(MainActivity.this);
        }

        Context context = getApplicationContext();
        mngr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        phone_state_enabled = false;

//        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
//
//        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_READ_PHONE_STATE);
//        } else {
//            String imeiNumber = mngr.getDeviceId();
//            imeiText.setText("Your IMEI\n" + imeiNumber);
//
//        }

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.


            builder.setTitle("Grant Permission")
                    .setMessage("Grant the permission to read phone state and reopen the app")
                    .setPositiveButton("Go to Settings", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent();
                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                            intent.setData(uri);
                            startActivity(intent);
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
//        else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            builder.setTitle("Grant Permission")
//                    .setMessage("Grant Location Permission")
//                    .setPositiveButton("Go to Settings", new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int which) {
//                            Intent intent = new Intent();
//                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                            Uri uri = Uri.fromParts("package", getPackageName(), null);
//                            intent.setData(uri);
//                            startActivity(intent);
//                        }
//                    })
//                    .setIcon(android.R.drawable.ic_dialog_alert)
//                    .show();
//        }
        else {
            phone_state_enabled = true;
            String imeiNumber = mngr.getDeviceId();
            imeiText.setText("Your IMEI\n" + imeiNumber);
        }

//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (phoneNumber.getText().toString().isEmpty()) {
//                    phoneNumber.setHint("Mobile Number is required");
//                    phoneNumber.setHintTextColor(Color.RED);
//                }
//                else {
//                    Context context = getApplicationContext();
//                    TelephonyManager mngr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
//                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
//                        //    ActivityCompat#requestPermissions
//                        // here to request the missing permissions, and then overriding
//                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                        //                                          int[] grantResults)
//                        // to handle the case where the user grants the permission. See the documentation
//                        // for ActivityCompat#requestPermissions for more details.
//
//
//                        builder.setTitle("Grant Permission")
//                                .setMessage("Grant the permission to read phone state")
//                                .setPositiveButton("Go to Settings", new DialogInterface.OnClickListener() {
//                                    public void onClick(DialogInterface dialog, int which) {
//                                        Intent intent = new Intent();
//                                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                                        Uri uri = Uri.fromParts("package", getPackageName(), null);
//                                        intent.setData(uri);
//                                        startActivity(intent);
//                                    }
//                                })
//                                .setIcon(android.R.drawable.ic_dialog_alert)
//                                .show();
//                    } else {
//                        String imeiNumber = mngr.getDeviceId();
//                        imeiText.setVisibility(View.VISIBLE);
//                        TextView imeiNumberView = findViewById(R.id.textView3);
//                        imeiNumberView.setText(imeiNumber);
//                    }
//                }
//            }
//        });

        Button button1 = findViewById(R.id.button);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!phone_state_enabled) {
                    builder.setTitle("Grant Permission")
                            .setMessage("Grant the permission to read phone state and reopen the app")
                            .setPositiveButton("Go to Settings", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent();
                                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                                    intent.setData(uri);
                                    startActivity(intent);
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
                else if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    builder.setTitle("Grant Permission")
                            .setMessage("Grant Location Permission")
                            .setPositiveButton("Go to Settings", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent();
                                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                                    intent.setData(uri);
                                    startActivity(intent);
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
                else {
                    Intent in = new Intent(getApplicationContext(), ThirdActivity.class);
                    startActivity(in);
                    finish();
                }
//                int permissionCheck1 = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);
//
//                if (permissionCheck1 != PackageManager.PERMISSION_GRANTED) {
//                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION);
//                } else {
//                    int permissionCheck2 = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION);
//
//                    if (permissionCheck2 != PackageManager.PERMISSION_GRANTED) {
//                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_COARSE_LOCATION);
//                    } else {
//                        Intent in = new Intent(getApplicationContext(), ThirdActivity.class);
//                        startActivity(in);
//                        finish();
//                    }
//                }

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            String imeiNumber = mngr.getDeviceId();
            imeiText.setText("Your IMEI\n" + imeiNumber);
            phone_state_enabled = true;
        }
    }

    //    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        switch (requestCode){
//            case REQUEST_READ_PHONE_STATE:
//                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
//                    @SuppressLint("MissingPermission") String imeiNumber = mngr.getDeviceId();
//                    imeiText.setText("Your IMEI\n" + imeiNumber);
//                }
//                else{
//                    Toast.makeText(getApplicationContext(), "You have to enable Phone state and Location permissions to use the app", Toast.LENGTH_LONG).show();
//                }
//                break;
//            case REQUEST_FINE_LOCATION:
//                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
//                    @SuppressLint("MissingPermission") String imeiNumber = mngr.getDeviceId();
//                    imeiText.setText("Your IMEI\n" + imeiNumber);
//                }
//                else{
//                    Toast.makeText(getApplicationContext(), "You have to enable Phone state and Location permissions to use the app", Toast.LENGTH_LONG).show();
//                }
//                break;
//
//            default:
//                break;
//        }
//    }
}
