package me.hekr.sthome.configuration.activity;

import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;


import java.util.Timer;
import java.util.TimerTask;

import me.hekr.sthome.R;
import me.hekr.sthome.common.TopbarSuperActivity;

public class GuideToSetEspWifiActivity extends TopbarSuperActivity implements View.OnClickListener {
    private final String TAG = GuideToSetEspWifiActivity.class.getName();
    private EspWifiAdminSimple mWifiAdmin;
    private String mApSSId;
    private String mApPwd;
    private Button btn_set_wifi;

    @Override
    protected void onCreateInit() {
        btn_set_wifi = (Button)findViewById(R.id.ap_config);
        btn_set_wifi.setOnClickListener(this);
        getTopBarView().setTopBarStatus(1,1,getResources().getString(R.string.setting),null,new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        },null);
        mApSSId = getIntent().getStringExtra("ssid");
        mApPwd = getIntent().getStringExtra("psw");
        mWifiAdmin = new EspWifiAdminSimple(this);
        mWifiAdmin.startScan();
//        MyTask myTask = new MyTask();
//        mTimer = new Timer();
//        mTimer.schedule(myTask,0,500);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_guide_set_wifi;
    }

//    private int mCount = 0;
//    private Timer mTimer;
//    private Handler mHandler = new Handler();

    @Override
    public void onClick(View view) {
        Intent i = new Intent();
        if(android.os.Build.VERSION.SDK_INT >= 11){
            //Honeycomb
            i.setClassName("com.android.settings", "com.android.settings.Settings$WifiSettingsActivity");
        }else{
            //other versions
            i.setClassName("com.android.settings"
                    , "com.android.settings.wifi.WifiSettings");
        }
        startActivity(i);
    }

//    private class MyTask extends TimerTask {
//        @Override
//        public synchronized void run() {
//            String wifi = mWifiAdmin.getWifiConnectedSsid();
//            Log.i(TAG,"当前wifi:"+wifi);
//            if((!TextUtils.isEmpty(wifi))&&wifi.contains("ESP_")){
//                mCount++;
//                if(mCount==2){
//                    mCount = 0;
//                    mTimer.cancel();
//                    mTimer = null;
//                    gotoNext();
//                }
//            }
//        }
//    }

//    private void gotoNext(){
//        mHandler.post(new Runnable() {
//            @Override
//            public void run() {
//                Intent intent = new Intent(GuideToSetEspWifiActivity.this, EsptouchAnimationActivity.class);
//                intent.putExtra("isApConnect", true);
//                intent.putExtra("ssid", mApSSId);
//                intent.putExtra("psw", mApPwd);
//                startActivity(intent);
//                finish();
//            }
//        });
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        if (mTimer != null) {
//            mTimer.cancel();
//            mTimer = null;
//        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        String wifi = mWifiAdmin.getWifiConnectedSsid();
        Log.i(TAG,"当前wifi:"+wifi);
        if((!TextUtils.isEmpty(wifi))&&wifi.contains("ESP_")){
                Intent intent = new Intent(GuideToSetEspWifiActivity.this, EsptouchAnimationActivity.class);
                intent.putExtra("isApConnect", true);
                intent.putExtra("ssid", mApSSId);
                intent.putExtra("psw", mApPwd);
                startActivity(intent);
                finish();
            }
    }
}
