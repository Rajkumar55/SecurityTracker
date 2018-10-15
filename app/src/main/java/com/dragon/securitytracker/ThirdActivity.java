package com.dragon.securitytracker;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.OnNmeaMessageListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class ThirdActivity extends AppCompatActivity {
    private boolean api23, apiAbove23, stopped_patrol;
    private GpsStatus.NmeaListener nmea23;
    private OnNmeaMessageListener nmeaAbove23;
    private String nmeaString, imeiNumber;
    private LocationManager LM;
    private boolean toRemove, isListenerActive;
    private Button checkinBtn, startPatrolBtn, stopPatrolBtn, checkoutBtn, sosBtn;
    private int checked_in, started_patrol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

//        try {
//            boolean hasSoftKey = ViewConfiguration.get(getApplicationContext()).hasPermanentMenuKey();
//            if (hasSoftKey){
//                ImageView imageView = findViewById(R.id.imageView3);
////                LinearLayoutCompat linearLayoutCompat = findViewById(R.id.linearLayout);
//                imageView.getLayoutParams().height = (int) getResources().getDimension(R.dimen.imageview_height);;
//                imageView.requestLayout();
//
////                int dimensionInDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dimensionInPixel, getResources().getDisplayMetrics());
////                view.getLayoutParams().height = dimensionInDp;
////                view.getLayoutParams().width = dimensionInDp;
////                view.requestLayout();
//            }
////            View decorView = getWindow().getDecorView();
////            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
////                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
////            decorView.setSystemUiVisibility(uiOptions);
//        }
//        catch (Exception e){
//            Log.e("GPS", Log.getStackTraceString(e));
//        }

        final AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(ThirdActivity.this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(ThirdActivity.this);
        }

        Context context = getApplicationContext();
        TelephonyManager mngr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.


            builder.setTitle("Grant Permission")
                    .setMessage("Grant the permission to read phone state")
                    .setPositiveButton("Go to Settings", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent();
                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                            intent.setData(uri);
                            startActivity(intent);
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        } else {
            imeiNumber = mngr.getDeviceId();
        }

        if (!isNetworkAvailable()){
            builder.setTitle("Internet")
                    .setMessage("Please connect to the Internet.")
                    .setPositiveButton("Go to Settings", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent();
                            intent.setAction(Settings.ACTION_SETTINGS);
                            startActivity(intent);
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            builder.setTitle("Grant Permission")
                    .setMessage("Grant Location Permission")
                    .setPositiveButton("Go to Settings", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent();
                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                            intent.setData(uri);
                            startActivity(intent);
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }

        LM = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        ((LocationManager) getSystemService(Context.LOCATION_SERVICE)).requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                builder.setTitle("Enable GPS")
                        .setMessage("Please set GPS to High Accuracy Mode")
                        .setPositiveButton("Go to Settings", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent();
                                intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(intent);
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });

        api23 = false;
        apiAbove23 = false;
        toRemove = false;
        isListenerActive = false;
        if (Build.VERSION.SDK_INT <= 23)
            api23 = true;
        else
            apiAbove23 = true;




//        if (Build.VERSION.SDK_INT <= 23) {
//            api23 = true;
//
//            nmea23 = new GpsStatus.NmeaListener() {
//                public void onNmeaReceived(long timestamp, String nmea) {
//                    Log.d("TAG", "API 23 and Below - " + nmea + "\n");
//                    nmeaString = nmea;
//
//                }
//            };
//
//            //for API23 and below
//            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//
//                return;
//            }
//            else
//                LM.addNmeaListener(nmea23);
//        }
//
//        else {
//            apiAbove23 = true;
//
//            nmeaAbove23 = new OnNmeaMessageListener() {
//                public void onNmeaMessage(String nmea, long timestamp) {
//                    Log.d("TAG", "Above 23 - " + nmea + "\n");
//                    nmeaString = nmea;
//                }
//            };
//
//            //for API24 and above
//            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//
//                return;
//            }
//            else
//                LM.addNmeaListener(nmeaAbove23);
//        }

//        final Timer timer = new Timer();
//        final TimerTask hourlyTask = new TimerTask() {
//            @Override
//            public void run () {
//                Log.e("Timer", "Timer is running " + nmeaString);
//                parseNMEA(nmeaString);
//                nmeaString = null;
//            }
//        };

        checkinBtn = findViewById(R.id.checinBtn2);
        startPatrolBtn = findViewById(R.id.startPtrlBtn);
        startPatrolBtn.setEnabled(false);
        stopPatrolBtn = findViewById(R.id.stopPtrlBtn);
        stopPatrolBtn.setEnabled(false);
        checkoutBtn = findViewById(R.id.checkoutBtn2);
        checkoutBtn.setEnabled(false);
        sosBtn = findViewById(R.id.sosBtn1);
        sosBtn.setEnabled(false);

        stopped_patrol = false;
        checked_in = 0;
        started_patrol = 0;
//        started_patrol = false;

        try {
            Display mDisplay= getWindowManager().getDefaultDisplay();
            int width= mDisplay.getWidth();
            int height= mDisplay.getHeight();
            TextView tv = findViewById(R.id.textView7);
            ImageView imageView = findViewById(R.id.imageView3);
            Log.e("GPS", "Width - " + width + ", Height - " + height);
            if((width < 1100) && (height > 2000)){
                checkinBtn.getLayoutParams().width = (int) getResources().getDimension(R.dimen.circle_width);
                checkinBtn.getLayoutParams().height = (int) getResources().getDimension(R.dimen.circle_height);
                checkinBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                checkoutBtn.getLayoutParams().width = (int) getResources().getDimension(R.dimen.circle_width);
                checkoutBtn.getLayoutParams().height = (int) getResources().getDimension(R.dimen.circle_height);
                checkoutBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                startPatrolBtn.getLayoutParams().height = (int) getResources().getDimension(R.dimen.circle_height);
                startPatrolBtn.getLayoutParams().width = (int) getResources().getDimension(R.dimen.circle_width);
                startPatrolBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                stopPatrolBtn.getLayoutParams().height = (int) getResources().getDimension(R.dimen.circle_height);
                stopPatrolBtn.getLayoutParams().width = (int) getResources().getDimension(R.dimen.circle_width);
                stopPatrolBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                sosBtn.getLayoutParams().height = (int) getResources().getDimension(R.dimen.circle_height);
                sosBtn.getLayoutParams().width = (int) getResources().getDimension(R.dimen.circle_width);
                sosBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);

                Button qrcodeBtn = findViewById(R.id.qrcodeBtn);
                qrcodeBtn.getLayoutParams().width = (int) getResources().getDimension(R.dimen.circle_width);
                qrcodeBtn.getLayoutParams().height = (int) getResources().getDimension(R.dimen.circle_height);
                qrcodeBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);

                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);

//                ScrollView scrollView = findViewById(R.id.)
//                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) checkinBtn.getLayoutParams();
//                params.width = 200;
//                params.leftMargin = 131; //params.topMargin = 200;

//                checkinBtn.
//                imageView.getLayoutParams().height = (int) getResources().getDimension(R.dimen.imageview_height);

//                ConstraintLayout constraintLayout = findViewById(R.id.constraintLayout);
//                constraintLayout.requestDisallowInterceptTouchEvent(true);
            }
