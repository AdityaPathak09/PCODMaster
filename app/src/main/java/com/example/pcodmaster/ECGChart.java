package com.example.pcodmaster;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ECGChart extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ecgchart);

        Bundle bundle = getIntent().getExtras();
        ArrayList<String> list = (ArrayList<String>) getIntent().getSerializableExtra("list");

        Toast.makeText(getApplicationContext(), list.toString(), Toast.LENGTH_SHORT).show();

        LineChart lineChart = findViewById(R.id.linechart);


    }
}