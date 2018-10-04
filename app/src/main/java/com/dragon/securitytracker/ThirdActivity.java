package com.dragon.securitytracker;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
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
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
    private Button checkinBtn, startPatrolBtn, stopPatrolBtn, checkoutBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_next);

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
                        .setMessage("Please turn on GPS")
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

        checkinBtn = findViewById(R.id.checinBtn);
        startPatrolBtn = findViewById(R.id.startPatrolBtn);
        startPatrolBtn.setEnabled(false);
        stopPatrolBtn = findViewById(R.id.stopPatrolBtn);
        stopPatrolBtn.setEnabled(false);
        checkoutBtn = findViewById(R.id.checkoutBtn);
        checkoutBtn.setEnabled(false);

        stopped_patrol = false;

        final SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("ACTION_PREFS", Context.MODE_PRIVATE);
        String user_action = sharedPref.getString("action", "");
        checkData(sharedPref, user_action);

        final SharedPreferences.Editor editor = sharedPref.edit();

        checkinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Checked in", Toast.LENGTH_LONG).show();
                sendData(imeiNumber, 1, 0);
                checkinBtn.setEnabled(false);
                startPatrolBtn.setEnabled(true);
                checkoutBtn.setEnabled(true);

                editor.putString("action", "CHECKED_IN");
                editor.apply();

            }
        });


        startPatrolBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Patrol Started", Toast.LENGTH_LONG).show();
                if (api23){
                    Intent in = new Intent(getApplicationContext(), TrackerService.class);
                    startService(in);
                    Boolean isServiceRunning = ServiceTools.isServiceRunning(
                            getApplicationContext(),
                            TrackerService.class);
                }
                else if (apiAbove23){
                    Intent in = new Intent(getApplicationContext(), TrackService.class);
                    startService(in);
                    Boolean isServiceRunning = ServiceTools.isServiceRunning(
                            getApplicationContext(),
                            TrackService.class);
                }
                checkinBtn.setEnabled(false);
                checkoutBtn.setEnabled(true);
                startPatrolBtn.setEnabled(false);
                stopPatrolBtn.setEnabled(true);

                editor.putString("action", "STARTED_PATROL");
                editor.apply();
            }
        });

        stopPatrolBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Patrol Stopped", Toast.LENGTH_LONG).show();
                stopped_patrol = true;
                if (api23){
                    stopService(new Intent(getApplicationContext(), TrackerService.class));
                    Boolean isServiceRunning = ServiceTools.isServiceRunning(
                            getApplicationContext(),
                            TrackerService.class);

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
                    stopService(new Intent(getApplicationContext(), TrackService.class));
                    Boolean isServiceRunning = ServiceTools.isServiceRunning(
                            getApplicationContext(),
                            TrackService.class);

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

                editor.putString("action", "STOPPED_PATROL");
                editor.apply();
            }
        });

        checkoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Checked out", Toast.LENGTH_LONG).show();

                if (!stopped_patrol){
                    Toast.makeText(getApplicationContext(), "Patrol Stopped", Toast.LENGTH_LONG).show();
                    stopped_patrol = true;
                    if (api23){
                        stopService(new Intent(getApplicationContext(), TrackerService.class));
                        Boolean isServiceRunning = ServiceTools.isServiceRunning(
                                getApplicationContext(),
                                TrackerService.class);

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
                        stopService(new Intent(getApplicationContext(), TrackService.class));
                        Boolean isServiceRunning = ServiceTools.isServiceRunning(
                                getApplicationContext(),
                                TrackService.class);

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
                sendData(imeiNumber, 0, 0);
                checkinBtn.setEnabled(true);
                checkoutBtn.setEnabled(false);
                startPatrolBtn.setEnabled(false);
                stopPatrolBtn.setEnabled(false);

                editor.clear();
                editor.apply();
            }
        });

        Button sosBtn = findViewById(R.id.sosBtn);
        sosBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "SOS Sent", Toast.LENGTH_LONG).show();
                sendData(imeiNumber, 0, 1);
            }
        });
    }

    private void checkData(SharedPreferences sharedPref, String user_action){
        Log.e("GPS", user_action);
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
                break;
            case "STARTED_PATROL":
                checkinBtn.setEnabled(false);
                checkoutBtn.setEnabled(true);
                startPatrolBtn.setEnabled(false);
                stopPatrolBtn.setEnabled(true);
                break;
            case "STOPPED_PATROL":
                checkinBtn.setEnabled(false);
                checkoutBtn.setEnabled(true);
                stopPatrolBtn.setEnabled(false);
                startPatrolBtn.setEnabled(true);
                stopped_patrol = true;
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
//        final int check = checkin;
        final int alert = sos;
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    Socket s = new Socket("45.249.111.58", 5149);
                    String dateTime = getDateTime();
                    String dt[] = dateTime.split(" ");
                    PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(s.getOutputStream())), true);
                    String data = imei + ",0,N,0,E," + String.valueOf(alert) +",0,0,1,0,1,0,0," + dt[0] + "," + dt[1] +",0,0,";
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