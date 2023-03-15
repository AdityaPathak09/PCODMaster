package com.example.pcodmaster;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ThemedSpinnerAdapter;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class LiveGraph extends AppCompatActivity {


    public LineChart lineChart;

    public LineData data;
    public LineDataSet LDS1, LDS2, LDS3, AVRDS, AVLDS, AVFDS, V2DS, V3DS;

    private static InputStream inputStream;
    private static OutputStream outputStream;


    public ArrayList<Entry> L1 = new ArrayList<>(), L2 = new ArrayList<>(), L3 = new ArrayList<>(), AVR = new ArrayList<>(), AVL = new ArrayList<>(), AVF = new ArrayList<>(), V2 = new ArrayList<>(), V3 = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_graph);

        inputStream = Helper.getInstance().getInputStream();
        outputStream = Helper.getInstance().getOutputStream();
        lineChart = findViewById(R.id.liveGraph);
        setupData();
        setupChart();
        setupAxes();
        setLegend();


        lineChart.postDelayed(new Runnable() {

            long time = System.currentTimeMillis();
            @Override
            public void run() {

                String dataRecieved = recieveData((byte)1);
                try {
                    LDS1.addEntry(new Entry(L1.size(), (float) Integer.parseInt(dataRecieved.substring(dataRecieved.indexOf('a') + 1, dataRecieved.indexOf('b')))));
                    LDS2.addEntry(new Entry(L2.size(), (float) Integer.parseInt(dataRecieved.substring(dataRecieved.indexOf('b') + 1, dataRecieved.indexOf('c')))));
                    LDS3.addEntry(new Entry(L3.size(), (float) Integer.parseInt(dataRecieved.substring(dataRecieved.indexOf('c') + 1, dataRecieved.indexOf('d')))));
                    AVRDS.addEntry(new Entry(AVR.size(), (float) Integer.parseInt(dataRecieved.substring(dataRecieved.indexOf('d') + 1, dataRecieved.indexOf('e')))));
                    AVLDS.addEntry(new Entry(AVL.size(), (float) Integer.parseInt(dataRecieved.substring(dataRecieved.indexOf('e') + 1, dataRecieved.indexOf('f')))));
                    AVFDS.addEntry(new Entry(AVF.size(), (float) Integer.parseInt(dataRecieved.substring(dataRecieved.indexOf('f') + 1, dataRecieved.indexOf('g')))));
                    V2DS.addEntry(new Entry(V2.size(), (float) Integer.parseInt(dataRecieved.substring(dataRecieved.indexOf('g') + 1, dataRecieved.indexOf('h')))));
                    V3DS.addEntry(new Entry(V3.size(), (float) Integer.parseInt(dataRecieved.substring(dataRecieved.indexOf('h') + 1))));

                    data.notifyDataChanged();
//
                    lineChart.notifyDataSetChanged();
                    lineChart.setVisibleXRange(0, 60);
                    lineChart.moveViewToX(data.getEntryCount());
                }
                catch (Exception e){

                }

                lineChart.postDelayed(this, 5);
            }
        }, 5);


//

//        Runnable mRunnable = () -> {
//            double i = 0;
//            while(true) {
//                try {
//                    LDS1.addEntry(new Entry(L1.size(), (float) Math.sin(i)));
//                    LDS2.addEntry(new Entry(L2.size(), (float) Math.sin(i) + 1));
//                    LDS3.addEntry(new Entry(L3.size(), (float) Math.sin(i) + 2));
//                    AVRDS.addEntry(new Entry(AVR.size(), (float) Math.sin(i) + 3));
//                    AVLDS.addEntry(new Entry(AVL.size(), (float) Math.sin(i) + 4));
//                    AVFDS.addEntry(new Entry(AVF.size(), (float) Math.sin(i) + 5));
//                    V2DS.addEntry(new Entry(V2.size(), (float) Math.sin(i) + 6));
//                    V3DS.addEntry(new Entry(V3.size(), (float) Math.sin(i) + 7));
//                    i+= 0.1;
//                    data.notifyDataChanged();
////
//                    lineChart.notifyDataSetChanged();
////                    lineChart.moveViewToX(50);
//                    lineChart.setVisibleXRange(0, 60);
//                    lineChart.moveViewToX(data.getEntryCount());
//                    Thread.sleep(50);
//                } catch (Exception e) {
//                    Log.e("Catch","Error From Catch");
//                    e.printStackTrace();
//                }
//            }
//        };