//            DisplayMetrics displaymetrics = new DisplayMetrics();
//            if (Build.VERSION.SDK_INT >= 17) {
//            float dpi = getResources().getDisplayMetrics().density;
            ViewGroup.MarginLayoutParams vlp = (ViewGroup.MarginLayoutParams) checkinBtn.getLayoutParams();
            int margins = 2 * vlp.topMargin;
            int buttonHeight = checkinBtn.getLayoutParams().height;
            int textHeight = tv.getLayoutParams().height;
            int line = (int) (4 * getResources().getDisplayMetrics().density);
            int totalLineHeight = 4 * line;
            int viewHeight = 3 * (buttonHeight + margins);
            int remainingSpace = height - (viewHeight + totalLineHeight + textHeight);
//            int extraHeight = (int) (20 * getResources().getDisplayMetrics().density);
            imageView.getLayoutParams().height = remainingSpace;
//                int dpi = getWindowManager().getDefaultDisplay().getRealMetrics(displaymetrics);
//                int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, displaymetrics);
//            Toast.makeText(getApplicationContext(), "Button Height - " + remainingSpace, Toast.LENGTH_LONG).show();
//            }
        }
        catch (Exception e){

        }

        final SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("ACTION_PREFS", Context.MODE_PRIVATE);
        String user_action = sharedPref.getString("action", "");
        checkData(sharedPref, user_action);

        final SharedPreferences.Editor editor = sharedPref.edit();

        checkinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Checked in", Toast.LENGTH_LONG).show();
                checked_in = 1;
                sendData(imeiNumber, checked_in, 0);
                checkinBtn.setEnabled(false);
                startPatrolBtn.setEnabled(true);
                checkoutBtn.setEnabled(true);
                sosBtn.setEnabled(true);

                editor.putString("action", "CHECKED_IN");
                editor.apply();
                stopped_patrol = false;

            }
        });


        startPatrolBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Patrol Started", Toast.LENGTH_LONG).show();
                stopped_patrol = false;
                started_patrol = 1;
