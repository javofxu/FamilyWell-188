package me.hekr.sthome.equipment.detail;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.litesuits.android.log.Log;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import me.hekr.sdk.utils.CacheUtil;
import me.hekr.sthome.AddDeviceActivity;
import me.hekr.sthome.R;
import me.hekr.sthome.common.CCPAppManager;
import me.hekr.sthome.common.StatusBarUtil;
import me.hekr.sthome.commonBaseView.ECAlertDialog;
import me.hekr.sthome.commonBaseView.ECListDialog;
import me.hekr.sthome.commonBaseView.MultiDirectionSlidingDrawer;
import me.hekr.sthome.commonBaseView.ProgressDialog;
import me.hekr.sthome.commonBaseView.PullListView;
import me.hekr.sthome.crc.CoderUtils;
import me.hekr.sthome.event.LogoutEvent;
import me.hekr.sthome.event.STEvent;
import me.hekr.sthome.history.HistoryAdapter;
import me.hekr.sthome.http.HekrUser;
import me.hekr.sthome.http.HekrUserAction;
import me.hekr.sthome.model.modeladapter.OptionAdapter;
import me.hekr.sthome.model.modelbean.EquipmentBean;
import me.hekr.sthome.model.modelbean.NoticeBean;
import me.hekr.sthome.model.modeldb.EquipDAO;
import me.hekr.sthome.tools.ByteUtil;
import me.hekr.sthome.tools.ConnectionPojo;
import me.hekr.sthome.tools.ECPreferenceSettings;
import me.hekr.sthome.tools.ECPreferences;
import me.hekr.sthome.tools.EmojiFilter;
import me.hekr.sthome.tools.SendCommand;
import me.hekr.sthome.tools.SendEquipmentData;
import me.hekr.sthome.tools.SiterSDK;
import me.hekr.sthome.tools.SystemTintManager;
import me.hekr.sthome.tools.UnitTools;
import me.hekr.sthome.wheelwidget.view.WheelView;

/**
 * Created by jishu0001 on 2016/10/9.
 */
