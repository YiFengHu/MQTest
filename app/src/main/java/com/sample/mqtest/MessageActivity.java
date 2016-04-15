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

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;
import java.util.List;

public class MessageActivity extends AppCompatActivity implements
        MQModel.MqttMessageListener, MQModel.MqttDeliveryCompleteListener,
        View.OnClickListener{

    private List<String> historyMessages = new ArrayList<>(50);

    private ListView historyMessageListView;
    private Button sendButton;
    private EditText contentEditText;
    private EditText topicEditText;

    private HistoryMessageAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        initLayout();

        MQModel.addMsgListener(this);
        MQModel.addDeliveryListener(this);
    }

    private void initLayout() {
        historyMessageListView = (ListView) findViewById(R.id.message_history);
        sendButton = (Button) findViewById(R.id.message_sendButton);
        sendButton.setOnClickListener(this);
        contentEditText = (EditText) findViewById(R.id.message_contentEditText);
        topicEditText = (EditText) findViewById(R.id.message_topicEditText);
        topicEditText.setText("MQ Test");

        adapter = new HistoryMessageAdapter(this, new ArrayList<String>());
    }

    @Override
    protected void onDestroy() {
        MQModel.removeMsgListener(this);
        MQModel.removeDeliveryListener(this);
        super.onDestroy();
    }

    @Override
    public void onMessageArrived(String topic, MqttMessage message) {
        if(adapter!=null){
            adapter.addMessage("Topic: "+topic+", Content: "+new String(message.getPayload()));
        }
    }

    @Override
    public void onDeliveryComplete(IMqttDeliveryToken token) {
        if(adapter!=null){
            try {
                adapter.addMessage(new String(token.getMessage().getPayload()));
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.message_sendButton:
                sendMessage();
                break;
        }
    }

    private void sendMessage() {
        if(isContentValid(contentEditText.getText())
                && isContentValid(topicEditText.getText())) {
            String content = contentEditText.getText().toString();
            String topic = topicEditText.getText().toString();

            MQModel.sendMessage(topic, content);
        }else{
            Toast.makeText(this, "Must input both message and topic", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isContentValid(Editable text) {
        return text!=null && !text.toString().isEmpty();
    }
}
