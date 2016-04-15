/*******************************************************************
 * Copyright  (C) Newegg Corporation. All rights reserved.
 * <p/>
 * Author: Roder.Y.Hu (Roder.Y.Hu@newegg.com)
 * Create Date: 2016/4/15
 * Usage:
 * <p/>
 * RevisionHistory
 * Date    		Author    Description
 ********************************************************************/
package com.sample.mqtest;

import android.app.Activity;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.ArrayList;
import java.util.List;

public class MQModel{
    private static final MemoryPersistence persistence = new MemoryPersistence();

    private static String broker = "tcp://10.16.198.5:1883";
    private static final int qos = 2;
    private static String clientId;

    private static MqttClient sampleClient;
    private static MqttConnectOptions connOpts;

    private static final List<String> topics = new ArrayList<>(5);
    private static final List<MqttMessageListener> messageListeners = new ArrayList<>(5);
    private static final List<MqttConnectionListener> connectionListeners = new ArrayList<>(5);
    private static final List<MqttDeliveryCompleteListener> deliveryListeners = new ArrayList<>(5);

    public static MqttCallback callback = new MqttCallback() {
        @Override
        public void connectionLost(Throwable cause) {
            notifyConnectionListeners(cause);
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            notifyMessageListeners(topic, message);
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            notifyDeliveryListeners(token);
        }
    };

    public static void connect(final String broker, String id, final Activity activity, final ConnectCallback connectCallback){
        MQModel.broker = broker;
        clientId = id;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    sampleClient = new MqttClient(broker, clientId, persistence);
                    connOpts = new MqttConnectOptions();
                    connOpts.setCleanSession(true);
                    Log.d("mq", "Connecting to broker: " + broker);

                    sampleClient.connect(connOpts);
                    sampleClient.setCallback(callback);

                    if(connectCallback!=null && activity!=null){
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                connectCallback.onConnected();
                            }
                        });
                    }
                } catch (MqttException e) {
                    e.printStackTrace();

                } catch(final Exception e1){
                    e1.printStackTrace();
                    if(connectCallback!=null && activity!=null){
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                connectCallback.onFail(e1);
                            }
                        });
                    }
                }
            }
        }).start();
    }

    public static void subscribe(String topic){
        synchronized (topics) {
            if(!topics.contains(topic)) {
                try {
                    sampleClient.subscribe(topic);
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void addMsgListener(MqttMessageListener listener){
        synchronized (messageListeners){
            if(!messageListeners.contains(listener)){
                messageListeners.add(listener);
            }
        }
    }

    public static void removeMsgListener(MqttMessageListener listener){
        synchronized (messageListeners){
            if(messageListeners.contains(listener)){
                messageListeners.remove(listener);
            }
        }
    }

    public static void notifyMessageListeners(String topic, MqttMessage message){
        synchronized (messageListeners){
            for(MqttMessageListener listener : messageListeners){
                listener.onMessageArrived(topic, message);
            }
        }
    }

    public static void addConnectionListener(MqttConnectionListener listener){
        synchronized (connectionListeners){
            if(!connectionListeners.contains(listener)){
                connectionListeners.add(listener);
            }
        }
    }

    public static void removeConnectionListener(MqttConnectionListener listener){
        synchronized (connectionListeners){
            if(connectionListeners.contains(listener)){
                connectionListeners.remove(listener);
            }
        }
    }

    public static void notifyConnectionListeners(Throwable throwable){
        synchronized (connectionListeners){
            for(MqttConnectionListener listener : connectionListeners){
                listener.onConnectionLost(throwable);
            }
        }
    }

    public static void addDeliveryListener(MqttDeliveryCompleteListener listener){
        synchronized (deliveryListeners){
            if(!deliveryListeners.contains(listener)){
                deliveryListeners.add(listener);
            }
        }
    }

    public static void removeDeliveryListener(MqttDeliveryCompleteListener listener){
        synchronized (deliveryListeners){
            if(deliveryListeners.contains(listener)){
                deliveryListeners.remove(listener);
            }
        }
    }

    public static void notifyDeliveryListeners(IMqttDeliveryToken token){
        synchronized (deliveryListeners){
            for(MqttDeliveryCompleteListener listener : deliveryListeners){
                listener.onDeliveryComplete(token);
            }
        }
    }

    public static void sendMessage(String topic, String content){
        subscribe(topic);

        try {
            MqttMessage message = new MqttMessage(content.getBytes());
            message.setQos(qos);
            sampleClient.publish(topic, message);
            Log.d("mq", "Message published");
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public static void disconnect(){
        try {
            sampleClient.disconnect();
            Log.d("mq", "Disconnected");

        } catch (MqttException e) {
            e.printStackTrace();
        }

    }

    public static String getClientId() {
        return clientId;
    }

    public static String getBroker() {
        return broker;
    }

    public interface MqttMessageListener{
        void onMessageArrived(String topic, MqttMessage message);
    }

    public interface MqttConnectionListener{
        void onConnectionLost(Throwable cause);
    }

    public interface MqttDeliveryCompleteListener{
        void onDeliveryComplete(IMqttDeliveryToken token);
    }

    public interface ConnectCallback{
        void onConnected();
        void onFail(Throwable throwable);
    }
}
