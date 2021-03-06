package me.hekr.sthome.tools;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import me.hekr.sdk.Hekr;
import me.hekr.sdk.inter.HekrMsgCallback;
import me.hekr.sthome.MyApplication;
import me.hekr.sthome.R;
import me.hekr.sthome.autoudp.ControllerWifi;
import me.hekr.sthome.crc.CoderUtils;
import me.hekr.sthome.debugWindow.ViewWindow;
import me.hekr.sthome.service.NetWorkUtils;
import me.hekr.sthome.service.SiterwellUtil;

/**
 * Created by jishu0001 on 2016/11/17.
 */
public abstract class SendSceneGroupData {
    private Context context;
    private SendCommand sc;
    private static final String TAG = "SendSceneGroupData";

    public SendSceneGroupData(Context context){
        this.context =context;
        sc = new SendCommand(context);
    }
    /**
     * send code
     * @param groupCode
     */
    private void sendAction(final String groupCode){
        boolean status = NetWorkUtils.isNetworkAvailable(context);
        if (!status){
            Toast.makeText(context, context.getString(R.string.net_error), Toast.LENGTH_SHORT).show();
            return;
        }
        Log.i(TAG,"===send tag==="+ControllerWifi.getInstance().wifiTag);
        if(ControllerWifi.getInstance().wifiTag){
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
    protected abstract void sendEquipmentDataFailed();

    protected abstract void sendEquipmentDataSuccess();


    /**
     * scene group chose
     * @param position
     */
    public void sceneGroupChose(final int position){
        sendAction(sc.sceneGroupChose(position));
    }

    /**
     * increace scene group
     * @param fullCode
     */
    public void increaceSceneGroup(final String fullCode){

        sendAction(sc.increaceSceneGroup(fullCode));
    }

    /**
     * modify scene group
     * @param deCode
     */
    public void modifySceneGroup(final String deCode){
        sendAction(sc.modifySceneGroup(deCode));
    }

    /**
     * delete scene group
     * @param sceneGroupid
     */
    public void deleteSceneGroup(final String sceneGroupid){
        sendAction(sc.deleteSceneGroup(sceneGroupid));
    }

    private boolean isDebugMode(){

        SharedPreferences sharedPreferences = ECPreferences.getSharedPreferences();
        ECPreferenceSettings flag = ECPreferenceSettings.SETTINGS_DEBUG;
        boolean autoflag = sharedPreferences.getBoolean(flag.getId(), (boolean) flag.getDefaultValue());
        return autoflag;
    }
}
