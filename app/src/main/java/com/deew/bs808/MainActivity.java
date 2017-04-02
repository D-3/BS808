package com.deew.bs808;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.deew.jt808.ClientConstants;
import com.deew.jt808.JT808Client;
import com.deew.jt808.conn.ConnectionStateCallback;
import com.deew.jt808.msg.AuthenticateRequest;
import com.deew.jt808.msg.LocationMessage;
import com.deew.jt808.msg.RegisterRequest;
import com.deew.jt808.util.TimeUtils;

public class MainActivity extends AppCompatActivity implements ConnectionStateCallback, View.OnClickListener{
    private static final String TAG = MainActivity.class.getSimpleName();

    private JT808Client mJT808Client;
    private RegisterRequest.Builder mRegisterReqBuilder;
    private AuthenticateRequest.Builder mAuthReqBuilder;
    private LocationMessage.Builder mLocationMessageBuilder;

    private Button mBtnRegister;
    private Button mBtnAuth;
    private Button mBtnLocation;
    private Button mBtnClose;
    private Button mBtnConnect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBtnRegister = (Button) findViewById(R.id.btn_regsiter);
        mBtnRegister.setOnClickListener(this);
        mBtnAuth = (Button) findViewById(R.id.btn_auth);
        mBtnAuth.setOnClickListener(this);
        mBtnLocation = (Button) findViewById(R.id.btn_location);
        mBtnLocation.setOnClickListener(this);
        mBtnClose = (Button) findViewById(R.id.btn_close);
        mBtnClose.setOnClickListener(this);
        mBtnConnect = (Button) findViewById(R.id.btn_connect);
        mBtnConnect.setOnClickListener(this);


        mRegisterReqBuilder = new RegisterRequest.Builder();
        mLocationMessageBuilder  = new LocationMessage.Builder();

        mJT808Client = new JT808Client();
        mJT808Client.connect(ClientConstants.PREF_DEFAULT_HOST, ClientConstants.PREF_DEFAULT_PORT, this);
    }

    @Override
    public void onSuccess() {
        mJT808Client.sendMessage(
                mLocationMessageBuilder.setLatitude(22.5409180000)
                        .setLongitude(114.0560200000)
                        .setTimestamp(TimeUtils.getCurrentTimeJT808form())
                        .build());
    }

    @Override
    public void onFail() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mJT808Client.setConnectionStateCallback(null);
        mJT808Client.close();
        mJT808Client = null;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_regsiter:
                if(mJT808Client != null && mJT808Client.isConnected()){
                    mJT808Client.sendMessage(mRegisterReqBuilder.build());
                }
                break;
            case R.id.btn_auth:
                if(mJT808Client != null && mJT808Client.isConnected()){
                    //TODO
                }
                break;
            case R.id.btn_location:
                if(mJT808Client != null && mJT808Client.isConnected()){
                    mJT808Client.sendMessage(mLocationMessageBuilder.setLatitude(22.5409180000)
                            .setLongitude(114.0560200000)
                            .setTimestamp(TimeUtils.getCurrentTimeJT808form())
                            .build());
                }
                break;
            case R.id.btn_close:
                if (mJT808Client != null && mJT808Client.isConnected()){
                    mJT808Client.close();
                }
                break;
            case R.id.btn_connect:
                if (mJT808Client != null && mJT808Client.isClosed()){
                    mJT808Client.connect(ClientConstants.PREF_DEFAULT_HOST, ClientConstants.PREF_DEFAULT_PORT, this);
                }
                break;

        }
    }
}
