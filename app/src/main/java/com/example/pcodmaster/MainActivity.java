package com.example.pcodmaster;

import androidx.annotation.BoolRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IInterface;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    String name = "";
    String addressToConnect = "";
    BluetoothSocket bluetoothSocket = null;
    public OutputStream outputStream = null;
    public InputStream inputStream = null;
    EditText sendBox;
    TextView recieveBox;

    ExternalThread externalThread;

    String inputData = "";

    public MainActivity() {
    }

    public static int shifter(int msb_sample, int mid_sample, int lsb_sample) {
        int temp = 0;
        msb_sample = 0x00000003 & msb_sample;
        msb_sample = msb_sample << 16;
        temp = temp | msb_sample;
        mid_sample = mid_sample << 8;
        temp = temp | mid_sample;
        temp = temp | lsb_sample;
        temp = temp - 1;
        temp = ~temp;
        return temp;
    }

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
            Log.e("Packet Error", "Packet Not Recieved: " + String.valueOf(packetCode));
            e.printStackTrace();
        }

        return data;
    }

    public ArrayList<Integer> recieveECG() throws IOException {
        outputStream.write((byte) 1);
//        inputStream.skip(inputStream.available());
        int packetCounter = inputStream.read();// = 3
//        inputStream.skip(inputStream.available());
        int packetSize = inputStream.read(); //  = 18

//
        externalThread.kill = false;
        Toast.makeText(getApplicationContext(), "PacketSize: " + String.valueOf(packetSize), Toast.LENGTH_SHORT).show();

        ArrayList<Integer> ret = externalThread.run(packetSize, inputStream);
        Toast.makeText(getApplicationContext(), "Size: " + ret.size(), Toast.LENGTH_SHORT).show();
        outputStream.write((byte) 2);
        return ret;
    }

//    public int heartRate() throws IOException {
//        sendData((byte)2);
//        inputStream.skip(inputStream.available());
//        int hr = inputStream.read() & 0xff;
//        return hr;
//    }

    public void sendData(byte instruction) throws IOException {
        outputStream.write(instruction);
    }


    @SuppressLint("MissingPermission")
    public boolean connect(BluetoothDevice myDevice) {

        final UUID mUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
        int counter = 0;
        boolean ret = false;
        do {
            try {
//                            System.out.println(myDevice.toString());
                bluetoothSocket = myDevice.createRfcommSocketToServiceRecord(mUUID);
//                            System.out.println(bluetoothSocket);
                bluetoothSocket.connect();
//                System.out.println(bluetoothSocket.isConnected());
                if (bluetoothSocket.isConnected()) {
                    Toast.makeText(this, "Connected to " + myDevice.getName(), Toast.LENGTH_SHORT).show();

                }
            } catch (IOException e) {
                Toast.makeText(this, "Cannot connect to " + myDevice.getName() + ". Please retry.", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
            counter++;
        }
        while (!bluetoothSocket.isConnected() && counter < 5);


        try {
            outputStream = bluetoothSocket.getOutputStream();
            inputStream = bluetoothSocket.getInputStream();
            ret = true;

        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "bluetooth socket error", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        return ret;
    }

    @SuppressLint("MissingPermission")

    @Override
    protected void onPause() {

        super.onPause();
        externalThread.kill = true;
    }


    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        externalThread = new ExternalThread();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Button ecg = findViewById(R.id.ecgBut);
        Button temp = findViewById(R.id.tempBut);

        ecg.setEnabled(false);
        temp.setEnabled(false);

        Button connect = findViewById(R.id.connectBut);
        Button camStream = findViewById(R.id.CamButton);

        Button send = findViewById(R.id.send);
        send.setEnabled(false);

        sendBox = findViewById(R.id.sendData);
        recieveBox = findViewById(R.id.recieveData);


        TextView dispHr = findViewById(R.id.hr);
        EditText dispTemp = findViewById(R.id.temp);
        EditText dispAmbtemp = findViewById(R.id.ambtemp);
        TextView getName = findViewById(R.id.divName);


        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                byte data = Byte.parseByte(sendBox.getText().toString());
                try {
                    sendData(data);
                } catch (IOException e) {
                    e.printStackTrace();
                }
//                new CountDownTimer(300, 10) {
//                    public void onFinish() {
//                        Toast.makeText(getApplicationContext(), "Reciving", Toast.LENGTH_SHORT).show();
//                        try {
//                            inputData = inputData + " " +String.valueOf(inputStream.read() & 0xff);
//
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                        recieveBox.setText(String.valueOf(inputData));
//                    }
//
//                    public void onTick(long millisUntilFinished) {
//                        // millisUntilFinished    The amount of time until finished.
//                    }
//                }.start();

            }
        });

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        bluetoothAdapter.startDiscovery();

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BluetoothDevice myDevice = null;
                name = getName.getText().toString();

                Toast.makeText(getApplicationContext(), "Connecting to " + name, Toast.LENGTH_SHORT).show();

