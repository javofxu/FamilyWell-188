package me.hekr.sthome.equipment.detail;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.litesuits.android.log.Log;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import me.hekr.sdk.utils.CacheUtil;
import me.hekr.sthome.AddDeviceActivity;
import me.hekr.sthome.R;
import me.hekr.sthome.common.StatusBarUtil;
import me.hekr.sthome.common.TopbarSuperActivity;
import me.hekr.sthome.commonBaseView.ECAlertDialog;
import me.hekr.sthome.commonBaseView.ECListDialog;
import me.hekr.sthome.commonBaseView.MultiDirectionSlidingDrawer;
import me.hekr.sthome.commonBaseView.ProgressDialog;
import me.hekr.sthome.commonBaseView.PullListView;
import me.hekr.sthome.commonBaseView.RefreshableView2;
import me.hekr.sthome.commonBaseView.ResetLoadingView.ColorArcProgressBar;
import me.hekr.sthome.crc.CoderUtils;
import me.hekr.sthome.equipment.adapter.DataSwitchAdapter;
import me.hekr.sthome.event.DataSwitchRefreshEvent;
import me.hekr.sthome.event.LogoutEvent;
import me.hekr.sthome.event.STEvent;
import me.hekr.sthome.history.HistoryAdapter;
import me.hekr.sthome.http.HekrUser;
import me.hekr.sthome.http.HekrUserAction;
import me.hekr.sthome.model.modelbean.DataSwitchSubBean;
import me.hekr.sthome.model.modelbean.DataSwitchType;
import me.hekr.sthome.model.modelbean.EquipmentBean;
import me.hekr.sthome.model.modelbean.NoticeBean;
import me.hekr.sthome.model.modeldb.DataSwitchSubDAO;
import me.hekr.sthome.model.modeldb.EquipDAO;
import me.hekr.sthome.tools.ByteUtil;
import me.hekr.sthome.tools.ConnectionPojo;
import me.hekr.sthome.tools.EmojiFilter;
import me.hekr.sthome.tools.SendCommand;
import me.hekr.sthome.tools.SendEquipmentData;
import me.hekr.sthome.tools.SiterSDK;
import me.hekr.sthome.tools.SystemTintManager;
import me.hekr.sthome.tools.UnitTools;

/**
 * Created by jishu0001 on 2016/9/29.
 */
public class DataSwitchDetailActivity extends TopbarSuperActivity implements MultiDirectionSlidingDrawer.OnDrawerOpenListener,MultiDirectionSlidingDrawer.OnDrawerCloseListener{
    private static final String TAG = "ModeButtonDetail";
    private ImageView signal,quatity;
    private TextView showStatus;
    private EquipmentBean device;
    private EquipDAO ED;
    private TextView  battay_text;
    private ECAlertDialog alertDialog;
    private SendEquipmentData sd;
    private ListView listView_sys;
    private DataSwitchAdapter dataSwitchAdapter;
    private MultiDirectionSlidingDrawer drawer;
    private TextView textView_log;
    private ImageView imageView_line;
    private PullListView listView;
    private ImageView imageView_cancel;
    private ImageButton imageButton_clear;
    private List<NoticeBean> noticeBeanList;
    private List<NoticeBean> noticeBeanFilterList;
    private List<DataSwitchSubBean> list;
    private ProgressDialog progressDialog;
    private HistoryAdapter historyAdapter;
    private int page;
    private View empty;
    private View empty_data;
    private SystemTintManager tintManager;
    private DataSwitchSubDAO dataSwitchSubDAO;
    private RefreshableView2 refreshableView;
    private Animation loadAnimation;
    private ColorArcProgressBar colorArcProgressBar;
    private TextView textView_count;
    private int count_s;
    private boolean flag_set = false;
    private Timer timer_set;
    private UpdateCommandTask timertask;
    private String newname;

    @Override
    protected void onCreateInit() {
        initData();
        initViewGuider();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_detail_data_switch;
    }


