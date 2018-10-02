package com.dragon.securitytracker;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.OnNmeaMessageListener;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class TrackerService extends Service implements LocationListener, GpsStatus.NmeaListener{
    private String TAG = "GPS";
    private String nmeaString;
    private LocationManager LM;
    private Timer timer;
    private TimerTask hourlyTask;
    public TrackerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        LM = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        ((LocationManager) getSystemService(Context.LOCATION_SERVICE)).requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

        LM.addNmeaListener(this);

        TelephonyManager mngr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        final String imeiNumber = mngr.getDeviceId();

        if (imeiNumber != null) {

            timer = new Timer();
            hourlyTask = new TimerTask() {
                @Override
                public void run() {
//                    Log.e("Timer", "Timer is running " + nmeaString);
                    String latLong = parseNMEA(nmeaString);
                    nmeaString = null;

                    if (latLong != null) {
                        DateFormat df = new SimpleDateFormat("HH:mm:ss dd:MM:yy");
                        String dateTime = df.format(Calendar.getInstance().getTime());
                        String dt[] = dateTime.split(" ");
                        final String data = imeiNumber + "," + latLong + ",0,0,0,1,0,1,0,0," + dt[0] + "," + dt[1] + ",0,1,";

                        new Thread(new Runnable() {

                            @Override
                            public void run() {
                                try {
                                    Socket s = new Socket("45.249.111.58", 5149);
                                    PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(s.getOutputStream())), true);
                                    out.println(data);
                                    //                        out.println("352514086819871,1254.9344,N,7737.5362,E,0,0,0,1,0,1,99,0,12:38:56,15:09:18,3340,0,");
//                                    Log.e("GPS", "Sent " + data);
                                    out.flush();
                                    s.close();
                                    //                                out.writeUTF("352514086819871,1254.9344,N,7737.5362,E,0,0,0,1,0,1,99,0,12:38:56,15:09:18,3340,0,");
                                    //                        BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                                    //
                                    //                        Log.e("GPS", "Out " + in.readLine());
                                } catch (Exception e) {
//                                    Log.e("GPS", Log.getStackTraceString(e));
                                }
                            }
                        }).start();
                    }

                }
            };

            timer.schedule(hourlyTask, 0l, 1000 * 10);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        Log.e(TAG, "Security Track Service stopped");
        LM.removeNmeaListener(this);
        timer.cancel();
    }

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

    }

    @Override
    public void onNmeaReceived(long timestamp, String nmea) {
        nmeaString = nmea;
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
}
