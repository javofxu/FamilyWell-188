package me.hekr.sthome.equipment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.firebase.iid.FirebaseInstanceId;
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
import me.hekr.sthome.commonBaseView.ECListDialog;
import me.hekr.sthome.commonBaseView.SettingItem;

import me.hekr.sthome.commonBaseView.ToastTools;
import me.hekr.sthome.configuration.activity.BeforeConfigEsptouchActivity;
import me.hekr.sthome.http.HekrUser;
import me.hekr.sthome.http.HekrUserAction;
import me.hekr.sthome.model.modelbean.MyDeviceBean;
import me.hekr.sthome.model.modeldb.DeviceDAO;
import me.hekr.sthome.push.logger.Log;
import me.hekr.sthome.service.SiterService;
import me.hekr.sthome.tools.Config;
import me.hekr.sthome.tools.ECPreferenceSettings;
import me.hekr.sthome.tools.ECPreferences;
import me.hekr.sthome.tools.SiterSDK;
import me.hekr.sthome.tools.SystemUtil;
import me.hekr.sthome.tools.UnitTools;
import me.hekr.sthome.updateApp.UpdateAppAuto;
import me.hekr.sthome.xmipc.ActivityGuideDeviceAdd;
import me.hekr.sthome.xmipc.ActivityGuideDeviceWifiConfig;

/**
 * Created by xjj on 2016/12/20.
 */
public class ConfigActivity extends TopbarSuperActivity implements View.OnClickListener{
    private static final String TAG = "ConfigActivity";
    private SettingItem wifitag,modifypwd,aboutitem,emergencyitem,wificonfig,instrunction,gps_set;
    private DeviceDAO DDO;
    private UpdateAppAuto updateAppAuto;
    private static final int LOGOUT_SUCCESS = 1;

    @Override
    protected void onCreateInit() {
        setUpViews();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_config2;
    }



    private String getHuaweiToken(){

        SharedPreferences sharedPreferences = ECPreferences.getSharedPreferences();
        ECPreferenceSettings flag = ECPreferenceSettings.SETTINGS_HUAWEI_TOKEN;
        String autoflag = sharedPreferences.getString(flag.getId(), (String) flag.getDefaultValue());
        return autoflag;
    }

    public void setUpViews() {
        DDO = new DeviceDAO(this);
        findViewById(R.id.logout).setOnClickListener(this);//go out
        findViewById(R.id.configration).setOnClickListener(this);//hardare online
        wificonfig = (SettingItem)findViewById(R.id.wificonfig);
        wifitag = (SettingItem)findViewById(R.id.wifitag);
        modifypwd = (SettingItem)findViewById(R.id.modifypwd);
        aboutitem = (SettingItem)findViewById(R.id.about);
        instrunction = (SettingItem)findViewById(R.id.instruction);
        updateAppAuto = new UpdateAppAuto(this,aboutitem,false);
        emergencyitem =(SettingItem)findViewById(R.id.emergency);
        gps_set =(SettingItem)findViewById(R.id.gps_setting);
        wifitag.setOnClickListener(this);
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
        String name = "";
            try{
                MyDeviceBean d = DDO.findByChoice(1);
                if("报警器".equals(d.getDeviceName())){
                    name = getResources().getString(R.string.my_home);

                }else{
                    name = d.getDeviceName();
                }

                wifitag.setDetailText( name +"("+ d.getDevTid().substring(d.getDevTid().length()-4)+")");
            }
            catch (Exception e){
                wifitag.setDetailText(getResources().getString(R.string.please_choose_device));
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
            case R.id.logout://logout;

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
                                handler.sendEmptyMessageDelayed(LOGOUT_SUCCESS, 5000);
                    }
                });
                elc.show();


                break;
            case R.id.configration:
                startActivity(new Intent(ConfigActivity.this,  BeforeConfigEsptouchActivity.class));
                break;
            case R.id.wifitag:
                Intent i = new Intent(ConfigActivity.this,DeviceListActivity.class);
                startActivity(i);
                break;
            case R.id.about:
                gotoAboutActivity();
                break;
            case R.id.emergency:
                startActivity(new Intent(ConfigActivity.this,EmergencyAcitivity.class));
                break;
            case R.id.modifypwd:
                startActivity(new Intent(ConfigActivity.this,ChangePasswordActivity.class));
                break;
            case R.id.wificonfig:
                openCameraAlert();
                break;
            case R.id.gps_setting:
                startActivity(new Intent(ConfigActivity.this,SettingGpsEnableActivity.class));
                break;
            case R.id.instruction:


                UnitTools unitTools =new UnitTools(this);

                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");

                try {
                    String url = CacheUtil.getString(SiterSDK.SETTINGS_CONFIG_INSTRUCTION,"");
                    org.json.JSONObject jsonConfig = new org.json.JSONObject(url);
                    org.json.JSONObject json2 = jsonConfig.getJSONObject("url");
                    if(json2.has(unitTools.readLanguage())){
                        String url_last = json2.getString(unitTools.readLanguage());
                        Uri content_url = Uri.parse(url_last);
                        intent.setData(content_url);
                        startActivity(intent);
                    }else {
                       String url_last = json2.getString("default");
                        Uri content_url = Uri.parse(url_last);
                        intent.setData(content_url);
                        startActivity(intent);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                break;
            default:
                break;
        }
    }



    private void openCameraAlert(){
        Intent intent2 = new Intent(ConfigActivity.this,ActivityGuideDeviceAdd.class);
        startActivity(intent2);
    }

   private void gotoAboutActivity(){
      startActivity(new Intent(ConfigActivity.this,AboutActivity.class));

   }

   private void unbindFirebase(){
       String fcmtoken = FirebaseInstanceId.getInstance().getToken();
       if(!TextUtils.isEmpty(fcmtoken)){
           HekrUserAction.getInstance(ConfigActivity.this).unPushTagBind(fcmtoken, 3, null);
       }
   }

   private void unbindHuawei(){
       if ("huawei".equals(SystemUtil.getDeviceBrand().toLowerCase()) || "honor".equals(SystemUtil.getDeviceBrand().toLowerCase())) {
           String token = getHuaweiToken();
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




    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LOGOUT_SUCCESS:


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

                            if(errorCode==1){
                                try {
                                    ECPreferences.savePreference(ECPreferenceSettings.SETTINGS_HUAWEI_TOKEN, "", true);
                                } catch (InvalidClassException e) {
                                    e.printStackTrace();
                                }
                                HekrUserAction.getInstance(ConfigActivity.this).userLogout();

                                CCPAppManager.setClientUser(null);
                                finish();
                            }else {
                                try {
                                    JSONObject d = JSON.parseObject(message);
                                    int code = d.getInteger("code");
                                    Toast.makeText(ConfigActivity.this,UnitTools.errorCode2Msg(ConfigActivity.this,code),Toast.LENGTH_LONG).show();
                                }catch (Exception e){
                                    e.printStackTrace();
                                    Toast.makeText(ConfigActivity.this,UnitTools.errorCode2Msg(ConfigActivity.this,errorCode),Toast.LENGTH_LONG).show();
                                }
                            }

                        }
                    });


                    break;
            }

        }

    };




}
