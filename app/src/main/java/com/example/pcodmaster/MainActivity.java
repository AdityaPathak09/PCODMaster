package com.example.pcodmaster;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    public OutputStream outputStream = null;
    public InputStream inputStream = null;
    String name = "";
    String addressToConnect = "";
    BluetoothSocket bluetoothSocket = null;
    EditText sendBox;
    TextView recieveBox;

    ExternalThread externalThread;

    String inputData = "";

    public MainActivity() {
    }

    static Long findTwoscomplement(StringBuffer str)
    {
        int n = str.length();

        int i;
        for (i = n-1 ; i >= 0 ; i--)
            if (str.charAt(i) == '1')
                break;

        // If there exists no '1' concat 1 at the
        // starting of string
        if (i == -1)
            return Long.parseLong("1" + str);

        // Continue traversal after the position of
        // first '1'
        for (int k = i-1 ; k >= 0; k--)
        {
            //Just flip the values
            if (str.charAt(k) == '1')
                str.replace(k, k+1, "0");
            else
                str.replace(k, k+1, "1");
        }
//        String string = String.valueOf(str);
        return Long.parseLong(String.valueOf(str));
    }

    public static Long shifterECG(int msb_sample, int mid_sample, int lsb_sample) {
        Long temp = Long.valueOf(0);
//        msb_sample = 0x00000003 & msb_sample;
        msb_sample = msb_sample << 16;
        temp = temp | msb_sample;
        mid_sample = mid_sample << 8;
        temp = temp | mid_sample;
        temp = temp | lsb_sample;
        temp = temp & 0x3FFFF;
        StringBuffer sb = new StringBuffer();
        sb.append(Long.toBinaryString(temp));
        Log.e("Value", sb.toString());
        temp = findTwoscomplement(sb);
//        temp -= 1400;
//        temp = ~temp;
        return temp;
    }

    public static int shifterPPG(int msb_sample, int mid_sample, int lsb_sample) {
        int temp = 0;
//        msb_sample = 0x00000003 & msb_sample;
        msb_sample = msb_sample << 16;
        temp = temp | msb_sample;
        mid_sample = mid_sample << 8;
        temp = temp | mid_sample;
        temp = temp | lsb_sample;
        temp = temp & 0xFFFFF;
//        temp = ~temp;
//        temp = temp + 0x01;
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
            Log.e("Packet Error", "Packet Not Recieved: " + packetCode);
            e.printStackTrace();
        }

        return data;
    }

    public ArrayList<Integer> recieveECG() throws IOException {
        externalThread.kill = false;
        ArrayList<Integer> ret;
        outputStream.write((byte) 1);

//        inputStream.skip(inputStream.available());
//        int packetCounter = inputStream.read();// = 3
//        inputStream.skip(inputStream.available());
//        inputStream.read();
//        int packetSize = inputStream.read(); //  = 18

//

//        Toast.makeText(getApplicationContext(), "PacketSize: " + packetSize, Toast.LENGTH_SHORT).show();

        ret = externalThread.runECG(inputStream, outputStream);
//        outputStream.write((byte) 0);
        Toast.makeText(getApplicationContext(), "Size: " + ret.size(), Toast.LENGTH_SHORT).show();
//        outputStream.write((byte) 0);
        return ret;
    }

    public ArrayList<Integer> recievePPG() throws IOException {
        externalThread.kill = false;
        ArrayList<Integer> ret;
        outputStream.write((byte) 2);
        ret = externalThread.runPPG(inputStream, outputStream);
        outputStream.write((byte) 0);
        Toast.makeText(getApplicationContext(), "Size: " + ret.size(), Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(this, "Connected to " + myDevice.getName(), (int) 100).show();

                }
            } catch (IOException e) {
                Toast.makeText(this, "Cannot connect to " + myDevice.getName() + ". Please retry.", (int) 100).show();
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
        try {
            outputStream.write((byte) 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onPause();
        externalThread.kill = true;
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onStop() {
        super.onStop();
        try {
            outputStream.write((byte) 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        externalThread.kill = true;
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onDestroy() {
        try {
            outputStream.write((byte) 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onDestroy();
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
        Button ppg = findViewById(R.id.ppgBut);

        ecg.setEnabled(false);
        temp.setEnabled(false);
        ppg.setEnabled(false);

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
//                ArrayList<Integer> ecg = new ArrayList<Integer>();
//                for(int i = 0; i < 100; i ++){
//                    ecg.add((int) (100 * Math.sin((double) i)));
//                }
//                Toast.makeText(getApplicationContext(), ecg.toString(), Toast.LENGTH_SHORT).show();
//
//                Intent intent = new Intent(MainActivity.this, ECGChart.class);
//                intent.putIntegerArrayListExtra("list", ecg);
//                startActivity(intent);

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                byte data = Byte.parseByte(sendBox.getText().toString());
                try {
                    sendData(data);
                } catch (IOException e) {
                    e.printStackTrace();
                }

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
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
            @SuppressLint("WrongConstant")
            @Override
            public void onClick(View view) {
                BluetoothDevice myDevice = null;
                name = getName.getText().toString();

//                Toast.makeText(getApplicationContext(), "Connecting to " + name, Toast.LENGTH_SHORT).show();

//                textView.setText("");

                if (pairedDevices.size() > 0) {
                    // Loop through paired device
                    for (BluetoothDevice device : pairedDevices) {
//                        Toast.makeText(getApplicationContext(), device.getName().toString(), Toast.LENGTH_SHORT).show();

                        if (device.getName().equals(name)) {
                            addressToConnect = bluetoothAdapter.getRemoteDevice(device.getAddress()).toString();
//                            Toast.makeText(getApplicationContext(),device.getName().toString() + ": "+ addressToConnect, Toast.LENGTH_LONG).show();
                            myDevice = bluetoothAdapter.getRemoteDevice(addressToConnect);

//                            Toast.makeText(getApplicationContext(), myDevice.toString(), Toast.LENGTH_SHORT).show();
//                            System.out.println(myDevice.getName().toString());
//                            Toast.makeText(getApplicationContext(), bluetoothAdapter.getRemoteDevice(device.getAddress()).toString(), Toast.LENGTH_LONG).show();
                            break;
                        }
                    }

                    if (connect(myDevice)) {
                        ecg.setEnabled(true);
                        temp.setEnabled(true);
                        send.setEnabled(true);
                        connect.setEnabled(false);
                        ppg.setEnabled(true);
                        try {
                            outputStream.write((byte) 0);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
//                        externalThread.run();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "no devices connected", (int) 100).show();
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
//                    inputStream.skip(inputStream.available());
                    ArrayList<Integer> ecg = recieveECG();
//                    recieveBox.setText(ecg.toString());
                    Intent intent = new Intent(MainActivity.this, Chart.class);
                    intent.putIntegerArrayListExtra("list", ecg);
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

        ppg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
//                    inputStream.skip(inputStream.available());
                    ArrayList<Integer> ecg = recievePPG();
//                    recieveBox.setText(ecg.toString());
                    Intent intent = new Intent(MainActivity.this, Chart.class);
                    intent.putIntegerArrayListExtra("list", ecg);
                    startActivity(intent);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }
}

class ExternalThread extends Thread {

    Boolean kill = false;

    MainActivity mainActivity = new MainActivity();

    String receiveData = null;

    public ArrayList<Integer> runECG(InputStream inputStream, OutputStream outputStream) throws IOException {

        ArrayList<Integer> list = new ArrayList<Integer>();
        inputStream.read();
//
//        ArrayList<Integer> data = new ArrayList<Integer>();
        List<Integer> data = Arrays.asList(0xB0, 0x01, 0x2D, 0xB0, 0x01, 0x32, 0xB0, 0x01, 0x1B, 0xB0, 0x00, 0xFA, 0xB0, 0x01, 0x1C, 0xB0, 0x01, 0x2B, 0xB0, 0x01, 0x17, 0xB0, 0x01, 0x1A, 0xB0, 0x01, 0x35, 0xB0, 0x01, 0x30, 0xB0, 0x01, 0x2E, 0xB0, 0x01, 0x20, 0xB0, 0x01, 0x67, 0xB0, 0x01, 0x80, 0xB0, 0x01, 0x3E, 0xB0, 0x01, 0x26, 0xB0, 0x01, 0x30, 0xB0, 0x01, 0x3E, 0xB0, 0x01, 0x35, 0xB0, 0x01, 0x2F, 0xB0, 0x01, 0x2A, 0xB0, 0x01, 0x23, 0xB0, 0x01, 0x00, 0xB0, 0x01, 0x14, 0xB0, 0x01, 0x41, 0xB0, 0x01, 0x36, 0xB0, 0x01, 0x1A, 0xB0, 0x00, 0xF4, 0xB0, 0x01, 0x1A, 0xB0, 0x01, 0x2C, 0xB0, 0x01, 0x37, 0xB0, 0x01, 0x37, 0xB0, 0x01, 0x46, 0xB0, 0x01, 0x48, 0xB0, 0x01, 0x45, 0xB0, 0x01, 0x37, 0xB0, 0x01, 0x27, 0xB0, 0x01, 0x28, 0xB0, 0x01, 0x26, 0xB0, 0x01, 0x47, 0xB0, 0x01, 0x2E, 0xB0, 0x01, 0x31, 0xB0, 0x01, 0x5B, 0xB0, 0x01, 0x3C, 0xB0, 0x00, 0xF0, 0xB0, 0x01, 0x1C, 0xB0, 0x01, 0x2E, 0xB0, 0x01, 0x37, 0xB0, 0x01, 0x24, 0xB0, 0x01, 0x21, 0xB0, 0x01, 0x32, 0xB0, 0x01, 0x28, 0xB0, 0x01, 0x2D, 0xB0, 0x00, 0xF8, 0xB0, 0x00, 0xEB, 0xB0, 0x01, 0x1D, 0xB0, 0x01, 0x27, 0xB0, 0x01, 0x23, 0xB0, 0x01, 0x45, 0xB0, 0x01, 0x81, 0xB0, 0x01, 0x3E, 0xB0, 0x01, 0x31, 0xB0, 0x01, 0x21, 0xB0, 0x01, 0x1F, 0xB0, 0x01, 0x11, 0xB0, 0x01, 0x31, 0xB0, 0x01, 0x35, 0xB0, 0x01, 0x21, 0xB0, 0x01, 0x1E, 0xB0, 0x01, 0x13, 0xB0, 0x01, 0x12, 0xB0, 0x01, 0x25, 0xB0, 0x01, 0x29, 0xB0, 0x01, 0x44, 0xB0, 0x01, 0x29, 0xB0, 0x01, 0x16, 0xB0, 0x01, 0x1E, 0xB0, 0x01, 0x21, 0xB0, 0x01, 0x30, 0xB0, 0x01, 0x30, 0xB0, 0x01, 0x1B, 0xB0, 0x01, 0x23, 0xB0, 0x01, 0x2D, 0xB0, 0x01, 0x38, 0xB0, 0x01, 0x43, 0xB0, 0x01, 0x5A, 0xB0, 0x01, 0x31, 0xB0, 0x01, 0x1B, 0xB0, 0x01, 0x23, 0xB0, 0x01, 0x3F, 0xB0, 0x01, 0x2F, 0xB0, 0x01, 0x27, 0xB0, 0x01, 0x1E, 0xB0, 0x01, 0x2A, 0xB0, 0x01, 0x1E, 0xB0, 0x01, 0x01, 0xB0, 0x01, 0x2F, 0xB0, 0x01, 0x32, 0xB0, 0x01, 0x2E, 0xB0, 0x01, 0x15, 0xB0, 0x01, 0x5A, 0xB0, 0x01, 0x39, 0xB0, 0x01, 0x30, 0xB0, 0x01, 0x2C, 0xB0, 0x01, 0x44, 0xB0, 0x01, 0x1C, 0xB0, 0x01, 0x15, 0xB0, 0x01, 0x1C, 0xB0, 0x01, 0x2A, 0xB0, 0x01, 0x50, 0xB0, 0x01, 0x1B, 0xB0, 0x01, 0x17, 0xB0, 0x01, 0x19, 0xB0, 0x01, 0x1E, 0xB0, 0x01, 0x32, 0xB0, 0x01, 0x0B, 0xB0, 0x01, 0x10, 0xB0, 0x01, 0x1A, 0xB0, 0x01, 0x1B, 0xB0, 0x01, 0x25, 0xB0, 0x01, 0x29, 0xB0, 0x01, 0x38, 0xB0, 0x01, 0x34, 0xB0, 0x01, 0x28, 0xB0, 0x01, 0x26, 0xB0, 0x01, 0x1C, 0xB0, 0x01, 0x1C, 0xB0, 0x01, 0x29, 0xB0, 0x01, 0x20, 0xB0, 0x01, 0x1D, 0xB0, 0x01, 0x41, 0xB0, 0x01, 0x36, 0xB0, 0x01, 0x1D, 0xB0, 0x01, 0x31, 0xB0, 0x01, 0x2D, 0xB0, 0x01, 0x13, 0xB0, 0x01, 0x05, 0xB0, 0x01, 0x0E, 0xB0, 0x01, 0x14, 0xB0, 0x01, 0x32, 0xB0, 0x01, 0x36, 0xB0, 0x01, 0x38, 0xB0, 0x01, 0x16, 0xB0, 0x01, 0x1C, 0xB0, 0x01, 0x23, 0xB0, 0x01, 0x2D, 0xB0, 0x00, 0xFC, 0xB0, 0x01, 0x00, 0xB0, 0x01, 0x09, 0xB0, 0x01, 0x22, 0xB0, 0x01, 0x3F, 0xB0, 0x01, 0x4C, 0xB0, 0x01, 0x3A, 0xB0, 0x01, 0x1E, 0xB0, 0x01, 0x1D, 0xB0, 0x01, 0x21, 0xB0, 0x01, 0x2F, 0xB0, 0x01, 0x42, 0xB0, 0x01, 0x28, 0xB0, 0x01, 0x19, 0xB0, 0x01, 0x10, 0xB0, 0x01, 0x39, 0xB0, 0x01, 0x0E, 0xB0, 0x01, 0x1F, 0xB0, 0x01, 0x0F, 0xB0, 0x01, 0x11, 0xB0, 0x01, 0x26, 0xB0, 0x01, 0x2F, 0xB0, 0x01, 0x21, 0xB0, 0x01, 0x10, 0xB0, 0x01, 0x26, 0xB0, 0x01, 0x15, 0xB0, 0x01, 0x07, 0xB0, 0x01, 0x33, 0xB0, 0x01, 0x35, 0xB0, 0x01, 0x21);
        Log.e("data", data.toString());
        while (!kill) {

//                int data[] = new int[packetSize];

//                for(int i = 0; i < packetSize; i ++){
//                    data[i] = (int) inputStream.read();
//                }

            data.add(inputStream.read());

//            Log.e("values", String.valueOf(data.size()));
            if (data.size() >= 3000){
                kill = true;
                outputStream.write((byte) 0);
            }


//                int k = 0;
//                for(int i = 0; i < packetSize; i+=3){
//                    list.add(mainActivity.shifter(data[i], data[i+1], data[i+2]));
//                }

//                while(k <= packetSize - 3){
//                    if(data[k] == 0xB0) {
//                        list.add(mainActivity.shifter(data[k], data[k + 1], data[k + 2]));
//                        k += 3;
//                    }
//                    else{
//                        k++;
//                    }
//                }
//                Log.e("size", String.valueOf(data.length));
//                Log.e("Value", Arrays.toString(data));
//                Log.e("values", String.valueOf(list.size()));
//
//
//                if(list.size() >= 6000)
//                        kill = true;
        }

//        Log.e("data", data.toString());
        int k = 0;
//        list.add(-5000);
        while (k <= data.size() - 4) {
            if ((data.get(k) >= 0xB0 && data.get(k) <= 0xB3) && (data.get(k+3) >= 0xB0 && data.get(k+3) <= 0xB3)) {
                list.add(Math.toIntExact(MainActivity.shifterECG(data.get(k), data.get(k + 1), data.get(k + 2))));
                k += 3;
            } else {
                k++;
            }
        }
//        Log.e("data", list.toString());
//        data.clear();
        return list;
    }

    public ArrayList<Integer> runPPG(InputStream inputStream, OutputStream outputStream) throws IOException {

        ArrayList<Integer> list = new ArrayList<Integer>();
        inputStream.read();

        ArrayList<Integer> data = new ArrayList<Integer>();

        int period = 10;


        while (!kill) {

            data.add(inputStream.read());

            if (data.size() >= 5000){
                kill = true;
                outputStream.write((byte) 0);
            }

        }
        Log.e("Data", data.toString());

        int k = 0;
        while (k <= data.size() - 4) {
            if (data.get(k) >= 0x0000 && data.get(k) <= 0x0011 && data.get(k+3) >= 0x0000 && data.get(k+3) <= 0x0011) {
                int d = MainActivity.shifterPPG(data.get(k), data.get(k + 1), data.get(k + 2));
                list.add(d);
                k += 3;
            } else {
                k++;
            }
        }

        data.clear();

        for(int i = 0; i < list.size() - period; i++){
            double d = 0;
            for(int j = i; j < i + period; j ++){
                d += list.get(j);
            }
            data.add((int)(d/period));
            d = 0;
//            data.add((list.get(i)+list.get(i+1)+list.get(i+2)+list.get(i+3)+list.get(i+4)+list.get(i+5)+list.get(i+6))/7);
        }

        list.clear();
        return data;
    }
}

