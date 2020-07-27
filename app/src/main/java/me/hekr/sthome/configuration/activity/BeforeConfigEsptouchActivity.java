package me.hekr.sthome.configuration.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.net.wifi.ScanResult;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import me.hekr.sthome.R;
import me.hekr.sthome.common.TopbarSuperActivity;
import me.hekr.sthome.commonBaseView.ECAlertDialog;
import me.hekr.sthome.tools.PermissionUtils;
import me.hekr.sthome.tools.UnitTools;

/**
 * Created by gc-0001 on 2017/2/10.
 */
public class BeforeConfigEsptouchActivity extends TopbarSuperActivity implements View.OnClickListener{
    private Button next_btn;
    private ImageView image;
    private AnimationDrawable ad;
    private static final int REQUEST_LOCATION=1001;
    private static final int REQUEST_LOCATION_SERVICE=1002;
    private String[] permission = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};

    private boolean isAPConnect = false;
    private String mApSSId;
    private String mApPwd;

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
        next_btn = findViewById(R.id.next);
        image = findViewById(R.id.imageView1);
        image.setBackgroundResource(R.drawable.config_tishi);
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
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.next) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
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
                        ECAlertDialog ecAlertDialog = ECAlertDialog.buildAlert(BeforeConfigEsptouchActivity.this, getResources().getString(R.string.permission_reject_location_service_tip), listener);
                        ecAlertDialog.setTitle(getResources().getString(R.string.permission_register));
                        ecAlertDialog.setButton(ECAlertDialog.BUTTON_POSITIVE, getResources().getString(R.string.goto_set), listener);
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
            configApWifi();
        }else {
            startActivity(new Intent(this, EsptouchDemoActivity.class));
            finish();
        }
    }

    private void configApWifi(){
        EspWifiAdminSimple mWifiAdmin = new EspWifiAdminSimple(this);
        ScanResult wifi = mWifiAdmin.getWifiList();
        if (wifi != null) {
            String SSId = wifi.SSID;
            boolean isConnect = mWifiAdmin.addNetwork(SSId, null, 0);
            if (isConnect) {
                Intent intent = new Intent(this, EsptouchAnimationActivity.class);
                intent.putExtra("isApConnect", true);
                intent.putExtra("ssid", mApSSId);
                intent.putExtra("psw", mApPwd);
                startActivity(intent);
                finish();
            }
        }
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
}
