package me.hekr.sthome.configuration.activity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import me.hekr.sdk.utils.CacheUtil;
import me.hekr.sdk.utils.LogUtil;
import me.hekr.sthome.DeviceListActivity;
import me.hekr.sthome.R;
import me.hekr.sthome.autoudp.ControllerWifi;
import me.hekr.sthome.common.CCPAppManager;
import me.hekr.sthome.common.DateUtil;
import me.hekr.sthome.common.TopbarSuperActivity;
import me.hekr.sthome.commonBaseView.CustomStatusView;
import me.hekr.sthome.commonBaseView.ECAlertDialog;
import me.hekr.sthome.commonBaseView.ProgressDialog;
import me.hekr.sthome.configuration.EsptouchTask;
import me.hekr.sthome.configuration.IEsptouchListener;
import me.hekr.sthome.configuration.IEsptouchResult;
import me.hekr.sthome.configuration.IEsptouchTask;
import me.hekr.sthome.event.STEvent;
import me.hekr.sthome.event.UdpConfigEvent;
import me.hekr.sthome.http.HekrUser;
import me.hekr.sthome.http.HekrUserAction;
import me.hekr.sthome.http.bean.BindDeviceBean;
import me.hekr.sthome.http.bean.DcInfo;
import me.hekr.sthome.http.bean.DeviceBean;
import me.hekr.sthome.main.MainActivity;
import me.hekr.sthome.model.modelbean.MyDeviceBean;
import me.hekr.sthome.model.modelbean.SceneBean;
import me.hekr.sthome.model.modelbean.SysModelBean;
import me.hekr.sthome.model.modeldb.DeviceDAO;
import me.hekr.sthome.model.modeldb.SceneDAO;
import me.hekr.sthome.model.modeldb.SysmodelDAO;
import me.hekr.sthome.model.newstyle.NewGroup2Activity;
import me.hekr.sthome.service.TcpClientThread;
import me.hekr.sthome.tools.ConnectionPojo;
import me.hekr.sthome.tools.SendOtherData;
import me.hekr.sthome.tools.SiterSDK;
import me.hekr.sthome.tools.UnitTools;

/**
 * Created by gc-0001 on 2017/2/15.
 */
public class EsptouchAnimationActivity extends TopbarSuperActivity implements View.OnClickListener{
    private final String TAG = EsptouchAnimationActivity.class.getName();
    private CustomStatusView roundProgressView;
    private EspWifiAdminSimple mWifiAdmin;
    private Timer timer = null;
    private TextView textView, mProgress;
    private int count = 0;
    private final int SPEED1 = 2;
    private final int SPEED2 = 20;
    private final int SPEED3 = 30000;
    private int Now_speed;
    private String apSsid;
    private String apPassword;
    private String apBssid;
    private String  isSsidHiddenStr;
    private String taskResultCountStr;
    private EspTouchAsyncTask task3;
    private IEsptouchTask mEsptouchTask;
    private String already_deivce_name;

    private HekrUserAction hekrUserAction;
    private Button btn_retry, btn_AP;
    private int flag = -1;  //1代表绑定成功,2代表绑定失败，3代表已绑定其他设备,4代表回调绑定失败
    private SysmodelDAO sysmodelDAO;
    private SceneDAO sceneDAO;
    private String failmsg = null;
    private SendOtherData sendOtherData;
    private String choosetoDeviceid;
    private String gatewaytype;
    private int count_of_bind = 0;
    private TextView fail_reason_view;
    private DecimalFormat mFormat;
    private boolean isApConnect = false;

    @Override
    protected void onCreateInit() {
        EventBus.getDefault().register(this);
        isApConnect = getIntent().getBooleanExtra("isApConnect", false);
        initSSIdInfo();
        init();
        mFormat = new DecimalFormat("##0");
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_esp_animation;
    }

    private void initSSIdInfo(){
        sendOtherData = new SendOtherData(this);
        mWifiAdmin = new EspWifiAdminSimple(this);
        apSsid = getIntent().getStringExtra("ssid");
        apPassword = getIntent().getStringExtra("psw");
        apBssid = mWifiAdmin.getWifiConnectedBssid();
        isSsidHiddenStr = "NO";
        taskResultCountStr = "1";
    }

