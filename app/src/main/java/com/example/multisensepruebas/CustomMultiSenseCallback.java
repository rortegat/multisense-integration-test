package com.example.multisensepruebas;

import com.cellocator.nano.android.sdk.MultiSenseDeviceCallback;
import com.cellocator.nano.android.sdk.MultiSenseObserver;
import com.cellocator.nano.android.sdk.model.MultiSenseConfiguration;
import com.cellocator.nano.android.sdk.model.MultiSenseDevice;
import com.cellocator.nano.android.sdk.model.MultiSenseEnabledSensors;

public class CustomMultiSenseCallback implements MultiSenseDeviceCallback {

    private final MultiSenseObserver multiSenseObserver;

    public CustomMultiSenseCallback(MultiSenseObserver multiSenseObserver) {
        this.multiSenseObserver = multiSenseObserver;
    }

    @Override
    public void onError(int i, String s) {
        System.out.println("DEVICE ERROR: " + i);
    }

    @Override
    public void onChange(MultiSenseDevice multiSenseDevice) {

        MultiSenseEnabledSensors enabledSensors = new MultiSenseEnabledSensors
                .Builder()
                .setLight(true)
                .setHumidity(true)
                .create();

        //System.out.println(multiSenseDevice.getConfiguration().toString());

        /*MultiSenseConfiguration multiSenseConfiguration =
                new MultiSenseConfiguration
                        .Builder(multiSenseDevice.getConfiguration())
                        .setSensorMask(enabledSensors)
                        .create();*/

        //multiSenseObserver.saveConfiguration(multiSenseDevice.getAddress(), multiSenseConfiguration);

        //multiSenseObserver.addTag(multiSenseDevice.getAddress());

        /*        System.out.println("DEVICE: " + multiSenseDevice.getName()
                + " MAC: " + multiSenseDevice.getAddress()
                + " SETTINGS: " + multiSenseDevice.getSettings()
                + " DATALOG: " + multiSenseDevice.getLoggerData()
                + " RSSI: " + multiSenseDevice.getRssi()
                + " SENSORS: " + multiSenseDevice.getSensors()//!=null ? multiSenseDevice.getSensors().stream().filter(Objects::nonNull).map(MultiSenseSensors::getTemperature).findFirst().orElse(null) : null
                + " ADVERTISEMENT: " + multiSenseDevice.getAdvertisement());*/
    }


}