//        Executor mExecutor = Executors.newSingleThreadExecutor();
//            mExecutor.execute(mRunnable);

    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onPause() {
        Intent intent = new Intent(LiveGraph.this, MainActivity.class);
        startActivity(intent);
        super.onPause();
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onStop() {
        super.onStop();
        Intent intent = new Intent(LiveGraph.this, MainActivity.class);
        startActivity(intent);
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent intent = new Intent(LiveGraph.this, MainActivity.class);
        startActivity(intent);
    }

    private void setupChart() {
        // disable description text
        lineChart.getDescription().setEnabled(false);
        // enable touch gestures
        lineChart.setTouchEnabled(true);
        // if disabled, scaling can be done on x- and y-axis separately
//        lineChart.setPinchZoom(true);
        // enable scaling
//        lineChart.setScaleEnabled(true);
        lineChart.setDrawGridBackground(false);
        // set an alternative background color
        lineChart.setBackgroundColor(Color.DKGRAY);
        // set background color
        lineChart.setBackgroundColor(Color.rgb(255, 255, 255));
        lineChart.setVisibleXRangeMaximum(100);
//        lineChart.setScrollX(500);
    }

    private void setupAxes() {
        XAxis xl = lineChart.getXAxis();
        xl.setTextColor(Color.BLACK);
        xl.setDrawGridLines(true);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true); //vertical

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setEnabled(true); //left  axis
//        leftAxis.setAxisMaximum(TOTAL_MEMORY);
//        leftAxis.setAxisMinimum(0f);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setEnabled(false); //right axis

        // Add a limit line
//        LimitLine ll = new LimitLine(LIMIT_MAX_MEMORY, "Upper Limit");
//        ll.setLineWidth(2f);
//        ll.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
//        ll.setTextSize(10f);
//        ll.setTextColor(Color.BLACK);
        // reset all limit lines to avoid overlapping lines
//        leftAxis.removeAllLimitLines();
//        leftAxis.addLimitLine(ll);
        // limit lines are drawn behind data (and not on top)
//        leftAxis.setDrawLimitLinesBehindData(true);
    }

    private void setupData() {

        L1.add(new Entry(0, (float) 0F));
        L2.add(new Entry(0, (float) 0F));
        L3.add(new Entry(0, (float) 0F));
        AVR.add(new Entry(0, (float) 0F));
        AVL.add(new Entry(0, (float) 0F));
        AVF.add(new Entry(0, (float) 0F));
        V2.add(new Entry(0, (float) 0F));
        V3.add(new Entry(0, (float) 0F));


        LDS1 = new LineDataSet(L1, "L1");
        LDS2 = new LineDataSet(L2, "L2");
        LDS3 = new LineDataSet(L3, "L3");
        AVRDS = new LineDataSet(AVR, "AVR");
        AVLDS = new LineDataSet(AVL, "AVL");
        AVFDS = new LineDataSet(AVF, "AVF");
        V2DS = new LineDataSet(V2, "V2");
        V3DS = new LineDataSet(V3, "V3");
        //We add features to our chart
        LDS1.setColor(Color.rgb(255, 0, 0));
        LDS1.setDrawCircles(false);
        LDS1.setDrawValues(false);
        LDS1.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LDS2.setColor(Color.rgb(0, 255, 0));
        LDS2.setDrawCircles(false);
        LDS2.setDrawValues(false);
        LDS2.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LDS3.setColor(Color.rgb(0, 0, 255));
        LDS3.setDrawCircles(false);
        LDS3.setDrawValues(false);
        LDS3.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        AVRDS.setColor(Color.rgb(255, 0, 255));
        AVRDS.setDrawCircles(false);
        AVRDS.setDrawValues(false);
        AVRDS.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        AVLDS.setColor(Color.rgb(0, 255, 255));
        AVLDS.setDrawCircles(false);
        AVLDS.setDrawValues(false);
        AVLDS.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        AVFDS.setColor(Color.rgb(0, 0, 0));
        AVFDS.setDrawCircles(false);
        AVFDS.setDrawValues(false);
        AVFDS.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        V2DS.setColor(Color.rgb(128, 0, 0));
        V2DS.setDrawCircles(false);
        V2DS.setDrawValues(false);
        V2DS.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        V3DS.setColor(Color.rgb(0, 128, 0));
        V3DS.setDrawCircles(false);
        V3DS.setDrawValues(false);
        V3DS.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        List<ILineDataSet> lines = new ArrayList<ILineDataSet>();
        lines.add(LDS1);
        lines.add(LDS2);
        lines.add(LDS3);
        lines.add(AVRDS);
        lines.add(AVLDS);
        lines.add(AVFDS);
        lines.add(V2DS);
        lines.add(V3DS);

        data = new LineData(lines);

        // add empty data
        lineChart.setData(data);
    }

    private void setLegend() {
        // get the legend (only possible after setting data)
        Legend l = lineChart.getLegend();

        // modify the legend ...
//        l.setDirection(Legend.LegendDirection.LEFT_TO_RIGHT);
        l.setForm(Legend.LegendForm.CIRCLE);
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
//        l.setVerticalAlignment(Legend.LegendVerticalAlignment.CENTER);
        l.setTextColor(Color.BLACK);
    }

    public String recieveData(int packetCode) { //1
        String data = "";

        try {
//            inputStream.skip(inputStream.available());
//            int size = inputStream.read();
            while (true){
//            for (int i = 0; i < size; i++) {
                char d = (char) inputStream.read();
                if(d == 'l')
                    break;
                data = data + d;
            }
//            Toast.makeText(getApplicationContext(), "Size: " + size, Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            Log.e("Packet Error", "Packet Not Recieved: " + packetCode);
            e.printStackTrace();
        }

//        Log.e("Data", data);
        return data;
    }


}