//                started_patrol = true;
                setAlarm(getApplicationContext());
//                if (api23){
//                    Intent in = new Intent(getApplicationContext(), TrackerService.class);
//                    startService(in);
//                    Boolean isServiceRunning = ServiceTools.isServiceRunning(
//                            getApplicationContext(),
//                            TrackerService.class);
//                }
//                else if (apiAbove23){
//                    Intent in = new Intent(getApplicationContext(), TrackService.class);
//                    startService(in);
//                    Boolean isServiceRunning = ServiceTools.isServiceRunning(
//                            getApplicationContext(),
//                            TrackService.class);
//                }
                checkinBtn.setEnabled(false);
                checkoutBtn.setEnabled(false);
                startPatrolBtn.setEnabled(false);
                stopPatrolBtn.setEnabled(true);
                sosBtn.setEnabled(true);

                editor.putString("action", "STARTED_PATROL");
                editor.apply();
            }
        });

        stopPatrolBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Patrol Stopped", Toast.LENGTH_LONG).show();
                stopped_patrol = true;
                started_patrol = 0;
//                started_patrol = false;
                cancelAlarm(getApplicationContext());
                if (api23){
                    stopService(new Intent(getApplicationContext(), CaplocTrackService.class));
//                    stopService(new Intent(getApplicationContext(), TrackerService.class));
//                    Boolean isServiceRunning = ServiceTools.isServiceRunning(
//                            getApplicationContext(),
//                            TrackerService.class);

                    nmea23 = new GpsStatus.NmeaListener() {
                        public void onNmeaReceived(long timestamp, String nmea) {
//                            Log.d("TAG", "API 23 and Below - " + nmea + "\n");
                            nmeaString = nmea;
                            String latLong = parseNMEA(nmeaString);
                            nmeaString = null;
                            if (latLong != null) {
                                stopPatrol(imeiNumber, latLong);
                                toRemove = true;
                            }
                        }
                    };

                    //for API23 and below
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                        return;
                    }
                    else {
                        LM.addNmeaListener(nmea23);
                        isListenerActive = true;
                    }
                }
                else if (apiAbove23){
                    stopService(new Intent(getApplicationContext(), CaplocTrackingService.class));
//                    stopService(new Intent(getApplicationContext(), TrackService.class));
//                    Boolean isServiceRunning = ServiceTools.isServiceRunning(
//                            getApplicationContext(),
//                            TrackService.class);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        nmeaAbove23 = new OnNmeaMessageListener() {
                            public void onNmeaMessage(String nmea, long timestamp) {
//                                Log.d("TAG", "Above 23 - " + nmea + "\n");
                                nmeaString = nmea;
                                String latLong = parseNMEA(nmeaString);
                                nmeaString = null;
                                if (latLong != null) {
                                    stopPatrol(imeiNumber, latLong);
                                    toRemove = true;
                                }
                            }
                        };


                        //for API24 and above
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                            return;
                        } else {
                            LM.addNmeaListener(nmeaAbove23);
                            isListenerActive = true;
                        }
                    }
                }
                checkinBtn.setEnabled(false);
                checkoutBtn.setEnabled(true);
                stopPatrolBtn.setEnabled(false);
                startPatrolBtn.setEnabled(true);
                sosBtn.setEnabled(true);

                editor.putString("action", "STOPPED_PATROL");
                editor.apply();
            }
        });

        checkoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Checked out", Toast.LENGTH_LONG).show();

                if (!stopped_patrol && (started_patrol == 1)){
                    Toast.makeText(getApplicationContext(), "Patrol Stopped", Toast.LENGTH_LONG).show();
                    stopped_patrol = true;
                    started_patrol = 0;
//                    started_patrol = false;
                    cancelAlarm(getApplicationContext());
                    if (api23){
                        stopService(new Intent(getApplicationContext(), CaplocTrackService.class));
//                        stopService(new Intent(getApplicationContext(), TrackerService.class));
//                        Boolean isServiceRunning = ServiceTools.isServiceRunning(
//                                getApplicationContext(),
//                                TrackerService.class);

                        nmea23 = new GpsStatus.NmeaListener() {
                            public void onNmeaReceived(long timestamp, String nmea) {
    //                            Log.d("TAG", "API 23 and Below - " + nmea + "\n");
                                nmeaString = nmea;
                                String latLong = parseNMEA(nmeaString);
                                nmeaString = null;
                                if (latLong != null) {
                                    stopPatrol(imeiNumber, latLong);
                                    toRemove = true;
                                }
                            }
                        };

                        //for API23 and below
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                            return;
                        }
                        else {
                            LM.addNmeaListener(nmea23);
                            isListenerActive = true;
                        }
                    }
                    else if (apiAbove23){
                        stopService(new Intent(getApplicationContext(), CaplocTrackingService.class));
//                        stopService(new Intent(getApplicationContext(), TrackService.class));
//                        Boolean isServiceRunning = ServiceTools.isServiceRunning(
//                                getApplicationContext(),
//                                TrackService.class);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            nmeaAbove23 = new OnNmeaMessageListener() {
                                public void onNmeaMessage(String nmea, long timestamp) {
    //                                Log.d("TAG", "Above 23 - " + nmea + "\n");
                                    nmeaString = nmea;
                                    String latLong = parseNMEA(nmeaString);
                                    nmeaString = null;
                                    if (latLong != null) {
                                        stopPatrol(imeiNumber, latLong);
                                        toRemove = true;
                                    }
                                }
                            };


                            //for API24 and above
                            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                                return;
                            } else {
                                LM.addNmeaListener(nmeaAbove23);
                                isListenerActive = true;
                            }
                        }
                    }

                }
                checked_in = 0;
                sendData(imeiNumber, checked_in, 0);
                checkinBtn.setEnabled(true);
                checkoutBtn.setEnabled(false);
                startPatrolBtn.setEnabled(false);
                stopPatrolBtn.setEnabled(false);
                sosBtn.setEnabled(false);

                editor.clear();
                editor.apply();
            }
        });


        sosBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "SOS Sent", Toast.LENGTH_LONG).show();
                sendData(imeiNumber, checked_in, 1);
            }
        });
    }

    private void checkData(SharedPreferences sharedPref, String user_action){
//        Log.e("GPS", user_action);
        switch (user_action) {
//            case "":
//                SharedPreferences.Editor editor = sharedPref.edit();
//                editor.putString("action", "CHECKED_IN");
//                editor.apply();
//                break;
            case "CHECKED_IN":
                checkinBtn.setEnabled(false);
                startPatrolBtn.setEnabled(true);
                checkoutBtn.setEnabled(true);
                stopPatrolBtn.setEnabled(false);
                sosBtn.setEnabled(true);
                stopped_patrol = false;
                checked_in = 1;
                started_patrol = 0;
                break;
            case "STARTED_PATROL":
                checkinBtn.setEnabled(false);
                checkoutBtn.setEnabled(false);
                startPatrolBtn.setEnabled(false);
                stopPatrolBtn.setEnabled(true);
                sosBtn.setEnabled(true);
                stopped_patrol = false;
                checked_in = 1;
                started_patrol = 1;
                break;
            case "STOPPED_PATROL":
                checkinBtn.setEnabled(false);
                checkoutBtn.setEnabled(true);
                stopPatrolBtn.setEnabled(false);
                startPatrolBtn.setEnabled(true);
                stopped_patrol = true;
                sosBtn.setEnabled(true);
                checked_in = 1;
                started_patrol = 0;
                break;
//            default:
//                SharedPreferences.Editor sharedPrefEditor = sharedPref.edit();
//                sharedPrefEditor.putString("action", "CHECKED_IN");
//                sharedPrefEditor.apply();
//                break;
        }
//        else if (user_action.equals("CHECKED_OUT")){
//            checkinBtn.setEnabled(true);
//            checkoutBtn.setEnabled(false);
//            startPatrolBtn.setEnabled(false);
//        }
    }

    private void sendData(String imeiNumber, int checkin, final int sos){
        final String imei = imeiNumber;
        final int check = checkin;
        final int alert = sos;
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    Socket s = new Socket("45.249.111.58", 5149);
                    String dateTime = getDateTime();
                    String dt[] = dateTime.split(" ");
                    PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(s.getOutputStream())), true);
                    String data = imei + ",0,N,0,E," + String.valueOf(alert) +",0,0,1,0," + check +",0,0," + dt[0] + "," + dt[1] +",0,";
                    if (alert == 1)
                        data += String.valueOf(started_patrol) + ",";
                    else
                        data += "0,";
                    out.println(data);
