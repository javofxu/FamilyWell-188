package me.hekr.sthome.configuration.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

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

    @Override
    protected void onCreateInit() {
        initview();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_before_config_esptouch;
    }

    private void initview(){
        next_btn = (Button)findViewById(R.id.next);
        image =(ImageView)findViewById(R.id.imageView1);
        image.setBackgroundResource(R.drawable.config_tishi);
        ad = (AnimationDrawable) image.getBackground();
        next_btn.setOnClickListener(this);
        getTopBarView().setTopBarStatus(1, 1, getResources().getString(R.string.net_configuration), null, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 finish();
            }
        },null);



        image.post(new Runnable()
        {

            @Override

            public void run()

            {

                ad.start();

            }

        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.next:
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
                    if(PermissionUtils.requestPermission(this,permission,REQUEST_LOCATION)){
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

                    }
                }else {
                    Intent tent = new Intent(BeforeConfigEsptouchActivity.this, EsptouchDemoActivity.class);
                    startActivity(tent);
                    finish();
                }

                break;
            default:break;
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode!=RESULT_OK) return;

        if(requestCode==REQUEST_LOCATION_SERVICE){


        }
    }


}