    private void initData() {
        dataSwitchSubDAO = new DataSwitchSubDAO(this);
        EventBus.getDefault().register(this);
        sd = new SendEquipmentData(this) {
            @Override
            protected void sendEquipmentDataFailed() {

            }

            @Override
            protected void sendEquipmentDataSuccess() {

            }
        };
        try{
            device = (EquipmentBean) this.getIntent().getSerializableExtra("device");
        }catch(Exception e){
            Log.i("Detail socket","device is null");
        }
    }

    private void initViewGuider() {
        loadAnimation = AnimationUtils.loadAnimation(this, R.anim.loading_reset);
        timer_set = new Timer();
        timertask = new UpdateCommandTask();
        timer_set.schedule(timertask,0,1000);
        refreshableView = (RefreshableView2)findViewById(R.id.refresh);
        listView_sys = (ListView)findViewById(R.id.modelist);
        battay_text = (TextView)findViewById(R.id.quantitydesc);
        showStatus = (TextView) findViewById(R.id.showStatus);
        signal = (ImageView) findViewById(R.id.signalPosition);
        quatity = (ImageView) findViewById(R.id.quantityPosition);

        list = dataSwitchSubDAO.findAllSubDevice(device.getEqid(),device.getDeviceid());
        dataSwitchAdapter = new DataSwitchAdapter(this,list);
        listView_sys.setAdapter(dataSwitchAdapter);
        empty_data = findViewById(R.id.empty_data_switch);
        listView_sys.setEmptyView(empty_data);

        textView_log = (TextView)findViewById(R.id.title2);
        imageView_line = (ImageView)findViewById(R.id.log_line);
        textView_log.setVisibility(View.GONE);
        imageView_line.setVisibility(View.GONE);
        drawer =(MultiDirectionSlidingDrawer)findViewById(R.id.drawer1);
        drawer.setOnDrawerOpenListener(this);
        drawer.setOnDrawerCloseListener(this);
        listView = (PullListView) drawer.findViewById(R.id.logs);
        listView.setPullLoadEnable(false);
        imageView_cancel = (ImageView)findViewById(R.id.cancel);
        imageView_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawer.close();
            }
        });
        imageButton_clear = (ImageButton)findViewById(R.id.clear);
        imageButton_clear.setVisibility(View.GONE);
        noticeBeanList = new ArrayList<NoticeBean>();
        noticeBeanFilterList = new ArrayList<NoticeBean>();
        historyAdapter = new HistoryAdapter(this,noticeBeanFilterList);
        listView.setAdapter(historyAdapter);
        empty = findViewById(R.id.empty);
        listView.setEmptyView(empty);
        tintManager = new SystemTintManager(this);// 创建状态栏的管理实例

        doStatusShow(device.getState());
        showBattery();
        refreshableView.setOnRefreshListener(new RefreshableView2.PullToRefreshListener() {
            @Override
            public void onRefresh() {
                handler.sendEmptyMessage(4);
                sd.sendDataSwitchSync(device.getEqid());
                handler.sendMessageDelayed(handler.obtainMessage(2),2000);
            }
        },3);
        new Thread(){
            @Override
            public void run() {
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                handler.sendMessage(handler.obtainMessage(1));
            }
        }.start();
        listView_sys.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view,final int i, long l) {
                alertDialog = ECAlertDialog.buildAlert(DataSwitchDetailActivity.this, R.string.data_switch_test, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int j) {
                           DataSwitchSubBean dataSwitchSubBean = list.get(i);
                           int address = dataSwitchSubBean.getSubid();
                           String ds = Integer.toHexString(address);
                           int lengh = ds.length();
                           for(int m=0;m<5-lengh;m++){
                               ds = "0"+ds;
                           }
                           SendCommand.Command = SendCommand.EQUIPMENT_CONTROL;
                           String d = "3";
                            if(DataSwitchType.getType(dataSwitchSubBean.getStatus())==DataSwitchType.STATUS_FIRE_ALARMER_WARN
                               ||DataSwitchType.getType(dataSwitchSubBean.getStatus())==DataSwitchType.STATUS_FIRE_ALARMER_NORMAL
                                    ||DataSwitchType.getType(dataSwitchSubBean.getStatus())==DataSwitchType.STATUS_FIRE_ALARMER_LOW_VOLTAGE){
                                d = "3";
                            }else if(DataSwitchType.getType(dataSwitchSubBean.getStatus())==DataSwitchType.STATUS_GAS_ALARMER_WARN
                                    ||DataSwitchType.getType(dataSwitchSubBean.getStatus())==DataSwitchType.STATUS_GAS_ALARMER_NORMAL){
                                d = "6";
                            }else if(DataSwitchType.getType(dataSwitchSubBean.getStatus())==DataSwitchType.STATUS_WATER_ALARMER_WARN
                                    ||DataSwitchType.getType(dataSwitchSubBean.getStatus())==DataSwitchType.STATUS_WATER_ALARMER_NORMAL){
                                d = "B";
                            }

                           sd.sendEquipmentCommand(device.getEqid(),   ds+d+"00");
                           startToCountdown();
                    }
                });
                alertDialog.show();
            }
        });

        String name = "";
        if(TextUtils.isEmpty(device.getEquipmentName())){
            name = getResources().getString(R.string.data_switch)+device.getEqid();
        }else{
            name = device.getEquipmentName();
        }

        getTopBarView().setTopBarStatus(1, 2, name, getResources().getString(R.string.manage), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(drawer!=null && drawer.isOpened()){
                    drawer.animateClose();
                }else {
                    finish();
                }
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ECListDialog ecListDialog = new ECListDialog(DataSwitchDetailActivity.this,getResources().getStringArray(R.array.DeivceOperation));
                ecListDialog.setTitle(getResources().getString(R.string.manage));
                ecListDialog.setOnDialogItemClickListener(new ECListDialog.OnDialogItemClickListener() {
                    @Override
                    public void onDialogItemClick(Dialog d, int position) {

                        switch (position){
                            case 0:
                                SendCommand.Command = SendCommand.REPLACE_EQUIPMENT;
                                sd.replaceEquipment(device.getEqid());
                                Intent intent =new Intent(DataSwitchDetailActivity.this,AddDeviceActivity.class);
                                Bundle bundle = new Bundle();
                                bundle.putString("eqid",device.getEqid());
                                intent.putExtras(bundle);
                                startActivity(intent);
                                finish();
                                break;
                            case 1:
                                ECAlertDialog elc = ECAlertDialog.buildAlert(DataSwitchDetailActivity.this,getResources().getString(R.string.delete_or_not), getResources().getString(R.string.cancel), getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                }, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        SendCommand.Command = SendCommand.DELETE_EQUIPMENT_DETAIL;
                                        sd.deleteEquipment(device.getEqid());
                                    }
                                });
                                elc.show();
                                break;
                            case 2:
                                alertDialog = ECAlertDialog.buildAlert(DataSwitchDetailActivity.this, getResources().getString(R.string.update_name),getResources().getString(R.string.cancel),getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        alertDialog.setDismissFalse(true);
                                    }
                                }, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        EditText text = (EditText) alertDialog.getContent().findViewById(R.id.tet);
                                        newname = text.getText().toString().trim();

                                        if(!TextUtils.isEmpty(newname)){

                                            try {
                                                String encode = CacheUtil.getString(SiterSDK.SETTINGS_CONFIG_ENCODE,"GBK");
                                                if(newname.getBytes(encode).length<=15){
                                                    if(!EmojiFilter.containsEmoji(newname)) {
                                                        alertDialog.setDismissFalse(true);
                                                        getTopBarView().setTextTitle(newname);
                                                        String ds = CoderUtils.getAscii(newname);
                                                        String dsCRC = ByteUtil.CRCmaker(ds);
                                                        SendCommand.Command = SendCommand.MODIFY_EQUIPMENT_NAME;
                                                        sd.modifyEquipmentName(device.getEqid(), ds + dsCRC);
                                                    }else {
                                                        alertDialog.setDismissFalse(false);
                                                        Toast.makeText(DataSwitchDetailActivity.this,getResources().getString(R.string.name_contain_emoji),Toast.LENGTH_SHORT).show();
                                                    }
                                                }else{
                                                    alertDialog.setDismissFalse(false);
                                                    Toast.makeText(DataSwitchDetailActivity.this,getResources().getString(R.string.name_is_too_long),Toast.LENGTH_SHORT).show();
                                                }
                                            } catch (UnsupportedEncodingException e) {
                                                e.printStackTrace();
                                            }

                                        }
                                        else{
                                            alertDialog.setDismissFalse(false);
                                            Toast.makeText(DataSwitchDetailActivity.this,getResources().getString(R.string.name_is_null),Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                });
                                alertDialog.setContentView(R.layout.edit_alert);
                                alertDialog.setTitle(getResources().getString(R.string.update_name));
                                EditText text = (EditText) alertDialog.getContent().findViewById(R.id.tet);
                                text.setText(device.getEquipmentName());
                                text.setSelection(device.getEquipmentName().length());

                                alertDialog.show();

                                break;
                            default:
                                break;
                        }

                    }
                });
                ecListDialog.show();
            }
        });
    }

    private void updateName(String edit) {
        if( !device.getEquipmentName().equals(edit)){

            device.setEquipmentName(edit);
            ED = new EquipDAO(this);
            try {
                ED.updateName(device);
            }catch (Exception e){
                Toast.makeText(DataSwitchDetailActivity.this,getResources().getString(R.string.name_is_repeat),Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void doStatusShow(String aaaa) {
        try {
            String signal1 = aaaa.substring(0,2);
            String quantity1 = aaaa.substring(2,4);
            String draw = aaaa.substring(9,10);
            int statusa = Integer.parseInt(draw,16);
            int qqqq = Integer.parseInt(quantity1,16);
            quatity.setImageResource(ShowBascInfor.choseQPic(qqqq));
            battay_text.setText(ShowBascInfor.choseLNum(qqqq));
            if(signal1 != null){
                signal.setImageResource(ShowBascInfor.choseSPic(signal1));
            }
             if(statusa == 0){
                if( qqqq <= 15 ){
                    showStatus.setText(getResources().getString(R.string.low_battery));
                }else{
                    showStatus.setText(getResources().getString(R.string.normal));
                }

            }else if(statusa == 15){
                showStatus.setText(getResources().getString(R.string.offline));
            }else{

                if( DataSwitchType.getType(statusa) == DataSwitchType.STATUS_FIRE_ALARMER_WARN){
                    showStatus.setText(getResources().getString(R.string.alarm));
                }else if( DataSwitchType.getType(statusa) == DataSwitchType.STATUS_FIRE_ALARMER_LOW_VOLTAGE){
                     showStatus.setText(getResources().getString(R.string.low_battery));
                 }else if( DataSwitchType.getType(statusa) == DataSwitchType.STATUS_FIRE_ALARMER_NORMAL){
                    showStatus.setText(getResources().getString(R.string.normal));
                }else if( DataSwitchType.getType(statusa) == DataSwitchType.STATUS_GAS_ALARMER_WARN){
                    showStatus.setText(getResources().getString(R.string.alarm));
                }else if( DataSwitchType.getType(statusa) == DataSwitchType.STATUS_GAS_ALARMER_NORMAL){
                    showStatus.setText(getResources().getString(R.string.normal));
                }else if( DataSwitchType.getType(statusa) == DataSwitchType.STATUS_WATER_ALARMER_WARN){
                    showStatus.setText(getResources().getString(R.string.alarm));
                }else if( DataSwitchType.getType(statusa) == DataSwitchType.STATUS_WATER_ALARMER_NORMAL){
                    showStatus.setText(getResources().getString(R.string.normal));
                }else if( DataSwitchType.getType(statusa) == DataSwitchType.STATUS_DEVICE_DELETE){
                    showStatus.setText(getResources().getString(R.string.normal));
                }else {
                    showStatus.setText(getResources().getString(R.string.offline));
                }

            }
        }catch (Exception e){
            showStatus.setText(getResources().getString(R.string.offline));
            quatity.setImageResource(ShowBascInfor.choseQPic(100));
            battay_text.setText(ShowBascInfor.choseLNum(100));
        }


    }

    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        if(timer_set!=null){
            timer_set.cancel();
            timer_set = null;
            timertask = null;
        }

    }


    @Subscribe          //订阅事件FirstEvent
    public  void onEventMainThread(STEvent event){
        if(event.getEvent() == SendCommand.MODIFY_EQUIPMENT_NAME){
            SendCommand.clearCommnad();
            updateName(newname);
            Toast.makeText(DataSwitchDetailActivity.this,getResources().getString(R.string.update_name_success),Toast.LENGTH_SHORT).show();
        }else if(event.getEvent() == SendCommand.DELETE_EQUIPMENT_DETAIL){
            SendCommand.clearCommnad();
            Toast.makeText(DataSwitchDetailActivity.this,getResources().getString(R.string.delete_success),Toast.LENGTH_SHORT).show();
            ED = new EquipDAO(DataSwitchDetailActivity.this);
            ED.deleteByEqid(device);
            dataSwitchSubDAO.deleteSubdevice(device.getEqid(),device.getDeviceid());
            finish();
        }else if(event.getEvent()== SendCommand.EQUIPMENT_CONTROL){
            SendCommand.clearCommnad();
            Toast.makeText(DataSwitchDetailActivity.this,getResources().getString(R.string.success_test),Toast.LENGTH_SHORT).show();
        }

        if(event.getRefreshevent()==5){
            String new_status = event.getEq_status();
            String new_eqid = event.getEq_id();
            String new_eqtype = event.getEq_type();
            String new_deviceid = event.getCurrent_deviceid();

            if(new_deviceid.equals(device.getDeviceid())&&new_eqid.equals(device.getEqid())&&new_eqtype.equals(device.getEquipmentDesc())){
                device.setState(new_status);
                doStatusShow(new_status);

                if(new_status.length()>=10){
                    String draw = new_status.substring(9,10);
                    int statusa = Integer.parseInt(draw,16);

                    if((DataSwitchType.getType(statusa)!=DataSwitchType.STATUS_UNKNOWN
                      &&statusa!=15)||statusa==0){
                        list = dataSwitchSubDAO.findAllSubDevice(device.getEqid(),device.getDeviceid());
                        dataSwitchAdapter.refresh(list);
                    }
                }

            }
        }
    }

    @Subscribe          //订阅事件FirstEvent
    public  void onEventMainThread(DataSwitchRefreshEvent event){
        if(event.getDeviceid().equals(ConnectionPojo.getInstance().deviceTid)
                && event.getEqid().equals(device.getEqid())){
            list = dataSwitchSubDAO.findAllSubDevice(device.getEqid(),device.getDeviceid());
            dataSwitchAdapter.refresh(list);
        }
    }

    private void showBattery(){
        try {
            if("1".equals(device.getEquipmentDesc().substring(0,1))){
                battay_text.setVisibility(View.GONE);
                quatity.setVisibility(View.GONE);
            }else{
                battay_text.setVisibility(View.VISIBLE);
                quatity.setVisibility(View.VISIBLE);
            }
        }catch (Exception e){
            Log.i(TAG,"data err");
        }
    }


    @Override
    public void onDrawerOpened() {
        getHistory();
        StatusBarUtil.setStatusBarDarkTheme(this,true);
    }

    @Override
    public void onDrawerClosed() {
        page = 0;
        StatusBarUtil.setStatusBarDarkTheme(this, false);
        doStatusShow(device.getState());
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            if(drawer.isOpened()){
                drawer.animateClose();
                return true;
            }else {
                return super.onKeyDown(keyCode, event);
            }
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    private void getHistory(){
        if(page == 0){
            progressDialog =  new ProgressDialog(this);
            progressDialog.setPressText(getResources().getText(R.string.wait));
            progressDialog.show();
        }

        HekrUserAction.getInstance(this).getAlarmHistoryList(this, ConnectionPojo.getInstance().deviceTid, ConnectionPojo.getInstance().ctrlKey, ConnectionPojo.getInstance().propubkey, page, new HekrUser.GetDeviceHistoryListener() {
            @Override
            public void getSuccess(List<NoticeBean> list, int pagenumber, boolean last) {
                if(pagenumber == 0){
                    noticeBeanList.clear();
                }
                noticeBeanList.addAll(list);
                if(pagenumber<10){
                    if(!last&&list.size()==20){
                        //防止以前的告警数据造成解析失效
                        page = pagenumber + 1;
                        getHistory();
                    }else {
                        if(progressDialog!=null && progressDialog.isShowing()){
                            progressDialog.dismiss();
                            progressDialog = null;
                        }
                        page = 0;
                        filterNotice();

                    }
                }else {
                    if(progressDialog!=null && progressDialog.isShowing()){
                        progressDialog.dismiss();
                        progressDialog = null;
                    }
                    page = 0;
                    filterNotice();
                }
            }

            @Override
            public void getFail(int errorCode) {
                if(progressDialog!=null && progressDialog.isShowing()){
                    progressDialog.dismiss();
                    progressDialog = null;
                }
                filterNotice();
                if(errorCode==1){
                    LogoutEvent tokenTimeoutEvent = new LogoutEvent();
                    EventBus.getDefault().post(tokenTimeoutEvent);
                }else{

                    Toast.makeText(DataSwitchDetailActivity.this, UnitTools.errorCode2Msg(DataSwitchDetailActivity.this,errorCode),Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void filterNotice(){
        noticeBeanFilterList.clear();
        for(NoticeBean noticeBean:noticeBeanList){
            if(!TextUtils.isEmpty( noticeBean.getEqid())&&noticeBean.getEqid().equals(device.getEqid())&&!TextUtils.isEmpty(noticeBean.getEquipmenttype())&&noticeBean.getEquipmenttype().equals(device.getEquipmentDesc())){
                noticeBeanFilterList.add(noticeBean);
            }
        }
        historyAdapter.refreshList(noticeBeanFilterList);
    }

    private void startToCountdown(){
        alertDialog = ECAlertDialog.buildAlert(this, "",getResources().getString(R.string.cancel),getResources().getString(R.string.ok), null, null);
        alertDialog.setContentView(R.layout.layout_resetloading);
        colorArcProgressBar = alertDialog.getContent().findViewById(R.id.loading);
        textView_count = alertDialog.getContent().findViewById(R.id.prodcess);
        alertDialog.setButtonHidden();
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
        colorArcProgressBar.startAnimation(loadAnimation);
        flag_set = true;
        count_s = 0;
        textView_count.setText(""+(60-count_s));
        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                flag_set = false;
            }
        });
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    refreshableView.setRrefresh();
                    break;
                case 2:
                    refreshableView.finishRefreshing();
                    break;
                case 4:
                    dataSwitchSubDAO.deleteSubdevice(device.getEqid(),device.getDeviceid());
                    list = dataSwitchSubDAO.findAllSubDevice(device.getEqid(),device.getDeviceid());
                    dataSwitchAdapter.refresh(list);
                    break;
                case 11:
                    textView_count.setText(""+(60-count_s));
                    break;
                case 12:
                    if(alertDialog!=null && alertDialog.isShowing()){
                        alertDialog.dismiss();
                        alertDialog = null;
                    }
            }
        }
    };

    class UpdateCommandTask extends TimerTask {
        @Override
        public void run() {


            if(flag_set) {
                android.util.Log.i(TAG,"设置命令超时计数:"+count_s);
                count_s ++;
                handler.sendEmptyMessage(11);
            }

            if(count_s >= 60){
                count_s = 0;

                handler.sendEmptyMessage(12);
            }

        }
    }
}
