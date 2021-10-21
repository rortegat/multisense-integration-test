package com.example.multisensepruebas;

import com.cellocator.nano.android.sdk.MultiSenseObserverCallback;
import com.cellocator.nano.android.sdk.MultiSenseReadingLoggerStatus;
import com.cellocator.nano.android.sdk.model.MultiSenseDevice;
import com.cellocator.nano.android.sdk.model.MultiSenseSensors;

import java.util.ArrayList;
import java.util.List;

public class CustomObserverCallback implements MultiSenseObserverCallback {
    @Override
    public void onReadingLoggerStatusChange(String s, MultiSenseReadingLoggerStatus multiSenseReadingLoggerStatus) {
        System.out.println(s);
        System.out.println("LOGGER: " + multiSenseReadingLoggerStatus.getStatus());
    }

    @Override
    public void onError(int i, String s) {
        System.out.println("OBSERVER ERROR: " + i + " :: "+s);
    }

    @Override
    public void onChange(MultiSenseDevice multiSenseDevice) {

        System.out.println(multiSenseDevice.getConfiguration().toString());
        // Reading device general info
        String mac = multiSenseDevice.getAddress();
        String name = multiSenseDevice.getName();
        int rssi = multiSenseDevice.getRssi();
        String firmwareRevision =
                multiSenseDevice.getAdvertisement().getFirmwareVersionName();
        String hardwareRevision =
                multiSenseDevice.getAdvertisement().getOtaVersionName();

// Reading sensors data
        List<Float> temperatureSamples = new ArrayList<>();
        List<Float> humiditySamples = new ArrayList<>();
        for(MultiSenseSensors sensorSample : multiSenseDevice.getSensors()) {
            if (sensorSample.getTemperature() != null) {
                sensorSample.getTemperature();
                temperatureSamples.add(sensorSample.getTemperature());
            }
            if (sensorSample.getHumidity() != null) {
                sensorSample.getHumidity();
                humiditySamples.add(sensorSample.getHumidity());
            }
        }
        System.out.println(mac +" -> "+name);
    /*    System.out.println("DEVICE: " + multiSenseDevice.getName()
                + " MAC: " + multiSenseDevice.getAddress()
                + " SETTINGS: " + multiSenseDevice.getSettings()
                + " DATALOG: " + multiSenseDevice.getLoggerData()
                + " RSSI: " + multiSenseDevice.getRssi()
                + " SENSORS: " + multiSenseDevice.getSensors()//!=null ? multiSenseDevice.getSensors().stream().filter(Objects::nonNull).map(MultiSenseSensors::getTemperature).findFirst().orElse(null) : null
                + " ADVERTISEMENT: " + multiSenseDevice.getAdvertisement());*/

    }
}
