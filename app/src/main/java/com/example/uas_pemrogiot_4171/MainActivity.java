package com.example.uas_pemrogiot_4171;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements MqttCallback {

    MqttClient client = null;
    private static final String MQTT_MESSAGE_NYALA = "nyala";
    ImageView ldrimageview;

    LineChart HumidityChart;
    LineChart Temperaturechart;
    private TextView ldrTextView;
    private TextView slotTextView;
    private int parkingSlot = 15;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectToMQTTBroker();

        Log.d("MQTT", "subscribed");

        ldrimageview = findViewById(R.id.ldrImageView);

        HumidityChart = findViewById(R.id.HumidityChart);
        Temperaturechart = findViewById(R.id.TemperatureChart);
        ldrTextView = findViewById(R.id.ldrTextView);
        slotTextView = findViewById(R.id.slotTextView);

        LineData data = new LineData(getLabel(10));
        HumidityChart.setData(data);
        HumidityChart.animateXY(4000, 4000);
        HumidityChart.invalidate();

        LineData Tempdata = new LineData(getLabel(10));
        Temperaturechart.setData(Tempdata);
        Temperaturechart.animateXY(4000, 4000);
        Temperaturechart.invalidate();



    }

    private void connectToMQTTBroker() {
        try {
            client = new MqttClient("tcp://192.168.41.71:1883", "asdasd", new MemoryPersistence());
            client.setCallback(this);
            client.connect();

            // Re-subscribe to topics after reconnection if needed

            client.subscribe("4171/ldr");
            client.subscribe("4171/dht11/temp");
            client.subscribe("4171/dht11/hum");
            client.subscribe("4171/random");

            Log.d("MQTT", "Connected to MQTT broker");
        } catch (MqttException e) {
            e.printStackTrace();
            Log.d("MQTT", "Connection to MQTT broker failed");
        }
    }

    private ArrayList getLabel(int n) {
        ArrayList xLabel = new ArrayList();
        for(int i = 0; i < n; i++) {
            xLabel.add("n: " + Integer.toString(i + 1));
        }
        return xLabel;
    }

    @Override
    public void connectionLost(Throwable cause) {
        Log.d("MQTT", "Connection lost: " + cause.getMessage());
        // Handle reconnection logic here
        connectToMQTTBroker();
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        Log.d("MQTT", "topic: " + topic);
        Log.d("MQTT", "message: " + message);
        float value = Float.parseFloat(String.valueOf(message));


        if (topic.equals("4171/ldr")) {
            // Update ldr line chart with the received data
            // ...
            float ldrValue = value;
            Log.d("MQTT", "ldrValue: " + ldrValue);
            if (ldrValue < 50) {
                Log.d("MQTT", "Inside ldrValue < 50 condition");
                ldrimageview.setImageResource(R.drawable.lamp_on);
                ldrTextView.setText("Ruangan Gelap Lampu akan Menyala" );
            } else {
                Log.d("MQTT", "Inside ldrValue >= 50 condition");
                ldrimageview.setImageResource(R.drawable.lamp_off);
                ldrTextView.setText("Ruangan Terang Lampu mati");
            }
            ldrTextView.setVisibility(View.VISIBLE); // Set the visibility explicitly


        } else if (topic.equals("4171/dht11/temp")) {
            // Update temperature line chart with the received data
            // ...
            updateFlowLineChart(value);
        } else if (topic.equals("4171/dht11/hum")) {
            // Update humidity line chart with the received data
            // ...
            updateHumLineChart(value);
        } else if (topic.equals("4171/random")){
            int randomValue = (int) value; // Konversi float ke int

            slotTextView.setText("Jumlah Pengunjung: " + randomValue);
        }
    }


    private void updateFlowLineChart(float value) {
        LineData lineData = Temperaturechart.getData();

        if (lineData != null) {
            ILineDataSet dataSet = lineData.getDataSetByIndex(0);

            if (dataSet == null) {
                dataSet = createDataSet();
                lineData.addDataSet(dataSet);
            }

            int entryCount = dataSet.getEntryCount();

            if (entryCount >= 10) {
                //dataSet.removeEntry(0);
                for (int i = 0; i < 5; i++) {
                    dataSet.removeEntry(i);
                }

                Entry newEntry = new Entry(value, 0);
                dataSet.addEntry(newEntry);
            } else {
                Entry newEntry = new Entry(value, entryCount);
                dataSet.addEntry(newEntry);
            }

            lineData.notifyDataChanged();
            Temperaturechart.notifyDataSetChanged();
            Temperaturechart.setVisibleXRangeMaximum(10);
            Temperaturechart.moveViewToX(-1);
        }
    }
    private void updateHumLineChart(float value) {
        LineData lineData = HumidityChart.getData();

        if (lineData != null) {
            ILineDataSet dataSet = lineData.getDataSetByIndex(0);

            if (dataSet == null) {
                dataSet = createDataSet2();
                lineData.addDataSet(dataSet);
            }

            int entryCount = dataSet.getEntryCount();

            if (entryCount >= 10) {
                //dataSet.removeEntry(0);
                for (int i = 0; i < 10; i++) {
                    dataSet.removeEntry(i);
                }

                Entry newEntry = new Entry(value, 0);
                dataSet.addEntry(newEntry);
            } else {
                Entry newEntry = new Entry(value, entryCount);
                dataSet.addEntry(newEntry);
            }

            lineData.notifyDataChanged();
            HumidityChart.notifyDataSetChanged();
            HumidityChart.setVisibleXRangeMaximum(10);
            HumidityChart.moveViewToX(-1);
        }
    }

    private LineDataSet createDataSet() {
        LineDataSet dataSet = new LineDataSet(null, "Temperature");
        dataSet.setColor(Color.BLUE);
        dataSet.setCircleColor(Color.BLUE);
        dataSet.setDrawValues(false);
        return dataSet;
    }
    private LineDataSet createDataSet2() {
        LineDataSet dataSet = new LineDataSet(null, "Humidity");
        dataSet.setColor(Color.GREEN);
        dataSet.setCircleColor(Color.GREEN);
        dataSet.setDrawValues(false);
        return dataSet;
    }



    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }

    public void publishNyala(View view) {
        if (client != null && client.isConnected()) {
            try {
                MqttMessage message = new MqttMessage();
                message.setPayload(MQTT_MESSAGE_NYALA.getBytes());
                client.publish("4171/relay", message);
                Toast.makeText(this, "Pengunjung Keluar", Toast.LENGTH_SHORT).show();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }
    public void publishMati(View view) {
        if (client != null && client.isConnected()) {
            try {
                MqttMessage message = new MqttMessage();
                message.setPayload("mati".getBytes());
                client.publish("4171/relay", message);
                Toast.makeText(this, "Pengunjung Masuk", Toast.LENGTH_SHORT).show();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }


}