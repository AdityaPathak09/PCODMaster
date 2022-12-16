package com.example.pcodmaster;

import androidx.annotation.BoolRes;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.IInterface;
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
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    String name = "";
    String addressToConnect = "";
    BluetoothSocket bluetoothSocket = null;
    public OutputStream outputStream = null;
    public InputStream inputStream = null;

    public String recieveData(int packetCode)
    {
        String data = "";

        try {
            inputStream.skip(inputStream.available());
            int size = inputStream.read();

            for(int i = 0; i < size; i ++)
            {
                data = data + (char) inputStream.read();
            }
//            Toast.makeText(getApplicationContext(), data, Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            Log.e("Packet Error", "Packet Not Recieved: " + String.valueOf(packetCode));
            e.printStackTrace();
        }

        return data;
    }

    public String recieveECG() throws IOException {
        sendData((byte)1);
        inputStream.skip(inputStream.available());
        int packetCounter = inputStream.read();
        String data = "";
        for(int i = 0; i < packetCounter; i++){
            data = data + recieveData(i+1);
        }
        return data;
    }

    public int heartRate() throws IOException {
        sendData((byte)2);
        inputStream.skip(inputStream.available());
        int hr = inputStream.read();
        return hr;
    }

    public void sendData(byte instruction) throws IOException {
//        System.out.println("Sending data");
        outputStream.write(instruction);
//        System.out.println("data sent");
    }


    @SuppressLint("MissingPermission")
    public boolean connect(BluetoothDevice myDevice)
    {

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
                if(bluetoothSocket.isConnected())
                {
                    Toast.makeText(this, "Connected to " +myDevice.getName(), Toast.LENGTH_SHORT).show();
                    ret = true;
                }
            } catch (IOException e) {
                Toast.makeText(this, "Cannot connect to " +myDevice.getName() +". Please retry.", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
            counter++;
        }
        while(!bluetoothSocket.isConnected() && counter < 5);


        try {
            outputStream = bluetoothSocket.getOutputStream();
            inputStream = bluetoothSocket.getInputStream();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return ret;
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Button ecg = findViewById(R.id.ecgBut);
        Button temp = findViewById(R.id.tempBut);

        ecg.setEnabled(false);
        temp.setEnabled(false);

        Button connect = findViewById(R.id.connectBut);
        Button camStream = findViewById(R.id.CamButton);

        TextView dispHr = findViewById(R.id.hr);
        EditText dispTemp = findViewById(R.id.temp);
        EditText dispAmbtemp = findViewById(R.id.ambtemp);
        TextView getName = findViewById(R.id.divName);

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothAdapter.startDiscovery();

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        final ExternalThread externalThread = new ExternalThread();

        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BluetoothDevice myDevice = null;
                name = getName.getText().toString();

                Toast.makeText(getApplicationContext(), "Connecting to " +name, Toast.LENGTH_SHORT).show();

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
                        externalThread.run();
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
                    String ecg = recieveECG();
                    int hr = heartRate();
                    dispHr.setText(ecg +" " +String.valueOf(hr));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        temp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    sendData((byte)3);

                    String temps;

                    for(int i = 0; i < 9; i ++) //simple delay for 9 seconds
                    {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    String temp = recieveData(0);

                    String[] parts = temp.split(",");
                    temp = parts[0];
                    temps = parts[1];

                    dispTemp.setText(temp);
                    dispAmbtemp.setText(temps);
                    Toast.makeText(getApplicationContext(), "done", Toast.LENGTH_SHORT).show();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }
}

class ExternalThread extends Thread{

    Boolean kill = false;


    MainActivity mainActivity = new MainActivity();
    OutputStream outputStream = mainActivity.outputStream ;
    InputStream inputStream = mainActivity.inputStream;


    String receiveData = null;
    @Override
    public void run(){

        while (true)
        {
            receiveData = mainActivity.recieveData(0);
            if(receiveData != null){
                break;
            }
            else{
                //Add Functionality
            }

            if(kill){
                break;
            }

            try {
                sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}

