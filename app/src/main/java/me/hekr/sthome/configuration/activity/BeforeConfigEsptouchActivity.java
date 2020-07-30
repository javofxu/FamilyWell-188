package me.hekr.sthome.configuration.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.net.wifi.ScanResult;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import me.hekr.sthome.R;
import me.hekr.sthome.common.TopbarSuperActivity;
import me.hekr.sthome.commonBaseView.ECAlertDialog;
import me.hekr.sthome.commonBaseView.loadingView.ZLoadingView;
import me.hekr.sthome.commonBaseView.loadingView.Z_TYPE;
import me.hekr.sthome.tools.PermissionUtils;
import me.hekr.sthome.tools.UnitTools;

/**
 * Created by gc-0001 on 2017/2/10.
 */
public class BeforeConfigEsptouchActivity extends TopbarSuperActivity implements View.OnClickListener{
    private AnimationDrawable ad;
    private static final int REQUEST_LOCATION=1001;
    private static final int REQUEST_LOCATION_SERVICE=1002;
    private String[] permission = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
    private EspWifiAdminSimple mWifiAdmin;
    private boolean isAPConnect = false;
    private String mApSSId;
    private String mApPwd;
    private ZLoadingView zLoadingView;

    @Override
    protected void onCreateInit() {
        isAPConnect = getIntent().getBooleanExtra("isAp", false);
        initView();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_before_config_esptouch;
    }

    private void initView(){
        Button next_btn = findViewById(R.id.next);
        ImageView image = findViewById(R.id.imageView1);
        image.setBackgroundResource(R.drawable.config_tishi);
        zLoadingView = findViewById(R.id.loadingView);
        ad = (AnimationDrawable) image.getBackground();
        next_btn.setOnClickListener(this);
        getTopBarView().setTopBarStatus(1, 1, getResources().getString(R.string.net_configuration), null, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 finish();
            }
        },null);
        image.post(new Runnable() {
            @Override
            public void run() {
                ad.start();
            }
        });
        if (isAPConnect){
            mApSSId = getIntent().getStringExtra("SSID");
            mApPwd = getIntent().getStringExtra("PWD");
            TextView title = findViewById(R.id.ap_title);
            TextView message = findViewById(R.id.ap_message);
            title.setText(getString(R.string.ap_config_title));
            message.setText(getString(R.string.ap_title_msg));
        }
        mWifiAdmin = new EspWifiAdminSimple(this);
        mWifiAdmin.startScan();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.next) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (PermissionUtils.requestPermission(this, permission, REQUEST_LOCATION)) {
                    if (UnitTools.isLocServiceEnable(this)) {
                        gotoNext();
                    } else {
                        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivityForResult(intent, REQUEST_LOCATION_SERVICE);
                            }
                        };
                        ECAlertDialog ecAlertDialog = ECAlertDialog.buildAlert(this, getString(R.string.permission_reject_location_service_tip), listener);
                        ecAlertDialog.setTitle(getString(R.string.permission_register));
                        ecAlertDialog.setButton(ECAlertDialog.BUTTON_POSITIVE, getString(R.string.goto_set), listener);
                        ecAlertDialog.show();
                    }
                }
            } else {
                gotoNext();
            }
        }
    }

    private void gotoNext(){
        if (isAPConnect){
            mTimer = new Timer();
            TimerTask mTask = new MyTask();
            zLoadingView.setLoadingBuilder(Z_TYPE.SINGLE_CIRCLE);
            zLoadingView.setVisibility(View.VISIBLE);
            mTimer.schedule(mTask, 0, 1000);
        }else {
            startActivity(new Intent(this, EsptouchDemoActivity.class));
            finish();
        }
    }

    private int mCount = 0;
    private Timer mTimer;
    private Handler mHandler = new Handler();

    private class MyTask extends TimerTask{
        @Override
        public synchronized void run() {
            ScanResult wifi = mWifiAdmin.findWifiList();
            if (wifi != null) {
                mTimer.cancel();
                mTimer = null;
                mCount = 0;
                configApWifi(wifi);
            }else {
                mCount++;
                mWifiAdmin.startScan();
                if (mCount>=5){
                    mTimer.cancel();
                    mTimer = null;
                    mCount = 0;
                    showFinal(R.string.not_find_wifi);
                }
            }
        }
    }

    private void configApWifi(ScanResult wifi){
        String SSId = wifi.SSID;
        boolean isConnect = mWifiAdmin.addNetwork(SSId, null, 0);
        if (isConnect) {
            Intent intent = new Intent(this, EsptouchAnimationActivity.class);
            intent.putExtra("isApConnect", true);
            intent.putExtra("ssid", mApSSId);
            intent.putExtra("psw", mApPwd);
            startActivity(intent);
            finish();
        }else {
            showFinal(R.string.wait_connect_wifi);
        }
    }

    private void showFinal(final int str){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                zLoadingView.setVisibility(View.GONE);
                showToast(getString(str));
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION) {
            if (permissions.length == grantResults.length) {
                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        if(UnitTools.isLocServiceEnable(this)){
                            Intent tent = new Intent(BeforeConfigEsptouchActivity.this, EsptouchDemoActivity.class);
                            startActivity(tent);
                            finish();
                        }else {
                            DialogInterface.OnClickListener listener=new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                    startActivityForResult(intent, REQUEST_LOCATION_SERVICE);
                                }
                            };
                            ECAlertDialog ecAlertDialog = ECAlertDialog.buildAlert(BeforeConfigEsptouchActivity.this,getResources().getString(R.string.permission_reject_location_service_tip),listener);
                            ecAlertDialog.setTitle(getResources().getString(R.string.permission_register));
                            ecAlertDialog.setButton(ECAlertDialog.BUTTON_POSITIVE,getResources().getString(R.string.goto_set),listener);
                            ecAlertDialog.show();
                        }
                    } else {
                        DialogInterface.OnClickListener listener=new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                PermissionUtils.startToSetting(BeforeConfigEsptouchActivity.this);
                            }
                        };
                        ECAlertDialog ecAlertDialog = ECAlertDialog.buildAlert(BeforeConfigEsptouchActivity.this,getResources().getString(R.string.permission_reject_location_tip),listener);
                        ecAlertDialog.setTitle(getResources().getString(R.string.permission_register));
                        ecAlertDialog.setButton(ECAlertDialog.BUTTON_POSITIVE,getResources().getString(R.string.goto_set),listener);
                        ecAlertDialog.show();
                    }
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTimer!=null){
            mTimer.cancel();
            mTimer = null;
        }
    }
}
