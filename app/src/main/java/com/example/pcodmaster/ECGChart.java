package com.example.pcodmaster;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Binder;
import android.os.Bundle;
import android.renderscript.ScriptGroup;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

public class ECGChart extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ecgchart);

        ArrayList<Integer> list = getIntent().getIntegerArrayListExtra("list");

        Toast.makeText(getApplicationContext(), list.toString(), Toast.LENGTH_SHORT).show();

        LineChart lineChart = findViewById(R.id.linechart);

        lineChart.setBackgroundColor(Color.rgb(255,255,255));

        Object ArrayList;
        ArrayList<Entry> linevalues = new ArrayList<Entry>();
        for(int i = 0; i < 100;  i ++){
            linevalues.add(new Entry((float) i, (float) list.get(i)));
        }

        LineDataSet linedataset = new LineDataSet(linevalues, "ECG Wave Form");
        //We add features to our chart
        linedataset.setColor(Color.rgb(255, 0, 0));
        linedataset.setDrawCircles(false);
        linedataset.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        //We connect our data to the UI Screen
        LineData data = new LineData(linedataset);


        lineChart.setData(data);

    }
}