    private void init(){
        fail_reason_view = findViewById(R.id.fail_reason);
        fail_reason_view.setText(getClickableSpan());
        fail_reason_view.setMovementMethod(LinkMovementMethod.getInstance());
        fail_reason_view.setVisibility(View.GONE);
        gatewaytype = getIntent().getStringExtra("gatewaytype");
        sysmodelDAO = new SysmodelDAO(this);
        sceneDAO = new SceneDAO(this);
        btn_retry = findViewById(R.id.retry);
        btn_retry.setOnClickListener(this);
        btn_AP = findViewById(R.id.ap_config);
        btn_AP.setOnClickListener(this);
        textView = findViewById(R.id.tishi);
        roundProgressView = findViewById(R.id.roundprogress);
        mProgress = findViewById(R.id.progress_num);
        roundProgressView.loadLoading();
        getTopBarView().setTopBarStatus(1, 1, getResources().getString(R.string.net_configuration), null, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                STEvent stEvent2 = new STEvent();
                stEvent2.setServiceevent(10);
                EventBus.getDefault().post(stEvent2);
                finish();
            }
        }, null);

        hekrUserAction = HekrUserAction.getInstance(this);
        Now_speed = SPEED1;
        timer = new Timer();
        MyTask timerTask = new MyTask();
        timer.schedule(timerTask,0,1);
        if (!isApConnect) {
            textView.setText(getString(R.string.esptouch_is_configuring));
            task3 = new EspTouchAsyncTask();
            task3.execute(apSsid, apBssid, apPassword, isSsidHiddenStr, taskResultCountStr);
        }else {
            ControllerWifi.getInstance().ap_config_ing = true;
            textView.setText(getString(R.string.wait_connect_wifi));
            sendTcpCode();
        }
    }

    private Handler handler = new Handler(new Handler.Callback() {
        @Override




        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    textView.setText(R.string.information_success);
                    String receive = (String) msg.obj;
                    Log.i(TAG, "handleMessage: " + receive);
                    getTcpResponse(receive);
                    break;
                case 1:
                    int progress = (int)msg.obj;
                    UpdateInfo(progress);
                    break;
                case 2:
                    mProgress.setVisibility(View.GONE);
                    roundProgressView.loadFailure();
                    Now_speed = SPEED1;
                    cancelTask();
                    btn_retry.setVisibility(View.VISIBLE);
                    btn_AP.setVisibility(View.VISIBLE);
                    fail_reason_view.setVisibility(View.VISIBLE);
                    LogUtil.i(TAG,"flag_标志为:"+flag);
                    showTextMsg(flag);
                    break;
                case 3:
                    mProgress.setVisibility(View.GONE);
                    roundProgressView.loadSuccess();
                    Log.i(TAG,"跳转到成功页面");
                    if(timer!=null){
                        timer.cancel();
                        timer = null;
                    }
                    connectSuccess();
                    break;
                case 4:
                    if(count_of_bind<8){
                        count_of_bind ++;
                        Log.i(TAG,"绑定次数count_of_bind："+count_of_bind);
                        connect();
                    }else{
                        count_of_bind = 0;
                        flag = 2;
                        failmsg = getResources().getString(R.string.network_timeout);
                    }
                    break;
                case 5:
                    connect();
                    break;
                case 0x111:
                    flag = 111;
                    mProgress.setVisibility(View.GONE);
                    roundProgressView.loadFailure();
                    Now_speed = SPEED1;
                    cancelTask();
                    btn_retry.setVisibility(View.VISIBLE);
                    btn_AP.setVisibility(View.VISIBLE);
                    fail_reason_view.setVisibility(View.VISIBLE);
                    LogUtil.i(TAG,"flag_标志为:"+flag);
                    showTextMsg(flag);
                    break;
                case 0x112:
                    checkNetWorkWifi();
                    break;
            }
            return false;
        }
    });

    private void UpdateInfo(int count){
        String num = mFormat.format(((float) count)/2000f) + "%";
        mProgress.setText(num);
    }

    private void gatewayConfigSuccess(final String deviceName){
        handler.post(new Runnable() {
            @Override
            public void run() {
                @SuppressLint({"StringFormatInvalid", "LocalSuppress"}) String text = String.format(getString(R.string.is_connected_to_wifi), deviceName);
                showToast(text);
                textView.setText(getResources().getString(R.string.device_has_been_find));
            }
        });
    }

    private void connectSuccess(){
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                MainActivity.flag_checkfireware = true;
                Intent intent = new Intent(EsptouchAnimationActivity.this, DeviceListActivity.class);
                intent.putExtra("devid",choosetoDeviceid);
                startActivity(intent);
                finish();
            }
        }, 1200);
    }

    private void connect(){
        Log.i(TAG, "connect: " + ControllerWifi.getInstance().deviceTid + "--" + ControllerWifi.getInstance().bind);
        BindDeviceBean bindDeviceBean = new BindDeviceBean(ControllerWifi.getInstance().deviceTid, ControllerWifi.getInstance().bind, getResources().getString(R.string.my_home), getResources().getString(R.string.app_name));
        HekrUserAction.getInstance(this).bindDevice(bindDeviceBean, new HekrUser.BindDeviceListener() {
            @Override
            public void bindDeviceSuccess(DeviceBean deviceBean) {
                Log.i(TAG, "bindDeviceSuccess: 绑定成功");
                bindSuccess(deviceBean);
            }

            @Override
            public void bindDeviceFail(int errorCode) {
                bindFail(errorCode);
            }
        });
    }

    private void getTcpResponse(String receive){
        try {
            JSONObject jsonObject = new JSONObject(receive);
            String deviceName = (String) jsonObject.get("rev");
            Log.i(TAG, "handleMessage: " + deviceName);
            ConnectionPojo.getInstance().deviceTid = deviceName;
            mWifiAdmin.addNetwork(apSsid, apPassword, 2);
            onSearchBindKey();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void onSearchBindKey(){
        new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                handler.sendEmptyMessage(0x112);
                try {
                    Thread.sleep(20000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                STEvent stEvent =new STEvent();
                stEvent.setServiceevent(9);
                EventBus.getDefault().post(stEvent);
            }
        }.start();
    }

    //检查是否是配置的WIFI，不是的话弹框提示设置
    private void checkNetWorkWifi(){
        EspWifiAdminSimple mWifiAdmin = new EspWifiAdminSimple(this);
        String currentWifi = mWifiAdmin.getWifiConnectedSsid();
        if(!apSsid.equals(currentWifi)){
            ECAlertDialog  ecAlertDialog =  ECAlertDialog.buildAlert(EsptouchAnimationActivity.this, String.format(getResources().getString(R.string.current_ssid_is_not_correct), apSsid), getResources().getString(R.string.cancel), getResources().getString(R.string.goto_set_wifi), null, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent intent = new Intent();
                    if (android.os.Build.VERSION.SDK_INT >= 11) {
                        //Honeycomb
                        intent.setClassName("com.android.settings", "com.android.settings.Settings$WifiSettingsActivity");
                    } else {
                        //other versions
                        intent.setClassName("com.android.settings"
                                , "com.android.settings.wifi.WifiSettings");
                    }
                    startActivity(intent);
                }
            });
            ecAlertDialog.show();
        }
    }


    //发送时间信息
    private void sendTimeInfo(){
        new Thread(){
            @Override
            public void run() {
                sendOtherData.timeCheck();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                sendOtherData.timeZoneCheck(DateUtil.getCurrentTimeZone());
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.i(TAG, "开始绑定用户账号");
                handler.sendEmptyMessage(5);
            }
        }.start();
    }

    private void sendTcpCode(){
        String de  = CacheUtil.getString(SiterSDK.SETTINGS_CONFIG_REGION,"");
        String service = de.contains("hekreu.me") ? "site07" : "site06";
        JSONObject json = new JSONObject();
        try {
            json.put("ssid", apSsid);
            json.put("pwd", apPassword);
            json.put("server", service);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "initApConnect: " + json.toString());
        TcpClientThread mTcpClient = new TcpClientThread(handler, "192.168.4.1", 80, json.toString());
        mTcpClient.start();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.retry:
                if(flag==8){
                    finish();
                }else {
                    Intent intent = new Intent(EsptouchAnimationActivity.this, BeforeConfigEsptouchActivity.class);
                    startActivity(intent);
                    finish();
                }
                break;
            case R.id.ap_config:
                Intent intent = new Intent(EsptouchAnimationActivity.this, BeforeConfigEsptouchActivity.class);
                intent.putExtra("isAp", true);
                intent.putExtra("SSID", apSsid);
                intent.putExtra("PWD", apPassword);
                startActivity(intent);
                finish();
                break;
        }
    }

    private void showTextMsg(int flag){
        switch (flag){
            case 6:
                textView.setText(getResources().getString(R.string.local_fail_to_get_info));
                break;
            case 7:
                textView.setText(getResources().getString(R.string.gateway_wifi_connect_but_not_connect_service));
                break;
            case -1:
            case 5:
                textView.setText(getResources().getString(R.string.failed_Esptouch_check_eq));
                break;
            case 0:
                textView.setText(getResources().getString(R.string.failed_Esptouch_check_net));
                break;
            case 2:
            case 4:
                if(!TextUtils.isEmpty(failmsg))
                    textView.setText(failmsg);
                break;
            case 8:
                if(!TextUtils.isEmpty(failmsg))
                    textView.setText(failmsg);
                btn_retry.setText(getResources().getString(R.string.re_login));
                break;
            case 3:
                if(TextUtils.isEmpty(already_deivce_name)){
                    textView.setText(getResources().getString(R.string.device_already_bind));
                }else{
                    String text = String.format(getResources().getString(R.string.device_already_bind_to), ControllerWifi.getInstance().deviceTid,already_deivce_name);
                    textView.setText(text);
                }
                break;
            case 111:
                textView.setText(getResources().getString(R.string.device_long_time_ap));
                break;
        }
    }

    class MyTask extends TimerTask{
        @Override
        public void run() {
            if(count>=200000){
               if(flag==1){
                   if(timer!=null){
                       timer.cancel();
                       timer = null;
                   }
                   handler.sendMessage(handler.obtainMessage(3));
               }else {
                   handler.sendMessage(handler.obtainMessage(2, flag));
               }
            }else{
                count = count+Now_speed;
                handler.sendMessage(handler.obtainMessage(1, count));
                if(flag<=0){
                    Now_speed = SPEED1;
                }else if(flag==1){
                    Now_speed = SPEED2;
                }else {
                    Now_speed = SPEED3;
                }
            }
        }
    }

    private IEsptouchListener myListener = new IEsptouchListener() {

        @Override
        public void onEsptouchResultAdded(final IEsptouchResult result) {
            Now_speed = SPEED2;
            gatewayConfigSuccess(result.getBssid());
            ConnectionPojo.getInstance().deviceTid = "GS193".equals(gatewaytype)?("RPMA_"+result.getBssid()):("ST_"+result.getBssid());
        }
    };


    @SuppressLint("StaticFieldLeak")
    private class EspTouchAsyncTask extends AsyncTask<String, Void, List<IEsptouchResult>> {

        private final Object mLock = new Object();

        @Override
        protected void onPreExecute() {
            synchronized (mLock) {
                if (mEsptouchTask != null) {
                    mEsptouchTask.interrupt();
                }
            }
        }

        @Override
        protected List<IEsptouchResult> doInBackground(String... params) {
            List<IEsptouchResult> resultList =null;
            try {
                int taskResultCount;
                synchronized (mLock) {
                    // !!!NOTICE
                    String apSsid = mWifiAdmin.getWifiConnectedSsidAscii(params[0]);
                    String apBssid = params[1];
                    String apPassword = params[2];
                    String isSsidHiddenStr = params[3];
                    String taskResultCountStr = params[4];
                    boolean isSsidHidden = false;
                    if ("YES".equals(isSsidHiddenStr)) {
                        isSsidHidden = true;
                    }
                    taskResultCount = Integer.parseInt(taskResultCountStr);
                    mEsptouchTask = new EsptouchTask(apSsid, apBssid, apPassword,
                            isSsidHidden, EsptouchAnimationActivity.this);
                    Log.i(TAG, "配网开始");

                    mEsptouchTask.setEsptouchListener(myListener);
                }
                resultList = mEsptouchTask.executeForResults(taskResultCount);
                IEsptouchResult ir = resultList.get(0);
                if (!ir.isCancelled()) {
                    ControllerWifi.getInstance().targetip = ir.getInetAddress();
                    if (ir.isSuc()) {
                        STEvent stEvent = new STEvent();
                        stEvent.setServiceevent(8);
                        EventBus.getDefault().post(stEvent);
                        try {
                            Thread.sleep(20000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        STEvent stEvent2 = new STEvent();
                        stEvent2.setServiceevent(5);
                        EventBus.getDefault().post(stEvent2);

//                        while (result_udpbind<0){
//
//                            try {
//                                Thread.sleep(1);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
//                        }
//
//                        if(result_udpbind >0){
//                            sendOtherData.timeCheck();
//                            try {
//                                Thread.sleep(1000);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
//                            sendOtherData.timeZoneCheck(DateUtil.getCurrentTimeZone());
//                        }
                    }
                }
                return resultList;
            }catch (Exception e){
                flag = 0;
                handler.sendMessage(handler.obtainMessage(2,flag));
                return resultList;
            }

        }

        @Override
        protected void onPostExecute(List<IEsptouchResult> result) {
            IEsptouchResult firstResult = result.get(0);
            if (!firstResult.isCancelled()) {
                if(firstResult.isSuc()){
//                    final ControllerWifi controllerWifi = ControllerWifi.getInstance();
//                    Log.i(TAG, "device tid="+controllerWifi.deviceTid+" +bind key="+controllerWifi.bind);
//                    if(result_udpbind==1){
//                        flag = 7;
//                        return;
//                    }else if(result_udpbind == 0){
//                        flag = 6;
//                        return;
//                    }
//                    handler.sendEmptyMessage(4);
                }else {
                    flag = 5;
                }
            }
        }
    }

    @Subscribe          //订阅事件FirstEvent
    public  void onEventMainThread(UdpConfigEvent event){
       int result_udpbind = event.getFlag_result();
        Log.i(TAG,"result_udpbind:"+result_udpbind);
       // if(isApConnect){
            if(result_udpbind==1){
                flag = 7;
                handler.sendEmptyMessage(2);
            }else if(result_udpbind == 0){
                flag = 6;
                handler.sendEmptyMessage(2);
            }else if(result_udpbind ==2){
                sendTimeInfo();
            }
       // }
    }

    private void bindSuccess(DeviceBean deviceBean){
        choosetoDeviceid = deviceBean.getDevTid();
        MyDeviceBean bean = new MyDeviceBean();
        bean.setChoice(1);
        bean.setDeviceName(deviceBean.getDeviceName());
        bean.setDevTid(deviceBean.getDevTid());
        bean.setBindKey(deviceBean.getBindKey());
        bean.setCtrlKey(deviceBean.getCtrlKey());
        bean.setOnline(deviceBean.isOnline());
        DcInfo info = new DcInfo();
        info.setConnectHost(deviceBean.getDcInfo().getConnectHost());
        bean.setDcInfo(info);
        bean.setProductPublicKey(deviceBean.getProductPublicKey());
        bean.setBinVersion(deviceBean.getBinVersion());
        bean.setBinType(deviceBean.getBinType());
        DeviceDAO DDO = new DeviceDAO(EsptouchAnimationActivity.this);
        DDO.deleteAllDeivceChoice();
        if(DDO.finddeviceCount(choosetoDeviceid)<1)
            DDO.addDevice(bean);


        SysModelBean sysModelBean1 = new SysModelBean();
        sysModelBean1.setChice("N");
        sysModelBean1.setSid("0");
        sysModelBean1.setModleName("在家");
        sysModelBean1.setDeviceid(deviceBean.getDevTid());
        sysModelBean1.setColor("F0");

        SysModelBean sysModelBean2 = new SysModelBean();
        sysModelBean2.setChice("N");
        sysModelBean2.setSid("1");
        sysModelBean2.setModleName("离家");
        sysModelBean2.setDeviceid(deviceBean.getDevTid());
        sysModelBean2.setColor("F1");

        SysModelBean sysModelBean3 = new SysModelBean();
        sysModelBean3.setChice("N");
        sysModelBean3.setSid("2");
        sysModelBean3.setModleName("睡眠");
        sysModelBean3.setDeviceid(deviceBean.getDevTid());
        sysModelBean3.setColor("F2");

        sysmodelDAO.addinit(sysModelBean1);
        sysmodelDAO.addinit(sysModelBean2);
        sysmodelDAO.addinit(sysModelBean3);

        SceneBean sceneBean = new SceneBean();
        sceneBean.setDeviceid(deviceBean.getDevTid());
        sceneBean.setSid("-1");
        sceneBean.setMid("129");
        sceneBean.setCode("");
        sceneBean.setName("");
        sceneDAO.addinit(sceneBean);

        SceneBean sceneBean2 = new SceneBean();
        sceneBean2.setDeviceid(deviceBean.getDevTid());
        sceneBean2.setSid("-1");
        sceneBean2.setMid("130");
        sceneBean2.setCode("");
        sceneBean2.setName("");
        sceneDAO.addinit(sceneBean2);

        SceneBean sceneBean3 = new SceneBean();
        sceneBean3.setDeviceid(deviceBean.getDevTid());
        sceneBean3.setSid("-1");
        sceneBean3.setMid("131");
        sceneBean3.setCode("");
        sceneBean3.setName("");
        sceneDAO.addinit(sceneBean3);
        flag = 1;
        if (isApConnect){
            gatewayConfigSuccess(ConnectionPojo.getInstance().deviceTid);
        }
    }

    private void bindFail(int errorCode){
        if(errorCode == 5400043){
            final ControllerWifi controllerWifi = ControllerWifi.getInstance();
            Log.i(TAG, "device tid2="+controllerWifi.deviceTid+" +bind key="+controllerWifi.bind);
            hekrUserAction.queryOwner(controllerWifi.deviceTid, controllerWifi.bind, new HekrUser.GetQueryOwnerListener() {
                @Override
                public void queryOwnerSuccess(String message) {
                    if(message.equals(CCPAppManager.getClientUser().getPhoneNumber())
                            ||  message.equals(CCPAppManager.getClientUser().getEmail())){
                        choosetoDeviceid = controllerWifi.deviceTid;
                        flag = 1;
                    }else{
                        already_deivce_name = message;
                        if(btn_retry.getVisibility()==View.VISIBLE){
                            String text = String.format(getResources().getString(R.string.device_already_bind_to),controllerWifi.deviceTid,already_deivce_name);
                            textView.setText(text);
                        }
                        Log.i(TAG,"已绑定其他设备");
                        flag = 3;
                    }
                }

                @Override
                public void queryOwnerFail(int errorCode) {
                    Log.i(TAG,"queryOwnerFail:errorCode:==="+errorCode);
                    if(errorCode != 1){
                        flag = 2;
                    }else {
                        flag = 8;
                        ControllerWifi.getInstance().choose_gateway =true;
                    }
                    failmsg = UnitTools.errorCode2Msg(EsptouchAnimationActivity.this,errorCode);
                }
            });
        }else if(errorCode == 1400022){
            flag = 2;
            failmsg = getResources().getString(R.string.restart_gateway);
        }
        else{
            flag = 2;
            failmsg = UnitTools.errorCode2Msg(EsptouchAnimationActivity.this,errorCode);
        }
    }

    private SpannableString getClickableSpan() {
        View.OnClickListener l = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EsptouchAnimationActivity.this, QuestionActivity.class));
            }
        };

        SpannableString spanableInfo = new SpannableString(
                getResources().getString(R.string.config_fail_reason));

        int end = spanableInfo.length();
        spanableInfo.setSpan(new Clickable(l), 0, end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spanableInfo;
    }

    /**
     * 内部类，用于截获点击富文本后的事件
     */
    class Clickable extends ClickableSpan implements View.OnClickListener {
        private final View.OnClickListener mListener;

        Clickable(View.OnClickListener mListener) {
            this.mListener = mListener;
        }

        @Override
        public void onClick(@NotNull View v) {
            mListener.onClick(v);
        }
        @Override
        public void updateDrawState(TextPaint ds) {
            ds.setColor(getResources().getColor(R.color.black));
            ds.setUnderlineText(true);    //去除超链接的下划线
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        count_of_bind = 0;
        ControllerWifi.getInstance().ap_config_ing = false;
        EventBus.getDefault().unregister(this);
        cancelTask();
    }

    private void cancelTask(){
        if(timer!=null){
            timer.cancel();
            timer = null;
        }
        if (mEsptouchTask != null) {
            mEsptouchTask.interrupt();
        }
        if(task3!=null){
            task3.cancel(true);
            task3 = null;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            STEvent stEvent2 = new STEvent();
            stEvent2.setServiceevent(10);
            EventBus.getDefault().post(stEvent2);
            return super.onKeyDown(keyCode, event);
        }
        return super.onKeyDown(keyCode, event);
    }
}
