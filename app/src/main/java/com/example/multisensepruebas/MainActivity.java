package com.example.multisensepruebas;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
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
import com.example.multisensepruebas.model.Location;
import com.example.multisensepruebas.model.Measurement;
import com.example.multisensepruebas.model.MultiSenseBeacon;
import com.example.multisensepruebas.service.CustomMqttService;
import com.example.multisensepruebas.util.MacAddressUtils;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;

import java.net.NetworkInterface;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiresApi(api = Build.VERSION_CODES.Q)
public class MainActivity extends AppCompatActivity {

    private final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    private List<MultiSenseBeacon> beacons;
    private List<String> macAddresses;
    private FusedLocationProviderClient fusedLocationClient;
    private Location mLocation;
    private CustomMqttService customMqttService;
    private Long deviceId;


    private Button startStopBtn;
    private EditText hostInput;
    private TextView scanStatus;

    private boolean scanning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkLocationPermissions();

        scanStatus = findViewById(R.id.textStatus);
        scanStatus.setMovementMethod(new ScrollingMovementMethod());
        startStopBtn = findViewById(R.id.buttonStartStop);
        hostInput = findViewById(R.id.inputHost);

        mLocation = new Location();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        customMqttService = new CustomMqttService(this);

        //Get device MacAddress and convert it from hex to decimal
        deviceId = MacAddressUtils.toLong(getMacAddress());

        MultiSenseManager multiSenseManager = new MultiSenseManager(this);
        MultiSenseScanner multiSenseScanner = multiSenseManager.createScanner();
        MultiSenseObserver multiSenseObserver = multiSenseManager.createObserver();

        startStopBtn.setOnClickListener(v -> {
            if (!scanning) {
                scanning = true;
                startStopBtn.setText(R.string.stop);
                scanStatus.setText(R.string.scan_started);
                customMqttService.connect(hostInput.getText().toString());
                getCurrentLocation();
                macAddresses = new ArrayList<>();
                beacons = new ArrayList<>();
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
                        Log.i("OBSERVER STATUS", String.valueOf(multiSenseReadingLoggerStatus.getPercent()));
                    }

                    @Override
                    public void onError(int i, String s) {
                        Log.e("OBSERVER ERROR", s);
                    }

                    @Override
                    public void onChange(MultiSenseDevice multiSenseDevice) {

                        System.out.println(multiSenseDevice.toString());
                        //Checking if beacon has read before
                        if (!macAddresses.contains(multiSenseDevice.getAddress())) {
                            macAddresses.add(multiSenseDevice.getAddress());

                            MultiSenseBeacon beacon = new MultiSenseBeacon();
                            Map<String, Object> telemetry = new HashMap<>();

                            //Convert mac address hex to decimal
                            beacon.setBeaconId(MacAddressUtils.toLong(multiSenseDevice.getAddress()));

                            // Reading sensors data
                            for (MultiSenseSensors sensorSample : multiSenseDevice.getSensors()) {

                                System.out.println(sensorSample);

                                if (sensorSample.getTemperature() != null)
                                    telemetry.put("temperature", sensorSample.getTemperature());

                                if (sensorSample.getBatteryLevel() != null)
                                    telemetry.put("battery", sensorSample.getBatteryLevel());

                                if (sensorSample.getLight() != null)
                                    telemetry.put("light", sensorSample.getLight());

                                if (sensorSample.getHumidity() != null)
                                    telemetry.put("humidity", sensorSample.getHumidity());

                                if (sensorSample.isOpenPackage() != null)
                                    telemetry.put("openPackage", sensorSample.isOpenPackage());

                                if (sensorSample.getAccelerometerX() != null)
                                    telemetry.put("accX", sensorSample.getAccelerometerX());

                                if (sensorSample.getAccelerometerY() != null)
                                    telemetry.put("accY", sensorSample.getAccelerometerY());

                                if (sensorSample.getAccelerometerZ() != null)
                                    telemetry.put("accZ", sensorSample.getAccelerometerZ());

                                if (sensorSample.getCreateDate() != null)
                                    telemetry.put("datetime", sensorSample.getCreateDate());
                            }

                            beacon.setTelemetry(telemetry);
                            scanStatus.append("\n" + beacon.toString());
                            beacons.add(beacon);
                        }
                    }
                });
            } else {
                scanning = false;
                startStopBtn.setText(R.string.start);
                scanStatus.append("\nScanning stopped");
                multiSenseScanner.stopScan();
                multiSenseObserver.stopObserveTags();
                publishMeasurement();
                customMqttService.disconnect();
            }
        });

    }

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

    @Override
    protected void onResume() {
        super.onResume();

        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }

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

    public void getCurrentLocation() {
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
        fusedLocationClient.getLastLocation().addOnSuccessListener(this.getMainExecutor(), new OnSuccessListener<android.location.Location>() {
            @Override
            public void onSuccess(android.location.Location location) {
                if (location != null) {
                    mLocation.setLatitude(location.getLatitude());
                    mLocation.setLongitude(location.getLongitude());
                }

            }
        });
    }

    private String getMacAddress() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(Integer.toHexString(b & 0xFF)).append(".");
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
            //handle exception
        }
        return "";
    }

    public void publishMeasurement() {
        Measurement measurement = new Measurement();
        measurement.setDeviceId(deviceId);
        measurement.setLocation(mLocation);
        measurement.setBeacons(beacons);
        measurement.setDatetime(DTF.format(ZonedDateTime.now()));

        Gson gson = new Gson();
        String json = gson.toJson(measurement);

        System.out.println(json);

        customMqttService.pub(json);
    }

}