//                    Log.e("GPS", "Sent " + data);
//                    out.flush();
                    s.close();
                } catch (Exception e) {
//                    Log.e("GPS", Log.getStackTraceString(e));
                }
            }
        }).start();
    }

    private String getDateTime(){
        DateFormat df = new SimpleDateFormat("HH:mm:ss dd:MM:yy");
        String dateTime = df.format(Calendar.getInstance().getTime());
        return(dateTime);
    }

    private String parseNMEA(String nmeaString){
        if ((nmeaString != null) && (!nmeaString.isEmpty())) {
            String words[] = nmeaString.split(",");
            String latLong = null;
            if (words[0].equals("$GPRMC")) {
                if ((!words[3].isEmpty()) && (!words[5].isEmpty())) {
                    latLong = words[3] + "," + words[4] + "," + words[5] + "," + words[6];
                }
            } else if (words[0].equals("$GPGGA")) {
                if ((!words[2].isEmpty()) && (!words[4].isEmpty())) {
                    latLong = words[2] + "," + words[3] + "," + words[4] + "," + words[5];
                }
            }
//            Log.e("Timer", "Lat Long  - " + latLong);
            return(latLong);
        }
        return(null);
    }

    private void stopPatrol(String imeiNumber, String latLong){
        final String imei = imeiNumber;
        final String lat_lng = latLong;
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    Socket s = new Socket("45.249.111.58", 5149);
                    String dateTime = getDateTime();
                    String dt[] = dateTime.split(" ");
                    PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(s.getOutputStream())), true);
                    String data = imei + "," + lat_lng + ",0,0,0,1,0,1,0,0," + dt[0] + "," + dt[1] +",0,0,";
                    out.println(data);
