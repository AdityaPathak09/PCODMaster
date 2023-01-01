package com.example.pcodmaster;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class stream extends AppCompatActivity {

    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stream);

        webView = findViewById(R.id.camView);


        webView.setWebViewClient(new WebViewClient());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setInitialScale(1);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webView.setScrollbarFadingEnabled(false);
//        System.out.println("Wifi State: " +wifiManager.getWifiState());
//        String url = "https://Instagram.com";
//        String url = "http://192.168.194.147/mjpeg/1";
//        String url = "http://192.168.207.203/mjpeg/1";
//        String url = "http://192.168.164.203/mjpeg/1";
//        String url = "http://192.168.178.203/mjpeg/1";
        String url = getIntent().getStringExtra("link");
        webView.loadUrl(url); //put url
    }

    protected void onPause(){
        super.onPause();
//        webView.clearCache();
        webView.destroy();

    }
}