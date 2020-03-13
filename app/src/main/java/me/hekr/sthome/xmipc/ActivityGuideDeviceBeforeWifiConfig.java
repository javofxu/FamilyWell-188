package me.hekr.sthome.xmipc;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;

import me.hekr.sthome.R;
import me.hekr.sthome.common.TopbarIpcSuperActivity;
import me.hekr.sthome.commonBaseView.ECAlertDialog;
import me.hekr.sthome.configuration.activity.BeforeConfigEsptouchActivity;
import me.hekr.sthome.configuration.activity.EsptouchDemoActivity;
import me.hekr.sthome.service.NetWorkUtils;
import me.hekr.sthome.tools.PermissionUtils;
import me.hekr.sthome.tools.UnitTools;

/**
 * Created by st on 2017/12/25.
 */

public class ActivityGuideDeviceBeforeWifiConfig extends TopbarIpcSuperActivity implements View.OnClickListener{

    private Button btn_next;
    private static final int REQUEST_LOCATION=1001;
    private static final int REQUEST_LOCATION_SERVICE=1002;
    private String[] permission = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};

    @Override
    protected void onCreateInit() {
         textView_title.setText(R.string.add_my_ipc);
         textView_back.setOnClickListener(this);
        btn_next= (Button)findViewById(R.id.next);
        btn_next.setOnClickListener(this);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_ipc_before_wifi_config;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.backBtnInTopLayout:
                finish();
                break;
            case R.id.next:

                if(NetWorkUtils.getNetWorkType(this)<4){
                    showToast(R.string.no_wifi);
                }else{

                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
                        if(PermissionUtils.requestPermission(this,permission,REQUEST_LOCATION)){
                            if(UnitTools.isLocServiceEnable(this)){
                                Intent tent = new Intent(ActivityGuideDeviceBeforeWifiConfig.this, ActivityGuideDeviceWifiConfigNew.class);
                                startActivity(tent);
                            }else {

                                DialogInterface.OnClickListener listener=new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                        startActivityForResult(intent, REQUEST_LOCATION_SERVICE);
                                    }
                                };
                                ECAlertDialog ecAlertDialog = ECAlertDialog.buildAlert(ActivityGuideDeviceBeforeWifiConfig.this,getResources().getString(R.string.permission_reject_location_service_tip),listener);
                                ecAlertDialog.setTitle(getResources().getString(R.string.permission_register));
                                ecAlertDialog.setButton(ECAlertDialog.BUTTON_POSITIVE,getResources().getString(R.string.goto_set),listener);
                                ecAlertDialog.show();


                            }

                        }
                    }else {
                        Intent tent = new Intent(ActivityGuideDeviceBeforeWifiConfig.this, ActivityGuideDeviceWifiConfigNew.class);
                        startActivity(tent);
                    }


                }

                break;

        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION) {
            if (permissions != null && grantResults != null &&
                    permissions.length == grantResults.length) {
                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        if(UnitTools.isLocServiceEnable(this)){
                            Intent tent = new Intent(ActivityGuideDeviceBeforeWifiConfig.this, ActivityGuideDeviceWifiConfigNew.class);
                            startActivity(tent);
                        }else {

                            DialogInterface.OnClickListener listener=new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                    startActivityForResult(intent, REQUEST_LOCATION_SERVICE);
                                }
                            };
                            ECAlertDialog ecAlertDialog = ECAlertDialog.buildAlert(ActivityGuideDeviceBeforeWifiConfig.this,getResources().getString(R.string.permission_reject_location_service_tip),listener);
                            ecAlertDialog.setTitle(getResources().getString(R.string.permission_register));
                            ecAlertDialog.setButton(ECAlertDialog.BUTTON_POSITIVE,getResources().getString(R.string.goto_set),listener);
                            ecAlertDialog.show();


                        }
                    } else {
                        DialogInterface.OnClickListener listener=new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                PermissionUtils.startToSetting(ActivityGuideDeviceBeforeWifiConfig.this);
                            }
                        };
                        ECAlertDialog ecAlertDialog = ECAlertDialog.buildAlert(ActivityGuideDeviceBeforeWifiConfig.this,getResources().getString(R.string.permission_reject_location_tip),listener);
                        ecAlertDialog.setTitle(getResources().getString(R.string.permission_register));
                        ecAlertDialog.setButton(ECAlertDialog.BUTTON_POSITIVE,getResources().getString(R.string.goto_set),listener);
                        ecAlertDialog.show();
                    }
                }
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode!=RESULT_OK) return;

        if(requestCode==REQUEST_LOCATION_SERVICE){


        }
    }
}