//                    Log.e("GPS", "Sent " + data);
//                    out.flush();
                    s.close();
                } catch (Exception e) {
//                    Log.e("GPS", Log.getStackTraceString(e));
                }
            }
        }).start();
        if (api23) {
            LM.removeNmeaListener(nmea23);
        } else if (apiAbove23) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                LM.removeNmeaListener(nmeaAbove23);
            }
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isListenerActive) {
            if (api23) {
                LM.removeNmeaListener(nmea23);
            } else if (apiAbove23) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    LM.removeNmeaListener(nmeaAbove23);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isListenerActive) {
            if (api23) {
                LM.removeNmeaListener(nmea23);
            } else if (apiAbove23) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    LM.removeNmeaListener(nmeaAbove23);
                }
            }
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        try {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        catch (Exception e){
//            Log.e("GPS", "Exception " + e.getMessage());
            return(false);
        }
    }

    private void setAlarm(Context context)
    {
        AlarmManager am=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, CaplocTrackService.class);;
//        if (api23)
//            intent = new Intent(context, CaplocTrackService.class);
        if (apiAbove23)
            intent = new Intent(context, CaplocTrackingService.class);
//        intent.putExtra(ONE_TIME, Boolean.FALSE);
        PendingIntent pi = PendingIntent.getService(context, 0, intent, 0);
