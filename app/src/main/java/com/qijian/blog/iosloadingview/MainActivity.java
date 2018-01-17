package com.qijian.blog.iosloadingview;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final IOSLoadingView iosLoadingView = (IOSLoadingView)findViewById(R.id.iosloadingview);
        iosLoadingView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("ios Load ", "onclick");
            }
        });

        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                iosLoadingView.setState(IOSLoadingView.STATE_LOADING);
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    float i = 0;
                    @Override
                    public void run() {
                        i +=0.01;
                        iosLoadingView.setProgress(i, true);
                        if (i <= 1){
                            handler.postDelayed(this, 100);
                        }
                    }
                }, 100);
            }
        });


    }
}
