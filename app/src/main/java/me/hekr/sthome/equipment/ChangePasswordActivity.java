package me.hekr.sthome.equipment;

import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.igexin.sdk.PushManager;
import com.litesuits.android.log.Log;
import com.xiaomi.mipush.sdk.MiPushClient;

import org.greenrobot.eventbus.EventBus;

import java.io.InvalidClassException;

import me.hekr.sdk.utils.CacheUtil;
import me.hekr.sthome.R;
import me.hekr.sthome.common.TopbarSuperActivity;
import me.hekr.sthome.crc.CoderUtils;
import me.hekr.sthome.event.STEvent;
import me.hekr.sthome.http.HekrUser;
import me.hekr.sthome.http.HekrUserAction;
import me.hekr.sthome.tools.ECPreferenceSettings;
import me.hekr.sthome.tools.ECPreferences;
import me.hekr.sthome.tools.SiterSDK;
import me.hekr.sthome.tools.SystemUtil;
import me.hekr.sthome.tools.UnitTools;

/**
 * Created by gc-0001 on 2017/4/21.
 */

public class ChangePasswordActivity extends TopbarSuperActivity {
    private static final String TAG = "ChangePasswordActivity";
    private EditText oldtext,newtext,confirmtext;
    @Override
    protected void onCreateInit() {
        initView();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_change_password;
    }

    private void initView(){

         getTopBarView().setTopBarStatus(1, 2, getResources().getString(R.string.modify_password), getResources().getString(R.string.ok), new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 hideSoftKeyboard();
                 finish();
             }
         }, new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 modify();
             }
         });
        oldtext = (EditText)findViewById(R.id.old_code);
        newtext = (EditText)findViewById(R.id.new_code);
        confirmtext = (EditText)findViewById(R.id.new_code_confirm);

    }


    private void modify(){
        String oldpsw = oldtext.getText().toString().trim();
       final String newpsw = newtext.getText().toString().trim();
        String confirmpsw = confirmtext.getText().toString().trim();

        if(TextUtils.isEmpty(oldpsw)){
            Toast.makeText(this,getResources().getString(R.string.setting_old_password),Toast.LENGTH_SHORT).show();
            return;
        }else if(TextUtils.isEmpty(newpsw)){
            Toast.makeText(this,getResources().getString(R.string.setting_new_password_hint),Toast.LENGTH_SHORT).show();
            return;
        }else if(TextUtils.isEmpty(confirmpsw)){
            Toast.makeText(this,getResources().getString(R.string.setting_confirm_new_password_hint),Toast.LENGTH_SHORT).show();
            return;
        }else if(!confirmpsw.equals(newpsw)){
            Toast.makeText(this,getResources().getString(R.string.password_two_different),Toast.LENGTH_SHORT).show();
            return;
        }else if(confirmpsw.length()<6){
            Toast.makeText(this,getResources().getString(R.string.password_length),Toast.LENGTH_SHORT).show();
            return;
        }else{
             showProgressDialog(getResources().getString(R.string.modifying_newpassword));
            HekrUserAction.getInstance(this).changePassword(newpsw, oldpsw, new HekrUser.ChangePwdListener() {
                @Override
                public void changeSuccess() {

                    HekrUserAction.getInstance(ChangePasswordActivity.this).unPushAllTagBind(new HekrUser.UnPushTagBindListener() {
                        @Override
                        public void unPushTagBindSuccess() {
                            try {
                                ECPreferences.savePreference(ECPreferenceSettings.SETTINGS_PASSWORD, CoderUtils.getEncrypt(newpsw),true);
                            } catch (InvalidClassException e) {
                                e.printStackTrace();
                            }
                            Toast.makeText(ChangePasswordActivity.this, getResources().getString(R.string.success), Toast.LENGTH_SHORT).show();
                            String de = CacheUtil.getString(SiterSDK.SETTINGS_CONFIG_REGION, "");
                            if (de.contains("hekreu.me")) {
                                String fcmclientid = FirebaseInstanceId.getInstance().getToken();
                                if (!TextUtils.isEmpty(fcmclientid)) {
                                    Log.i(TAG, "FCM平台CLIENTID：" + fcmclientid);
                                    STEvent stEvent = new STEvent();
                                    stEvent.setRefreshevent(9);
                                    stEvent.setFcm_token(fcmclientid);
                                    EventBus.getDefault().post(stEvent);

                                    HekrUserAction.getInstance(ChangePasswordActivity.this).unPushTagBind(PushManager.getInstance().getClientid(ChangePasswordActivity.this), 0, new HekrUser.UnPushTagBindListener() {
                                        @Override
                                        public void unPushTagBindSuccess() {
                                            Log.i(TAG, "FCM绑定的同时解绑个推成功");
                                        }

                                        @Override
                                        public void unPushTagBindFail(int errorCode) {
                                            Log.i(TAG, "FCM绑定的同时解绑个推失败");
                                        }
                                    });
                                    com.huawei.android.pushagent.api.PushManager.requestToken(ChangePasswordActivity.this);


                                }
                            } else {
                                if ("honor".equals(SystemUtil.getDeviceBrand().toLowerCase()) || "huawei".equals(SystemUtil.getDeviceBrand().toLowerCase())) {

                                    com.huawei.android.pushagent.api.PushManager.requestToken(ChangePasswordActivity.this);
                                } else if ("xiaomi".equals(SystemUtil.getDeviceBrand().toLowerCase())) {
                                    String ds = MiPushClient.getRegId(ChangePasswordActivity.this);
                                    Log.i(TAG, "小米平台CLIENTID：" + ds);
                                    if (!TextUtils.isEmpty(ds)) {
                                        STEvent stEvent = new STEvent();
                                        stEvent.setRefreshevent(13);
                                        stEvent.setFcm_token(ds);
                                        EventBus.getDefault().post(stEvent);
                                    } else {
                                        Log.i(TAG, "小米平台CLIENTID为空");
                                    }
                                } else {

                                    String cid = PushManager.getInstance().getClientid(ChangePasswordActivity.this);
                                    if (!TextUtils.isEmpty(cid)) {
                                        Log.i(TAG, "个推client id =" + cid);
                                        STEvent stEvent = new STEvent();
                                        stEvent.setRefreshevent(11);
                                        stEvent.setFcm_token(cid);
                                        EventBus.getDefault().post(stEvent);
                                    } else {
                                        Log.i(TAG, "个推client id为空");
                                    }
                                }

                            }
                            hideProgressDialog();
                            hideSoftKeyboard();
                            finish();
                        }

                        @Override
                        public void unPushTagBindFail(int errorCode) {
                            hideProgressDialog();
                            Toast.makeText(ChangePasswordActivity.this, UnitTools.errorCode2Msg(ChangePasswordActivity.this,errorCode),Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void changeFail(int errorCode) {
                    hideProgressDialog();
                    if(errorCode == 400014){
                        Toast.makeText(ChangePasswordActivity.this, getResources().getString(R.string.code_fault),Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(ChangePasswordActivity.this, UnitTools.errorCode2Msg(ChangePasswordActivity.this,errorCode),Toast.LENGTH_SHORT).show();
                    }

                }
            });

        }

    }
}