//        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);
        //After after 10 seconds
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 60 * 10, pi);
    }

    private void cancelAlarm(Context context)
    {
        Intent intent = new Intent(context, CaplocTrackService.class);
        if (apiAbove23)
            intent = new Intent(context, CaplocTrackingService.class);
        PendingIntent sender = PendingIntent.getService(context, 0, intent, 0);
//        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }

    //    private String decimalToDMS(double value) {
//        String result = null;
//        double degValue = value / 100;
//        int degrees = (int) degValue;
//        double decMinutesSeconds = ((degValue - degrees)) / .60;
//        double minuteValue = decMinutesSeconds * 60;
//        int minutes = (int) minuteValue;
//        double secsValue = (minuteValue - minutes) * 60;
//        result = degrees + "\u00B0" + " " + minutes + "' " + String.format("%.1f", secsValue) + "\" ";
//        return result;
//    }
}

//class PatrolThread extends Thread{
//
//    @Override
//    public void run() {
//        try {
//            InetAddress serverAddr = InetAddress.getByName("45.249.111.58");
//            Log.d("TCP Client Socket", "Connecting... " + serverAddr.getHostAddress());
//
//
//            // Creating new socket connection to the IP (first parameter) and its opened port (second parameter)
//            Socket s = new Socket("192.168.43.235", 5149);
//            Log.e("TCP", "Socket Connected");
//
////                                DataOutputStream out = new DataOutputStream(s.getOutputStream());
//            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(s.getOutputStream())), true);
//            Log.e("TCP", "Print Writer");
//            out.println("352514086819871,1254.9344,N,7737.5362,E,0,0,0,1,0,1,99,0,12:38:56,15:09:18,3340,0,");
//            Log.e("GPS", "Sent");
////                                out.writeUTF("352514086819871,1254.9344,N,7737.5362,E,0,0,0,1,0,1,99,0,12:38:56,15:09:18,3340,0,");
//            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
//
//            Log.e("GPS", "Out " + in.readLine());
//        }
//        catch (Exception e){
//            Log.e("GPS", Log.getStackTraceString(e));
//        }
//    }
//}

class ServiceTools {
    public static boolean isServiceRunning(Context context,Class<?> serviceClass){
        final ActivityManager activityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);

        for (ActivityManager.RunningServiceInfo runningServiceInfo : services) {

            if (runningServiceInfo.service.getClassName().equals(serviceClass.getName())){
//                Log.e("GPS", "Security Track Service is running");
                return true;
            }
        }
//        Log.e("GPS", "Security Track Service is not running");
        return false;
    }
}