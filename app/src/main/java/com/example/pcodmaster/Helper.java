package com.example.pcodmaster;

import java.io.InputStream;
import java.io.OutputStream;

public class Helper {
    private static Helper helper;
    private static InputStream inputStream;
    public static OutputStream outputStream;

    private Helper(){

    }

    public static Helper getInstance(){
        if(helper != null)
            return helper;
        return new Helper();
    }

    public void setInputStreamer(InputStream is){
        inputStream = is;
    }
    public void setOutputStreamer(OutputStream os){
        outputStream = os;
    }

    public InputStream getInputStream(){
        return inputStream;
    }
    public OutputStream getOutputStream(){
        return outputStream;
    }
}
