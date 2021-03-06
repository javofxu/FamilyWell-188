package me.hekr.sthome.equipment;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.firebase.iid.FirebaseInstanceId;
import com.igexin.sdk.PushManager;
import com.xiaomi.mipush.sdk.MiPushClient;

import org.json.JSONException;

import java.io.InvalidClassException;


import me.hekr.sdk.Hekr;
import me.hekr.sdk.inter.HekrCallback;
import me.hekr.sdk.utils.CacheUtil;
import me.hekr.sthome.DeviceListActivity;
import me.hekr.sthome.R;
import me.hekr.sthome.autoudp.ControllerWifi;
import me.hekr.sthome.common.CCPAppManager;
import me.hekr.sthome.common.TopbarSuperActivity;
import me.hekr.sthome.commonBaseView.ECAlertDialog;
import me.hekr.sthome.commonBaseView.SettingItem;

import me.hekr.sthome.configuration.activity.BeforeConfigEsptouchActivity;
import me.hekr.sthome.http.HekrUser;
import me.hekr.sthome.http.HekrUserAction;
import me.hekr.sthome.model.modelbean.MyDeviceBean;
import me.hekr.sthome.model.modeldb.DeviceDAO;
import me.hekr.sthome.push.logger.Log;
import me.hekr.sthome.tools.ECPreferenceSettings;
import me.hekr.sthome.tools.ECPreferences;
import me.hekr.sthome.tools.SiterSDK;
import me.hekr.sthome.tools.SystemUtil;
import me.hekr.sthome.tools.UnitTools;
import me.hekr.sthome.updateApp.UpdateAppAuto;
import me.hekr.sthome.xmipc.ActivityGuideDeviceAdd;

/**
 * Created by xjj on 2016/12/20.
 */
public class ConfigActivity extends TopbarSuperActivity implements View.OnClickListener{
    private static final String TAG = "ConfigActivity";
    private SettingItem mWifiTag;
    private DeviceDAO DDO;
    private static final int LOGOUT_SUCCESS = 1;

    @Override
    protected void onCreateInit() {
        setUpViews();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_config2;
    }


    public void setUpViews() {
        DDO = new DeviceDAO(this);
        findViewById(R.id.logout).setOnClickListener(this);//go out
        findViewById(R.id.configration).setOnClickListener(this);//hardare online
        SettingItem wificonfig = findViewById(R.id.wificonfig);
        mWifiTag = findViewById(R.id.wifitag);
        SettingItem modifypwd = findViewById(R.id.modifypwd);
        SettingItem aboutitem = findViewById(R.id.about);
        SettingItem instrunction = findViewById(R.id.instruction);
        UpdateAppAuto updateAppAuto = new UpdateAppAuto(this, aboutitem, false);
        SettingItem emergencyitem = findViewById(R.id.emergency);
        SettingItem gps_set = findViewById(R.id.gps_setting);
        mWifiTag.setOnClickListener(this);
        aboutitem.setOnClickListener(this);
        emergencyitem.setOnClickListener(this);
        modifypwd.setOnClickListener(this);
        wificonfig.setOnClickListener(this);
        instrunction.setOnClickListener(this);
        gps_set.setOnClickListener(this);
        getTopBarView().setTopBarStatus(1, 1, getResources().getString(R.string.setting), null, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        },null);
        refresh();
        updateAppAuto.getUpdateInfo();

