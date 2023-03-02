package com.example.pcodmaster;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class Chart extends AppCompatActivity {

    InputStream inputStream;

    public String recieveData(int packetCode) {
        String data = "";

        try {
//            inputStream.skip(inputStream.available());
            int size = inputStream.read();

            for (int i = 0; i < size; i++) {
                data = data + (char) inputStream.read();
            }
            Toast.makeText(getApplicationContext(), "Size: " + size, Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            Log.e("Packet Error", "Packet Not Recieved: " + packetCode);
            e.printStackTrace();
        }

        return data;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);

        Bundle bundle = getIntent().getExtras();
        inputStream = bundle.getParcelable("inputStream");

        ArrayList<Integer> list = getIntent().getIntegerArrayListExtra("list");

//        Toast.makeText(getApplicationContext(), list.toString(), Toast.LENGTH_SHORT).show();

        LineChart lineChart = findViewById(R.id.linechart);

        lineChart.setBackgroundColor(Color.rgb(255, 255, 255));

        ArrayList<Entry> linevalues = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            linevalues.add(new Entry((float) i, (float) list.get(i)));
        }


        Log.e("data size", String.valueOf(linevalues.size()));

        LineDataSet linedataset = new LineDataSet(linevalues, "Wave Form");
        //We add features to our chart
        linedataset.setColor(Color.rgb(255, 0, 0));
        linedataset.setDrawCircles(false);
        linedataset.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);

        //We connect our data to the UI Screen
        LineData data = new LineData(linedataset);


        lineChart.setData(data);

    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onPause() {
        Intent intent = new Intent(Chart.this, MainActivity.class);
        startActivity(intent);
        super.onPause();
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onStop() {
        super.onStop();
        Intent intent = new Intent(Chart.this, MainActivity.class);
        startActivity(intent);
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent intent = new Intent(Chart.this, MainActivity.class);
        startActivity(intent);
    }

}

