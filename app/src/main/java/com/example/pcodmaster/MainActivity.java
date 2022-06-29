package com.example.pcodmaster;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    String name = "";
    String addressToConnect = "";
    BluetoothSocket bluetoothSocket = null;
    OutputStream outputStream = null;
    InputStream inputStream = null;

    public String recieveData()
    {
        String data = "";

        try {

            inputStream.skip(inputStream.available());
            byte dataof = (byte) inputStream.read();

            inputStream.skip(inputStream.available());
            int size = inputStream.read();

            for(int i = 0; i < size; i ++)
            {
                data = data + (char) inputStream.read();
            }
            Toast.makeText(getApplicationContext(), data, Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return data;

    }


    public void sendData(byte instruction) throws IOException {
//        System.out.println("Sending data");
        outputStream.write(instruction);
//        System.out.println("data sent");
    }


    @SuppressLint("MissingPermission")
    public void connect(BluetoothDevice myDevice)
    {

        final UUID mUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
        int counter = 0;
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

        TextView dispHr = findViewById(R.id.hr);
        TextView dispTemp = findViewById(R.id.temp);
        TextView dispAmbtemp = findViewById(R.id.ambtemp);
        TextView getName = findViewById(R.id.divName);

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothAdapter.startDiscovery();

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

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

                    connect(myDevice);
                    ecg.setEnabled(true);
                    temp.setEnabled(true);

                }
                else
                {
                    Toast.makeText(getApplicationContext(), "no devices connected", Toast.LENGTH_SHORT).show();
                }
            }
        });

        ecg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    sendData((byte)1);
                    recieveData();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        temp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    sendData((byte)2);

                    String temps;
                    for(int i = 0; i < 9; i ++)
                    {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    String temp = recieveData();

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