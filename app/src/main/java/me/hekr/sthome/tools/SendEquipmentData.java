package me.hekr.sthome.tools;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.xiaomi.mipush.sdk.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import me.hekr.sdk.Hekr;
import me.hekr.sdk.inter.HekrMsgCallback;
import me.hekr.sthome.MyApplication;
import me.hekr.sthome.R;
import me.hekr.sthome.autoudp.ControllerWifi;
import me.hekr.sthome.crc.CoderUtils;
import me.hekr.sthome.debugWindow.ViewWindow;
import me.hekr.sthome.service.SiterwellUtil;

/**
 * Created by jishu0001 on 2016/11/16.
 */
public abstract class SendEquipmentData {
    private static final String TAG = "SendEquipmentData";
    private Context context;
    private SendCommand sc;
    private boolean wifiTag;

    public SendEquipmentData(Context context){
        this.context =context;
        sc = new SendCommand(context);
    }

    /**
     * send code
     * @param groupCode
     */
    private void sendAction(final  String groupCode){
        boolean status = NetWorkStatusUtil.getInstance().getNetWorkStatus();
        if (!status){
            Toast.makeText(context, context.getString(R.string.net_error), Toast.LENGTH_SHORT).show();
            return;
        }
        ControllerWifi controllerWifi = ControllerWifi.getInstance();
        wifiTag = controllerWifi.wifiTag;
        Log.i(TAG,"====send tag=== "+wifiTag);
        if(wifiTag){
            if(ConnectionPojo.getInstance().encryption){
                Log.i(TAG,"Udp before encryption:"+groupCode);
                byte[] encode = ByteUtil.getAllEncryption(groupCode);
                new SiterwellUtil(context).sendData(encode);
            }else {
                new SiterwellUtil(context).sendData(groupCode);
            }
        }else {
            try {
                Hekr.getHekrClient().sendMessage(new JSONObject(groupCode), new HekrMsgCallback() {
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
        if(isDebugMode()){
            MyApplication.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ViewWindow.showView(context,groupCode, R.color.redgreen);
                }
            });
        }
    }
    /**
     * send control command
     * @param eqid
     * @param status2
     */
    public void sendEquipmentCommand(final String eqid, final String status2){//send equipment detail
        sendAction(sc.equipControl(eqid,status2));
    }

    /**
     * sendGateWaySilence
     */
    public void sendGateWaySilence(){//send equipment detail
        sendAction(sc.equipControl("0","00000000"));
    }

    public void sendDataSwitchSync(String eqid){
        sendAction(sc.GetDataSwitchList(eqid));
    }

    protected abstract void sendEquipmentDataFailed();

    protected abstract void sendEquipmentDataSuccess();

    /**
     * increace equipment command
     */
    public void increaceEquipment(){
        sendAction(sc.equipIncreace());
    }

    /**
     * delete equipment
     * @param eqid
     */
    public void deleteEquipment(final String eqid){
        sendAction(sc.equipDelete(eqid));
    }


    /**
     * delete equipment
     * @param eqid
     */
    public void replaceEquipment(final String eqid){
        sendAction(sc.equipReplace(eqid));
    }



    /**
     * delete equipment
     * @param eqid
     */
    public void modifyEquipmentName(final String eqid,final String newname){
        sendAction(sc.modifyEquipmentName(eqid,newname));
    }


    public void getDeviceNameInfo(){
        sendAction(sc.getEquipmentName());
    }

    /**
     * syn get device status
     * @param deviceCRC
     */
    public void synGetDeviceStatus(String deviceCRC){
        sendAction(sc.synDeviceStatus(deviceCRC));
    }

    /**
     * syn get device name
     * @param deviceNameCRC
     */
    public void synGetDeviceName(String deviceNameCRC){
        sendAction(sc.synDeviceName(deviceNameCRC));
    }

    /**
     * cancel increase equipment
     */
    public void cancelIncreaseEq(){
        sendAction(sc.cancelEquipIncreace());
    }

    private boolean isDebugMode(){

        SharedPreferences sharedPreferences = ECPreferences.getSharedPreferences();
        ECPreferenceSettings flag = ECPreferenceSettings.SETTINGS_DEBUG;
        boolean autoflag = sharedPreferences.getBoolean(flag.getId(), (boolean) flag.getDefaultValue());
        return autoflag;
    }
}
