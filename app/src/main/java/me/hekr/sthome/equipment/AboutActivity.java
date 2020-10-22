package me.hekr.sthome.equipment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InvalidClassException;
import java.util.List;

import me.hekr.sdk.Hekr;
import me.hekr.sdk.inter.HekrMsgCallback;
import me.hekr.sdk.utils.CacheUtil;
import me.hekr.sthome.MyApplication;
import me.hekr.sthome.R;
import me.hekr.sthome.common.ConfigureUtil;
import me.hekr.sthome.common.TopbarSuperActivity;
import me.hekr.sthome.commonBaseView.ECAlertDialog;
import me.hekr.sthome.commonBaseView.ECListDialog;
import me.hekr.sthome.commonBaseView.LoadingProceedDialog;
import me.hekr.sthome.commonBaseView.SettingItem;
import me.hekr.sthome.debugWindow.ViewWindow;
import me.hekr.sthome.event.LogoutEvent;
import me.hekr.sthome.http.HekrUser;
import me.hekr.sthome.http.HekrUserAction;
import me.hekr.sthome.http.bean.DeviceBean;
import me.hekr.sthome.http.bean.FirmwareBean;
import me.hekr.sthome.model.modeldb.DeviceDAO;
import me.hekr.sthome.tools.Config;
import me.hekr.sthome.tools.ConnectionPojo;
import me.hekr.sthome.tools.ECPreferenceSettings;
import me.hekr.sthome.tools.ECPreferences;
import me.hekr.sthome.tools.SiterSDK;
import me.hekr.sthome.tools.UnitTools;
import me.hekr.sthome.updateApp.ProgressEvent;
import me.hekr.sthome.updateApp.UpdateAppAuto;
import me.hekr.sthome.updateApp.UpdateService;

/**
 * Created by gc-0001 on 2017/1/20.
 */
public class AboutActivity extends TopbarSuperActivity implements View.OnClickListener{
    private final String TAG  = "AboutActivity";
    private SettingItem version_txt;
    private TextView siter;
    private LinearLayout downing_txt;
    private TextView intro_txt;
    private DeviceBean deviceBean;
    private FirmwareBean file;
    private UpdateAppAuto updateAppAuto;
    private ProgressBar bar;
    private ECListDialog ecListDialog;
    private ImageView logoimage;
    private ECAlertDialog ecAlertDialog;
    public LoadingProceedDialog loadingProceedDialog;
    private  String dd = "";

    @Override
    protected void onCreateInit() {
        EventBus.getDefault().register(this);
        initGuider();
        initView();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_about;
    }