public class WaterDetailActivity extends AppCompatActivity implements MultiDirectionSlidingDrawer.OnDrawerOpenListener,MultiDirectionSlidingDrawer.OnDrawerCloseListener{
    private static final String TAG = "WaterDetail";
    private ImageView signal,quatity,deviceLogo;
    private TextView operation,emergencyCall,showStatus;
    private EquipmentBean device;
    private EquipDAO ED;
    private ImageView back_img;
    private TextView  edt_txt,eq_name,battay_text;
    private RelativeLayout root;
    private ECAlertDialog alertDialog,alertDialog2;
    private SendEquipmentData sd;
    private MultiDirectionSlidingDrawer drawer;
    private TextView textView_log;
    private ImageView imageView_line;
    private PullListView listView;
    private ImageView imageView_cancel;
    private ImageButton imageButton_clear;
    private List<NoticeBean> noticeBeanList;
    private List<NoticeBean> noticeBeanFilterList;
    private ProgressDialog progressDialog;
    private HistoryAdapter historyAdapter;
    private int page;
    private View empty;
    private SystemTintManager tintManager;
    private ECListDialog ecListDialog;
    private ArrayList<String> itemslist = new ArrayList<String>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_smalarm);
        initData();
        initViewGuider();
    }



    private void initData() {
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
            Log.i(TAG,"device is null");
        }
        for(int i=0;i<256;i++){
            itemslist.add(String.valueOf(i*10)+getResources().getString(R.string.device_setup_record_second));
        }
    }

    private void initViewGuider() {
        battay_text = (TextView)findViewById(R.id.quantitydesc);
        back_img   = (ImageView)findViewById(R.id.goBack);
        back_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        edt_txt    = (TextView)findViewById(R.id.detailEdit);
        edt_txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ECListDialog ecListDialog = new ECListDialog(WaterDetailActivity.this,getResources().getStringArray(R.array.DeivceOperation));
                ecListDialog.setTitle(getResources().getString(R.string.manage));
                ecListDialog.setOnDialogItemClickListener(new ECListDialog.OnDialogItemClickListener() {
                    @Override
                    public void onDialogItemClick(Dialog d, int position) {

                        switch (position){
                            case 0:
                                SendCommand.Command = SendCommand.REPLACE_EQUIPMENT;
                                sd.replaceEquipment(device.getEqid());
                                Intent intent =new Intent(WaterDetailActivity.this,AddDeviceActivity.class);
                                Bundle bundle = new Bundle();
                                bundle.putString("eqid",device.getEqid());
                                intent.putExtras(bundle);
                                startActivity(intent);
                                finish();
                                break;
                            case 1:
                                ECAlertDialog elc = ECAlertDialog.buildAlert(WaterDetailActivity.this,getResources().getString(R.string.delete_or_not), getResources().getString(R.string.cancel), getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
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
                                alertDialog = ECAlertDialog.buildAlert(WaterDetailActivity.this, getResources().getString(R.string.update_name),getResources().getString(R.string.cancel),getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        alertDialog.setDismissFalse(true);
                                    }
                                }, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        EditText text = (EditText) alertDialog.getContent().findViewById(R.id.tet);
                                        String newname = text.getText().toString().trim();

                                        if(!TextUtils.isEmpty(newname)){

                                            try {
                                                String encode = CacheUtil.getString(SiterSDK.SETTINGS_CONFIG_ENCODE,"GBK");
                                                if(newname.getBytes(encode).length<=15){
                                                    if(!EmojiFilter.containsEmoji(newname)) {
                                                        alertDialog.setDismissFalse(true);
                                                        eq_name.setText(newname);
                                                        updateName(newname);
                                                        String ds = CoderUtils.getAscii(newname);
                                                        String dsCRC = ByteUtil.CRCmaker(ds);
                                                        SendCommand.Command = SendCommand.MODIFY_EQUIPMENT_NAME;
                                                        sd.modifyEquipmentName(device.getEqid(), ds + dsCRC);
                                                    }else {
                                                        alertDialog.setDismissFalse(false);
                                                        Toast.makeText(WaterDetailActivity.this,getResources().getString(R.string.name_contain_emoji),Toast.LENGTH_SHORT).show();
                                                    }
                                                }else{
                                                    alertDialog.setDismissFalse(false);
                                                    Toast.makeText(WaterDetailActivity.this,getResources().getString(R.string.name_is_too_long),Toast.LENGTH_SHORT).show();
                                                }
                                            } catch (UnsupportedEncodingException e) {
                                                e.printStackTrace();
                                            }

                                        }
                                        else{
                                            alertDialog.setDismissFalse(false);
                                            Toast.makeText(WaterDetailActivity.this,getResources().getString(R.string.name_is_null),Toast.LENGTH_SHORT).show();
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
        root = (RelativeLayout)findViewById(R.id.root);
        //沉浸式设置支持API19
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int top = UnitTools.getStatusBarHeight(this);
            root.setPadding(0,top,0,0);
        }
        showStatus = (TextView) findViewById(R.id.showStatus);
        signal = (ImageView) findViewById(R.id.signalPosition);
        quatity = (ImageView) findViewById(R.id.quantityPosition);
        deviceLogo = (ImageView) findViewById(R.id.devicePosition);
        deviceLogo.setImageResource(R.drawable.detail5);
        emergencyCall = (TextView) findViewById(R.id.emergencyCall);
        emergencyCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPhoneAlert();
            }
        });
        eq_name = (TextView)findViewById(R.id.eq_name);
        eq_name.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        eq_name.setSelected(true);
        eq_name.setFocusable(true);
        eq_name.setFocusableInTouchMode(true);
        eq_name.setMarqueeRepeatLimit(-1);
        if(TextUtils.isEmpty(device.getEquipmentName())){
            eq_name.setText(getResources().getString(R.string.wt)+device.getEqid());
        }else{
            eq_name.setText(device.getEquipmentName());
        }
        operation = (TextView) findViewById(R.id.operation);
        try {
            int ds = Integer.parseInt(device.getEquipmentDesc().substring(device.getEquipmentDesc().length()-2),16);
            if(ds<=7||ds>=14){
                operation.setVisibility(View.VISIBLE);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        operation.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                int ds = Integer.parseInt(device.getEquipmentDesc().substring(device.getEquipmentDesc().length()-2),16);
                if(isDebugMode()&&ds>=14){
                    openTestModeAlert();
                }else {
                    SendCommand.Command = SendCommand.EQUIPMENT_CONTROL;
                    sd.sendEquipmentCommand(device.getEqid(),"BB000000");
                }
            }
        });
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
    }

    private void updateName(String edit) {
        if( !device.getEquipmentName().equals(edit)){

            device.setEquipmentName(edit);
            ED = new EquipDAO(this);
            try {
                ED.updateName(device);
            }catch (Exception e){
                Toast.makeText(WaterDetailActivity.this,getResources().getString(R.string.name_is_repeat),Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void doStatusShow(String aaaa) {
     try {
         String signal1 = aaaa.substring(0,2);
         String quantity1 = aaaa.substring(2,4);
         String draw = aaaa.substring(4,6);

         int qqqq = Integer.parseInt(quantity1,16);

         quatity.setImageResource(ShowBascInfor.choseQPic(qqqq));
         battay_text.setText(ShowBascInfor.choseLNum(qqqq));

         if(signal1 != null){
             signal.setImageResource(ShowBascInfor.choseSPic(signal1));
         }
         if("11".equals(draw)){
             showStatus.setText(getResources().getString(R.string.Illegal_demolition));
             root.setBackgroundColor(getResources().getColor(R.color.device_error));
             showStatus.setTextColor(getResources().getColor(R.color.device_error));
         }else if("55".equals(draw)){
             root.setBackgroundColor(getResources().getColor(R.color.device_error));
             showStatus.setTextColor(getResources().getColor(R.color.device_error));
             showStatus.setText(getResources().getString(R.string.alarm));
         }else if("AA".equals(draw)){

             if( qqqq <= 15 ){
                 root.setBackgroundColor(getResources().getColor(R.color.device_warn));
                 showStatus.setTextColor(getResources().getColor(R.color.device_warn));
                 showStatus.setText(getResources().getString(R.string.low_battery));
             }else{
                 root.setBackgroundColor(getResources().getColor(R.color.device_normal));
                 showStatus.setTextColor(getResources().getColor(R.color.device_normal));
                 showStatus.setText(getResources().getString(R.string.normal));
             }

         }else if("BB".equals(draw)){
             root.setBackgroundColor(getResources().getColor(R.color.device_error));
             showStatus.setTextColor(getResources().getColor(R.color.device_error));
             showStatus.setText(getResources().getString(R.string.test));
         }else if("50".equals(draw)){
             root.setBackgroundColor(getResources().getColor(R.color.device_normal));
             showStatus.setTextColor(getResources().getColor(R.color.device_normal));
             showStatus.setText(getResources().getString(R.string.silence));
         }else {
             root.setBackgroundColor(getResources().getColor(R.color.device_offine));
             showStatus.setTextColor(getResources().getColor(R.color.device_offine));
             showStatus.setText(getResources().getString(R.string.offline));
         }
     }catch (Exception e){
         root.setBackgroundColor(getResources().getColor(R.color.device_offine));
         showStatus.setTextColor(getResources().getColor(R.color.device_offine));
         showStatus.setText(getResources().getString(R.string.offline));
         quatity.setImageResource(ShowBascInfor.choseQPic(100));
         battay_text.setText(ShowBascInfor.choseLNum(100));
     }


    }
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe          //订阅事件FirstEvent
    public  void onEventMainThread(STEvent event){
        if(event.getEvent() == SendCommand.MODIFY_EQUIPMENT_NAME){
            SendCommand.clearCommnad();
            Toast.makeText(WaterDetailActivity.this,getResources().getString(R.string.update_name_success),Toast.LENGTH_SHORT).show();
        }else if(event.getEvent() == SendCommand.DELETE_EQUIPMENT_DETAIL){
            SendCommand.clearCommnad();
            Toast.makeText(WaterDetailActivity.this,getResources().getString(R.string.delete_success),Toast.LENGTH_SHORT).show();
            ED = new EquipDAO(WaterDetailActivity.this);
            ED.deleteByEqid(device);
            finish();
        }else if(event.getEvent()== SendCommand.EQUIPMENT_CONTROL){
            SendCommand.clearCommnad();
            Toast.makeText(WaterDetailActivity.this,getResources().getString(R.string.success_test),Toast.LENGTH_SHORT).show();
        }

        if(event.getRefreshevent()==5){
            String new_status = event.getEq_status();
            String new_eqid = event.getEq_id();
            String new_eqtype = event.getEq_type();
            String new_deviceid = event.getCurrent_deviceid();

            if(new_deviceid.equals(device.getDeviceid())&&new_eqid.equals(device.getEqid())&&new_eqtype.equals(device.getEquipmentDesc())){
                device.setState(new_status);
                doStatusShow(new_status);
            }
        }
    }


    private void openPhoneAlert(){

        final  String phone = CCPAppManager.getClientUser().getDescription();

        if(!TextUtils.isEmpty(phone)){
            ECAlertDialog alertDialog = ECAlertDialog.buildAlert(this, phone, getResources().getString(R.string.cancel), getResources().getString(R.string.call), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            }, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + phone));
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    }

                }
            });
            alertDialog.setTitleVisibility(View.GONE);
            alertDialog.show();
        }else {
            Toast.makeText(this,getResources().getString(R.string.please_set_emergencyphone),Toast.LENGTH_SHORT).show();
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
        root.setBackgroundColor(getResources().getColor(R.color.white));
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

                    Toast.makeText(WaterDetailActivity.this, UnitTools.errorCode2Msg(WaterDetailActivity.this,errorCode),Toast.LENGTH_LONG).show();
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

    private void openTestModeAlert(){


        ecListDialog = new ECListDialog(this,getResources().getStringArray(R.array.testMode));
        ecListDialog.setOnDialogItemClickListener(new ECListDialog.OnDialogItemClickListener() {
            @Override
            public void onDialogItemClick(Dialog d, int position) {

                switch (position){
                    case 0:
                        SendCommand.Command = SendCommand.EQUIPMENT_CONTROL;
                        sd.sendEquipmentCommand(device.getEqid(),"BB000000");
                        break;
                    case 1:
                        openSelectSecond();
                        break;
                    default:
                        break;
                }

            }
        });
        ecListDialog.show();



    }

    private void openSelectSecond(){
        alertDialog2 = ECAlertDialog.buildAlert(WaterDetailActivity.this, getResources().getString(R.string.please_choose),getResources().getString(R.string.cancel),getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        }, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                WheelView text = (WheelView) alertDialog2.getContent().findViewById(R.id.tet);
                int sa  = text.getCurrentItem();
                String minute = Integer.toHexString(sa);

                for(int i=0;i<2-minute.length();i++){
                    minute = "0"+minute;
                }

                SendCommand.Command = SendCommand.EQUIPMENT_CONTROL;
                sd.sendEquipmentCommand("0","55BB"+minute.toUpperCase()+"FF");
            }
        });
        alertDialog2.setContentView(R.layout.edit_number_selected);
        alertDialog2.setTitle(getResources().getString(R.string.please_choose_alert_time));
        WheelView text = (WheelView) alertDialog2.getContent().findViewById(R.id.tet);
        text.setAdapter(new OptionAdapter(itemslist,30));
        alertDialog2.show();
    }

    private boolean isDebugMode(){

        SharedPreferences sharedPreferences = ECPreferences.getSharedPreferences();
        ECPreferenceSettings flag = ECPreferenceSettings.SETTINGS_DEBUG;
        boolean autoflag = sharedPreferences.getBoolean(flag.getId(), (boolean) flag.getDefaultValue());
        return autoflag;
    }
}
