package com.example.multisensepruebas.service;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

public class CustomMqttService {

    private static final String HOST = "192.168.100.6";
    private static final String PORT = "1883";
    private static final String TOPIC = "android/measurement";

    private static final String TAG = "MQTT";
    private MqttAndroidClient client;
    private final Context mContext;

    public CustomMqttService(Context context) {
        this.mContext = context;
    }

    public void connect() {

        String connectionUri = "tcp://" + HOST + ":" + PORT;
        String clientId = MqttClient.generateClientId();

        client = new MqttAndroidClient(mContext, connectionUri, clientId);

        try {
            IMqttToken token = client.connect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Log.d(TAG, "success");
                    Toast.makeText(mContext, "Mqtt connected!!", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Log.d(TAG, "failure: " + exception.getMessage());
                    Toast.makeText(mContext, "Mqtt connection failed", Toast.LENGTH_LONG).show();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        if (client != null && client.isConnected())
            try {
                client.disconnect();
                Toast.makeText(mContext, "Mqtt disconnected!!", Toast.LENGTH_LONG).show();
            } catch (MqttException e) {
                e.printStackTrace();
            }
    }

    public void pub(String msg) {
        try {
            client.publish(TOPIC, msg.getBytes(), 0, false);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
