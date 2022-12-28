package com.example.pcodmaster;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;

public class Chart extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);

//        Bundle bundle = getIntent().getExtras();
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
}

