package com.deew.bs808;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.deew.jt808.ClientConstants;
import com.deew.jt808.ClientStateCallback;
import com.deew.jt808.JT808Client;
import com.deew.jt808.msg.AuthenticateRequest;
import com.deew.jt808.msg.LocationMessage;
import com.deew.jt808.msg.RegisterReply;
import com.deew.jt808.msg.RegisterRequest;
import com.deew.jt808.msg.ServerGenericReply;
import com.deew.jt808.util.LogUtils;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements ClientStateCallback, View.OnClickListener{

    private static final String TAG = LogUtils.makeTag(MainActivity.class);

    private SharedPreferences mPrefs;
    private String            mHost;
    private int               mPort;
    private String            mAuthCode;

    private JT808Client mJT808Client;
    private RegisterRequest.Builder mRegisterReqBuilder;
    private AuthenticateRequest.Builder mAuthReqBuilder;
    private LocationMessage.Builder mLocationMessageBuilder;

    private Timer mTimer;

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


        mPrefs = getSharedPreferences(ClientConstants.PREF_FILE_NAME, MODE_PRIVATE);
        mHost = mPrefs.getString(ClientConstants.PREF_KEY_HOST, ClientConstants.PREF_DEFAULT_HOST);
        mPort = mPrefs.getInt(ClientConstants.PREF_KEY_PORT, ClientConstants.PREF_DEFAULT_PORT);
        mAuthCode = mPrefs.getString(ClientConstants.PREF_KEY_AUTH_CODE, null);


        mRegisterReqBuilder = new RegisterRequest.Builder();
        mLocationMessageBuilder  = new LocationMessage.Builder();

        mJT808Client = new JT808Client();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mJT808Client.close();
        mJT808Client = null;
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_regsiter:
                if(mJT808Client != null && mJT808Client.isConnected()){
                    registerClient();
                }
                break;
            case R.id.btn_auth:
                if(mJT808Client != null && mJT808Client.isConnected()){
                    authenticate(mAuthCode);
                }
                break;
            case R.id.btn_location:
                if(mJT808Client != null && mJT808Client.isConnected()){
                    sendLocation();
                }
                break;
            case R.id.btn_close:
                if (mJT808Client != null && mJT808Client.isConnected()){
                    mJT808Client.close();
                }
                break;
            case R.id.btn_connect:
                if (mJT808Client != null){
                    connectToServer(mHost, mPort);
                }
                break;
        }
    }

    private void connectToServer(String host, int port){
        mJT808Client.connect(host, port, this);
    }

    private void registerClient(){
        RegisterRequest request = new RegisterRequest.Builder().build();
        mJT808Client.registerClient(request);
    }

    private void authenticate(String authCode){
        if(authCode == null){
            Toast.makeText(this, "鉴权码为空", Toast.LENGTH_SHORT).show();
            return;
        }
        mJT808Client.authenticate(mAuthCode);
    }

    @Override
    public void connectSuccess() {
        Toast.makeText(this, "连接成功", Toast.LENGTH_SHORT).show();
        if(mAuthCode == null){
            registerClient();
        }else{
            authenticate(mAuthCode);
        }
    }

    @Override
    public void connectFail() {
        Toast.makeText(this, "连接失败", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void connectionClosed() {
        Toast.makeText(this, "已关闭连接", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void registerComplete(RegisterReply reply) {
        switch (reply.getResult()) {
            case RegisterReply.RESULT_OK:
                mAuthCode = reply.getAuthCode();
                Log.d(TAG, "authCode=" + mAuthCode);
                mPrefs.edit().putString(ClientConstants.PREF_KEY_AUTH_CODE, mAuthCode).commit();
                //begin auth
                authenticate(mAuthCode);

                Log.i(TAG, "Client registered SUCCESS !!!");
                break;
            case RegisterReply.RESULT_VEH_NOT_FOUND:
                Log.w(TAG, "Registration failed - vehicle not found");
                break;
            case RegisterReply.RESULT_VEH_REGISTERED:
                Log.w(TAG, "Registration failed - vehicle registered");
                break;
            case RegisterReply.RESULT_CLT_NOT_FOUND:
                Log.w(TAG, "Registration failed - client not found");
                break;
            case RegisterReply.RESULT_CLT_REGISTERED:
                Log.w(TAG, "Registration failed - client registered");
                break;
            default:
                Log.e(TAG, "Unknown registration result");
        }
    }

    @Override
    public void authComplete(ServerGenericReply reply) {
        switch (reply.getResult()) {
            case ServerGenericReply.RESULT_OK:
                Log.d(TAG, "Auth SUCCESS!!!");
                //Now you can send other message
                startSendLocation();
                break;
            case ServerGenericReply.RESULT_FAIL:
            case ServerGenericReply.RESULT_UNSUPPORTED:
            case ServerGenericReply.RESULT_BAD_REQUEST:
            case ServerGenericReply.RESULT_CONFIRM:
            default:
                Log.e(TAG, "Auth FAIL!!!");
                break;
        }
    }


    private void sendLocation(){
        if(mJT808Client != null){
            try {
                mJT808Client.sendMessage(new LocationMessage.Builder()
                        .setLatitude(22.5409180000)
                        .setLongitude(114.0560200000)
                        .build());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private void startSendLocation(){
        if(mTimer == null) {
            mTimer = new Timer();
        }
        mTimer.schedule(mUploadTimerTask, 5000, 5000);
    }

    private void stopSendLocation(){
        if(mTimer == null)
            mTimer.cancel();
    }

    private TimerTask mUploadTimerTask = new TimerTask() {
        @Override
        public void run() {
            sendLocation();
        }
    };
}