    private void initGuider(){
        getTopBarView().setTopBarStatus(1, 2, getResources().getString(R.string.about), null, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 finish();
            }
        },null);
    }

    private void initView(){
        logoimage = (ImageView)findViewById(R.id.imageView1);
        bar         = (ProgressBar)findViewById(R.id.jindu);
        downing_txt = (LinearLayout)findViewById(R.id.downing);
        if(UpdateService.flag_updating){
            downing_txt.setVisibility(View.VISIBLE);
        }else{
            downing_txt.setVisibility(View.GONE);
        }
        siter       = (TextView)findViewById(R.id.guanwang);
        SettingItem sale = (SettingItem) findViewById(R.id.sale);
        SettingItem app_txt = (SettingItem) findViewById(R.id.app_version);
        SettingItem agreement = (SettingItem) findViewById(R.id.user_agreement);
        SettingItem privacy = (SettingItem) findViewById(R.id.privacy_policy);
        updateAppAuto = new UpdateAppAuto(this, app_txt,true);
        version_txt = (SettingItem)findViewById(R.id.gateway_version);
        intro_txt =   (TextView)findViewById(R.id.intro);
        String verName = Config.getVerName(this, getPackageName());
        app_txt.setDetailText(verName);
        siter.setText(getClickableSpan(0));
        siter.setMovementMethod(LinkMovementMethod.getInstance());//必须设置否则无效
        try {
            String url = CacheUtil.getString(SiterSDK.SETTINGS_CONFIG_WEBSITE,"");
            org.json.JSONObject jsonConfig = new org.json.JSONObject(url);
            boolean show = jsonConfig.getBoolean("show");
            if(show){
                siter.setVisibility(View.VISIBLE);
            }else {
                siter.setVisibility(View.GONE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sale.setOnClickListener(this);
        agreement.setOnClickListener(this);
        privacy.setOnClickListener(this);
        try {
            if(!TextUtils.isEmpty(ConnectionPojo.getInstance().deviceTid)){
                HekrUserAction.getInstance(this).getDevices(0,80,new HekrUser.GetDevicesListener() {
                    @Override
                    public void getDevicesSuccess(List<DeviceBean> devicesLists) {
                        for(DeviceBean d : devicesLists){
                            if(ConnectionPojo.getInstance().deviceTid.equals(d.getDevTid())){
                                deviceBean = d;
                                doActions();
                            }
                        }
                    }
                    @Override
                    public void getDevicesFail(int errorCode) {
                    }
                });

                version_txt.setOnClickListener(new View.OnClickListener(){

                    @Override
                    public void onClick(View v) {
                        if(file != null){
                            checkUpdatefirm(true);
                        }
                    }
                });
            }
        }catch (NullPointerException e){
            e.printStackTrace();
        }

        updateAppAuto.getUpdateInfo();

        intro_txt.setText(getResources().getString(R.string.about_app));
        logoimage.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

             if(isDebugMode()){
                 try {
                     ECPreferences.savePreference(ECPreferenceSettings.SETTINGS_DEBUG, false, true);
                 } catch (InvalidClassException e) {
                     e.printStackTrace();
                 }
                 showToast("调试模式结束");
                 ViewWindow.clearView();
             }else{

                 if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                     if(!Settings.canDrawOverlays(getApplicationContext())) {
                         //启动Activity让用户授权
                         Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                         intent.setData(Uri.parse("package:" + getPackageName()));
                         startActivityForResult(intent,100);
                     }else {
                         try {
                             ECPreferences.savePreference(ECPreferenceSettings.SETTINGS_DEBUG, true, true);
                         } catch (InvalidClassException e) {
                             e.printStackTrace();
                         }
                         showToast("调试模式开启");
                         ViewWindow.showView(AboutActivity.this,"调试模式开启",R.color.blue);
                     }
                 }else {
                     try {
                         ECPreferences.savePreference(ECPreferenceSettings.SETTINGS_DEBUG, true, true);
                     } catch (InvalidClassException e) {
                         e.printStackTrace();
                     }
                     showToast("调试模式开启");
                     ViewWindow.showView(AboutActivity.this,"调试模式开启",R.color.blue);
                 }
             }
             return false;
            }
        });
    }

    private void doActionSend() {
        String abc = "{\"msgId\":16810,\"action\":\"devUpgrade\"," +
                "\"params\":{\"appTid\":\""+ ConnectionPojo.getInstance().IMEI+"\"," +
                "\"devTid\":\""+deviceBean.getDevTid()+"\"," +
                "\"ctrlKey\":\""+deviceBean.getCtrlKey()+"\"," +
                "\"binUrl\":\""+file.getBinUrl()+"\"," +
                "\"md5\":\""+file.getMd5()+"\"," +
                "\"binType\":\""+file.getLatestBinType()+"\"," +
                "\"binVer\":\""+file.getLatestBinVer()+"\"," +
                "\"size\":"+file.getSize()+"}}";
        try {
            Hekr.getHekrClient().sendMessage(new JSONObject(abc), new HekrMsgCallback() {
                @Override
                public void onReceived(String msg) {

                }

                @Override
                public void onTimeout() {

                }

                @Override
                public void onError(int errorCode, String message) {

                }
            }, ConnectionPojo.getInstance().domain);



        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @Subscribe          //订阅事件FirstEvent
    public  void onEventMainThread(ProgressEvent event){
        int progress = event.getUpdateprogress();
        Log.i(TAG,"progress"+progress);
        if(progress<100){
            downing_txt.setVisibility(View.VISIBLE);
            bar.setProgress(progress);
        }else{
            bar.setProgress(progress);
            downing_txt.setVisibility(View.GONE);
        }

    }

    private void doActions() {
        HekrUserAction.getInstance(this).checkFirmwareUpdate(deviceBean.getDevTid(), deviceBean.getProductPublicKey(), deviceBean.getBinType(), deviceBean.getBinVersion(), new HekrUser.CheckFwUpdateListener() {
            @Override
            public void checkNotNeedUpdate() {
                version_txt.setDetailText(deviceBean.getBinVersion());
                version_txt.setNewUpdateVisibility(false);
            }

            @Override
            public void checkNeedUpdate(FirmwareBean firmwareBean) {
                file = firmwareBean;
                version_txt.setDetailText("v "+deviceBean.getBinVersion());
                version_txt.setNewUpdateVisibility(true);
            }

            @Override
            public void checkFail(int errorCode) {
                Toast.makeText(AboutActivity.this, UnitTools.errorCode2Msg(AboutActivity.this,errorCode),Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private SpannableString getClickableSpan(int start) {

        final UnitTools unitTools = new UnitTools(this);
        try {
            String url = CacheUtil.getString(SiterSDK.SETTINGS_CONFIG_WEBSITE,"");
            org.json.JSONObject jsonConfig = new org.json.JSONObject(url);
            org.json.JSONObject json2 = jsonConfig.getJSONObject("url");
            if(json2.has(unitTools.readLanguage())){
                dd = json2.getString(unitTools.readLanguage());
            }else {
                dd = json2.getString("default");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }



        View.OnClickListener l = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                Uri content_url = Uri.parse(dd);
                intent.setData(content_url);
                startActivity(intent);
            }
        };




        SpannableString spanableInfo = new SpannableString(dd);

        int end = spanableInfo.length();
        spanableInfo.setSpan(new Clickable(l), start, end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spanableInfo;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.user_agreement:
                String url = ConfigureUtil.getUserAgreement(this);
                onStartActivity(1, url);
                break;
            case R.id.privacy_policy:
                String url2 = ConfigureUtil.getPrivacyPolicy(this);
                onStartActivity(2, url2);
                break;
            case R.id.sale:
                openPhoneAlert();
                break;
        }
    }

    private void onStartActivity(int type, String url){
        Intent intent = new Intent();
        intent.setClass(AboutActivity.this, WebViewActivity.class);
        intent.putExtra("instructions_type", type);
        intent.putExtra("instructions_urls", url);
        startActivity(intent);
    }

    /**
     * 内部类，用于截获点击富文本后的事件
     */
    class Clickable extends ClickableSpan implements View.OnClickListener {
        private final View.OnClickListener mListener;

        public Clickable(View.OnClickListener mListener) {
            this.mListener = mListener;
        }

        @Override
        public void onClick(View v) {
            mListener.onClick(v);
        }
        @Override
        public void updateDrawState(TextPaint ds) {
            ds.setColor(getResources().getColor(R.color.black));
            ds.setUnderlineText(true);    //去除超链接的下划线
        }
    }


    private void openPhoneAlert(){

        String[] ad = getResources().getStringArray(R.array.lianxi);
        if(getPackageName().equals("com.siterwell.lifebox")){
            ad[0] = "SAV (0,85€/mn):"+ad[0];
        }
        
        ecListDialog = new ECListDialog(this,ad);
        ecListDialog.setOnDialogItemClickListener(new ECListDialog.OnDialogItemClickListener() {
            @Override
            public void onDialogItemClick(Dialog d, int position) {

                switch (position){
                    case 0:
                        Intent intent = new Intent(Intent.ACTION_DIAL);
                        intent.setData(Uri.parse("tel:"+getResources().getStringArray(R.array.lianxi)[0]));
                        if (intent.resolveActivity(getPackageManager()) != null) {
                            startActivity(intent);
                        }
                        break;
                    case 1:
                        Intent data=new Intent(Intent.ACTION_SENDTO);
                        data.setData(Uri.parse("mailto:"+getResources().getStringArray(R.array.lianxi)[1]));
                        startActivity(data);
                        break;
                    default:
                        break;
                }

            }
        });
        ecListDialog.show();



    }

    private boolean isDebugMode(){

        SharedPreferences sharedPreferences = ECPreferences.getSharedPreferences();
        ECPreferenceSettings flag = ECPreferenceSettings.SETTINGS_DEBUG;
        boolean autoflag = sharedPreferences.getBoolean(flag.getId(), (boolean) flag.getDefaultValue());
        return autoflag;
    }

    private void checkUpdatefirm(final boolean first){
        final DeviceDAO deviceDAO = new DeviceDAO(this);
        final DeviceBean d = deviceDAO.findByChoice(1);
        if(d!=null && ((first && d.isOnline()) || !first)){
            HekrUserAction.getInstance(this).checkFirmwareUpdate(d.getDevTid(),d.getProductPublicKey(), d.getBinType(), d.getBinVersion(), new HekrUser.CheckFwUpdateListener() {
                @Override
                public void checkNotNeedUpdate() {
                }

                @Override
                public void checkNeedUpdate(FirmwareBean firmwareBean) {
                    file = firmwareBean;
                    if(ecAlertDialog==null||!ecAlertDialog.isShowing()){
                        String s = null;
                        String s2 = null;
                        if(first){
                            s = String.format(getResources().getString(R.string.firewarm_to_update),file.getLatestBinVer());
                            s2 =  getResources().getString(R.string.ok);
                        }else {
                            s = getResources().getString(R.string.fail_upgrade);
                            s2 = getResources().getString(R.string.retry);
                        }

                        ecAlertDialog = ECAlertDialog.buildAlert(MyApplication.getActivity(),
                                s,
                                getResources().getString(R.string.now_not_to_update),
                                s2,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                }, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        doActionSend();
                                        loadingProceedDialog = new LoadingProceedDialog(AboutActivity.this);
                                        loadingProceedDialog.setResultListener(new LoadingProceedDialog.ResultListener() {
                                            @Override
                                            public void result(boolean success) {
                                                if(success){
                                                    Toast.makeText(AboutActivity.this,getResources().getString(R.string.success_upgrade),Toast.LENGTH_LONG).show();
                                                }else {
                                                    checkUpdatefirm(false);
                                                }
                                            }

                                            @Override
                                            public void proceed() {
                                                HekrUserAction.getInstance(AboutActivity.this).getDevices(d.getDevTid(), new HekrUser.GetDevicesListener() {
                                                    @Override
                                                    public void getDevicesSuccess(List<DeviceBean> devicesLists) {
                                                        if(devicesLists!=null &&devicesLists.size()>0){
                                                            DeviceBean deviceBean = devicesLists.get(0);
                                                            if(!d.getBinVersion().equals(deviceBean.getBinVersion())){
                                                                if(loadingProceedDialog!=null) loadingProceedDialog.setFlag_success(true);
                                                                version_txt.setEnabled(false);
                                                                version_txt.setNewUpdateVisibility(false);
                                                                version_txt.setDetailText(deviceBean.getBinVersion());
                                                                deviceDAO.updateDeivceBinversion(deviceBean.getDevTid(),deviceBean.getBinVersion());
                                                            }
                                                        }
                                                    }

                                                    @Override
                                                    public void getDevicesFail(int errorCode) {
                                                        com.litesuits.android.log.Log.i(TAG,"更新获取网关信息错误："+errorCode);
                                                    }
                                                });
                                            }
                                        });
                                        loadingProceedDialog.setPressText(getResources().getText(R.string.is_upgrading));
                                        loadingProceedDialog.setCancelable(false);
                                        loadingProceedDialog.show();
                                    }

                                });

                        ecAlertDialog.show();
                    }
                }

                @Override
                public void checkFail(int errorCode) {
                    if(errorCode==1){
                        LogoutEvent logoutEvent = new LogoutEvent();
                        EventBus.getDefault().post(logoutEvent);
                    }
                }
            });
        }
    }
}
