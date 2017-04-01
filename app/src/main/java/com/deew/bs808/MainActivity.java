package com.deew.bs808;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.deew.jt808.ServiceManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Start the message service
        new ServiceManager(this).startService();
    }
}
