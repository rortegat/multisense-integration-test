package com.example.multisensepruebas;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.cellocator.nano.android.sdk.MultiSenseDeviceCallback;
import com.cellocator.nano.android.sdk.MultiSenseManager;
import com.cellocator.nano.android.sdk.MultiSenseObserver;
import com.cellocator.nano.android.sdk.MultiSenseObserverCallback;
import com.cellocator.nano.android.sdk.MultiSenseReadingLoggerStatus;
import com.cellocator.nano.android.sdk.MultiSenseScanner;
import com.cellocator.nano.android.sdk.model.MultiSenseDevice;
import com.cellocator.nano.android.sdk.model.MultiSenseSensors;

public class MainActivity extends AppCompatActivity {

    private TextView scanStatus;
    private Button startStopBtn;

    private boolean scanning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkLocationPermissions();

        scanStatus = (TextView) findViewById(R.id.textStatus);
        startStopBtn = (Button) findViewById(R.id.buttonStartStop);

        MultiSenseManager multiSenseManager = new MultiSenseManager(this);
        MultiSenseScanner multiSenseScanner = multiSenseManager.createScanner();
        MultiSenseObserver multiSenseObserver = multiSenseManager.createObserver();

        startStopBtn.setOnClickListener(v -> {
            if (!scanning) {
                scanning = true;
                startStopBtn.setText(R.string.stop);
                scanStatus.setText(R.string.scan_started);
                multiSenseScanner.scan(
                        new MultiSenseDeviceCallback() {
                            @Override
                            public void onError(int errorType, String s) {
                                Log.e("SCAN", s);
                            }

                            @Override
                            public void onChange(MultiSenseDevice multiSenseDevice) {
                                // Adding found device by mac address to observer
                                multiSenseObserver.addTag(multiSenseDevice.getAddress());
                            }
                        });
                multiSenseObserver.startObserveTags(new MultiSenseObserverCallback() {
                    @Override
                    public void onReadingLoggerStatusChange(String s, MultiSenseReadingLoggerStatus multiSenseReadingLoggerStatus) {

                    }

                    @Override
                    public void onError(int i, String s) {
                        Log.e("OBSERVER", s);
                    }

                    @Override
                    public void onChange(MultiSenseDevice multiSenseDevice) {
                        // Reading device general info
                        String mac = multiSenseDevice.getAddress();
                        String name = multiSenseDevice.getName();
                        /*int rssi = multiSenseDevice.getRssi();
                        String firmwareRevision =
                                multiSenseDevice.getAdvertisement().getFirmwareVersionName();
                        String hardwareRevision =
                                multiSenseDevice.getAdvertisement().getOtaVersionName();*/

                        System.out.println("MAC: " + mac + " NAME: " + name);
                        // Reading sensors data
                        for (MultiSenseSensors sensorSample : multiSenseDevice.getSensors()) {
                            if (sensorSample.getTemperature() != null) {
                                System.out.println("TEMP: " + sensorSample.getTemperature());
                            }
                            if (sensorSample.getBatteryLevel() != null) {
                                System.out.println("BATTERY: " + sensorSample.getBatteryLevel());
                            }
                            if (sensorSample.getLight() != null) {
                                System.out.println("LIGHT: " + sensorSample.getLight());
                            }
                        }
                    }
                });
            } else {
                scanning = false;
                startStopBtn.setText(R.string.start);
                scanStatus.setText(R.string.scan_stopped);
                multiSenseScanner.stopScan();
                multiSenseObserver.stopObserveTags();
            }
        });

    }

    public void checkLocationPermissions() {
        //if we have permission to access to gps location
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //start location service
            //startLocationService();
        } else {
            //If do not have location access then request permissions
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.i("CODE", String.valueOf(requestCode));
        for (String p : permissions)
            Log.i("PERMISSIONS", String.valueOf(p));
        for (int g : grantResults)
            Log.i("RESULTS", String.valueOf(g));
        boolean granted = false;
        if (requestCode == 1) {
            //For each permission requested
            for (int i = 0, len = permissions.length; i < len; i++) {
                String permission = permissions[i];
                // If user denied the permission
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    //Check if you asked for the same permissions before
                    boolean showRationale = shouldShowRequestPermissionRationale(permission);
                    //If user cheked "nevers ask again"
                    if (!showRationale) {
                        Toast.makeText(this, "GPS Location access was rejected", Toast.LENGTH_LONG);
                    }
                    //If user hasn't checked for "never ask again"
                    else checkLocationPermissions();
                }
                //user grants permissions
                else granted = true;
            }
            //If user grants permissions
            if (granted)
                Toast.makeText(this, "GPS Location access was accepted", Toast.LENGTH_LONG);
            ;

        }
    }
}