        try {
            String url = CacheUtil.getString(SiterSDK.SETTINGS_CONFIG_INSTRUCTION,"");
            org.json.JSONObject jsonConfig = new org.json.JSONObject(url);
            if(jsonConfig.getBoolean("show")){
                instrunction.setVisibility(View.VISIBLE);
            }else {
                instrunction.setVisibility(View.GONE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            instrunction.setVisibility(View.GONE);
        }
    }


    private void refresh(){
        String name;
        try{
            MyDeviceBean d = DDO.findByChoice(1);
            if("报警器".equals(d.getDeviceName())){
                name = getResources().getString(R.string.my_home);
            }else{
                name = d.getDeviceName();
            }
            mWifiTag.setDetailText( name +"("+ d.getDevTid().substring(d.getDevTid().length()-4)+")");
        }
        catch (Exception e){
            mWifiTag.setDetailText(getResources().getString(R.string.please_choose_device));
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if(ControllerWifi.getInstance().choose_gateway){
            ControllerWifi.getInstance().choose_gateway = false;
            finish();
        }
        refresh();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.logout:
                onLoginOut();
                break;
            case R.id.configration:
                startActivity(new Intent(ConfigActivity.this,  BeforeConfigEsptouchActivity.class));
                break;
            case R.id.wifitag:
                startActivity(new Intent(ConfigActivity.this, DeviceListActivity.class));
                break;
            case R.id.about:
                startActivity(new Intent(ConfigActivity.this, AboutActivity.class));
                break;
            case R.id.emergency:
                startActivity(new Intent(ConfigActivity.this, EmergencyAcitivity.class));
                break;
            case R.id.modifypwd:
                startActivity(new Intent(ConfigActivity.this, ChangePasswordActivity.class));
                break;
            case R.id.wificonfig:
                startActivity(new Intent(ConfigActivity.this, ActivityGuideDeviceAdd.class));
                break;
            case R.id.gps_setting:
                startActivity(new Intent(ConfigActivity.this, SettingGpsEnableActivity.class));
                break;
            case R.id.instruction:
                startActivity(new Intent(ConfigActivity.this, InstructionActivity.class));
                break;
        }
    }

    private void onLoginOut(){
        ECAlertDialog elc = ECAlertDialog.buildAlert(this,getResources().getString(R.string.sure_to_logout), getResources().getString(R.string.cancel), getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showProgressDialog(getResources().getString(R.string.logouting));
                unbindFirebase();
                unbindHuawei();
                unbindXiaoMi();
                unbindGeTui();
                handler.sendEmptyMessageDelayed(LOGOUT_SUCCESS, 5000);
            }
        });
        elc.show();
    }

    private String getHuaWeiToken(){
        SharedPreferences sharedPreferences = ECPreferences.getSharedPreferences();
        ECPreferenceSettings flag = ECPreferenceSettings.SETTINGS_HUAWEI_TOKEN;
        return sharedPreferences.getString(flag.getId(), (String) flag.getDefaultValue());
    }

   private void unbindFirebase(){
       String fcmtoken = FirebaseInstanceId.getInstance().getToken();
       if(!TextUtils.isEmpty(fcmtoken)){
           HekrUserAction.getInstance(ConfigActivity.this).unPushTagBind(fcmtoken, 3, null);
       }
   }

   private void unbindHuawei(){
       if ("huawei".equals(SystemUtil.getDeviceBrand().toLowerCase()) || "honor".equals(SystemUtil.getDeviceBrand().toLowerCase())) {
           String token = getHuaWeiToken();
           if (!TextUtils.isEmpty(token)) {
               HekrUserAction.getInstance(ConfigActivity.this).unPushTagBind(token, 2, null);
           }
       }
   }

   private void unbindXiaoMi(){
       if ("xiaomi".equals(SystemUtil.getDeviceBrand().toLowerCase())) {
           String clientid = MiPushClient.getRegId(ConfigActivity.this);
           HekrUserAction.getInstance(ConfigActivity.this).unPushTagBind(clientid, 1, null);
       }
   }

   private void unbindGeTui(){
       String getui = PushManager.getInstance().getClientid(ConfigActivity.this);
       if(!TextUtils.isEmpty(getui)){
           HekrUserAction.getInstance(ConfigActivity.this).unPushTagBind(getui, 0, new HekrUser.UnPushTagBindListener() {
               @Override
               public void unPushTagBindSuccess() {
                   Log.i(TAG,"解绑个推成功");
               }

               @Override
               public void unPushTagBindFail(int errorCode) {
                   Log.i(TAG,"解绑个推失败:"+errorCode);
               }
           });
       }
   }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == LOGOUT_SUCCESS) {
                Hekr.getHekrUser().logout(new HekrCallback() {
                    @Override
                    public void onSuccess() {
                        hideProgressDialog();
                        try {
                            ECPreferences.savePreference(ECPreferenceSettings.SETTINGS_HUAWEI_TOKEN, "", true);
                        } catch (InvalidClassException e) {
                            e.printStackTrace();
                        }
                        HekrUserAction.getInstance(ConfigActivity.this).userLogout();
                        CCPAppManager.setClientUser(null);
                        finish();
                    }

                    @Override
                    public void onError(int errorCode, String message) {
                        hideProgressDialog();

                        if (errorCode == 1) {
                            try {
                                ECPreferences.savePreference(ECPreferenceSettings.SETTINGS_HUAWEI_TOKEN, "", true);
                            } catch (InvalidClassException e) {
                                e.printStackTrace();
                            }
                            HekrUserAction.getInstance(ConfigActivity.this).userLogout();

                            CCPAppManager.setClientUser(null);
                            finish();
                        } else {
                            try {
                                JSONObject d = JSON.parseObject(message);
                                int code = d.getInteger("code");
                                Toast.makeText(ConfigActivity.this, UnitTools.errorCode2Msg(ConfigActivity.this, code), Toast.LENGTH_LONG).show();
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(ConfigActivity.this, UnitTools.errorCode2Msg(ConfigActivity.this, errorCode), Toast.LENGTH_LONG).show();
                            }
                        }

                    }
                });
            }
            return false;
        }
    });
}