//                textView.setText("");

                if (pairedDevices.size() > 0) {
                    // Loop through paired device
                    for (BluetoothDevice device : pairedDevices) {
//                        Toast.makeText(getApplicationContext(), device.getName().toString(), Toast.LENGTH_SHORT).show();

                        if (device.getName().toString().equals(name)) {
                            addressToConnect = bluetoothAdapter.getRemoteDevice(device.getAddress()).toString();
//                            Toast.makeText(getApplicationContext(),device.getName().toString() + ": "+ addressToConnect, Toast.LENGTH_LONG).show();
                            myDevice = bluetoothAdapter.getRemoteDevice(addressToConnect);

//                            Toast.makeText(getApplicationContext(), myDevice.toString(), Toast.LENGTH_SHORT).show();
//                            System.out.println(myDevice.getName().toString());
//                            Toast.makeText(getApplicationContext(), bluetoothAdapter.getRemoteDevice(device.getAddress()).toString(), Toast.LENGTH_LONG).show();
                            break;
                        }
                    }

                    if(connect(myDevice)){
                        ecg.setEnabled(true);
                        temp.setEnabled(true);
                        send.setEnabled(true);
                        try {
                            outputStream.write((byte) 2);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
//                        externalThread.run();
                    }
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "no devices connected", Toast.LENGTH_SHORT).show();
                }
            }
        });

        camStream.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getApplicationContext(), stream.class);
//                    System.out.println("Starting Activity");
                startActivity(intent);
            }
        });

        ecg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    inputStream.skip(inputStream.available());
                    ArrayList<Integer> ecg = recieveECG();
//                    recieveBox.setText(ecg.toString());
                    Intent intent = new Intent(MainActivity.this, ECGChart.class);
                    intent.putExtra("list", ecg);
                    startActivity(intent);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        temp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                externalThread.kill = true;

//                try {
//                    sendData((byte)2);
//
//                    String temps;
//
//                    for(int i = 0; i < 9; i ++) //simple delay for 9 seconds
//                    {
//                        try {
//                            Thread.sleep(1000);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }
//
//                    String temp = recieveData(0);
//
//                    String[] parts = temp.split(",");
//                    temp = parts[0];
//                    temps = parts[1];
//
//                    dispTemp.setText(temp);
//                    dispAmbtemp.setText(temps);
//                    Toast.makeText(getApplicationContext(), "done", Toast.LENGTH_SHORT).show();
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
            }
        });

    }
}

class ExternalThread extends Thread{

    Boolean kill = false;

    MainActivity mainActivity = new MainActivity();
//    OutputStream outputStream = mainActivity.outputStream ;
//    InputStream inputStream = mainActivity.inputStream;

    String receiveData = null;

    public ArrayList< Integer > run(int packetSize, InputStream inputStream) throws IOException {

            ArrayList<Integer> list = new ArrayList<Integer>();
            inputStream.read();

            while(!kill){

                int data[] = new int[packetSize];

                for(int i = 0; i < packetSize; i ++){
                    data[i] = (int) inputStream.read();

//            Toast.makeText(getApplicationContext(), String.valueOf(data[i]), Toast.LENGTH_SHORT).show();
                }

                int k = 0;
                for(int i = 0; i < packetSize; i+=3){
                    list.add(mainActivity.shifter(data[i], data[i+1], data[i+2]));
//                    if(list.get(list.size()) >= 5000)
//                        kill = true;
                }

                if(list.size() >= 6000)
                        kill = true;

        }
        return list;
    }
}

