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
public abstract class SendSceneData {
    private static final String TAG = "SendSceneData";
    private Context context;
    private SendCommand sc;
    private boolean wifiTag;

    public SendSceneData(Context context){
        this.context =context;
        sc = new SendCommand(context);
    }
    protected abstract void sendEquipmentDataFailed();

    protected abstract void sendEquipmentDataSuccess();


    /**
     * send code
     * @param groupCode
     */
    private void sendAction(final  String groupCode){
        boolean status = NetWorkUtils.isNetworkAvailable(context);
        if (!status){
            Toast.makeText(context, context.getString(R.string.net_error), Toast.LENGTH_SHORT).show();
            return;
        }
        ControllerWifi controllerWifi = ControllerWifi.getInstance();
        wifiTag = controllerWifi.wifiTag;
        Log.i(TAG,"===send tag==="+wifiTag);
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
     * increace scene
     * @param deCode
     */
    public void increaceScene(final String deCode){
        sendAction(sc.increaceScene(deCode));
    }

    /**
     * modify scene
     * @param deCode
     */
    public void modifyScene(final String deCode){
        sendAction(sc.modifyScene(deCode));
    }

    /**
     * delete scene
     * @param id
     */
    public void deleteScene(final String id){
        sendAction(sc.deleteScene(id));
    }

    /**
     * syn get scene information
     * @param groupId
     * @param sceneGCRC
     * @param sceneCRC
     */
    public void synGetSceneInformation(String groupId,String sceneGCRC,String sceneCRC){
        sendAction(sc.synScene(groupId,sceneGCRC,sceneCRC));
    }

    /**
     * handleScene
     * @param mid
     */
    public void handleScene(final String mid){
        sendAction(sc.sceneHandle(mid));
    }

    private boolean isDebugMode(){

        SharedPreferences sharedPreferences = ECPreferences.getSharedPreferences();
        ECPreferenceSettings flag = ECPreferenceSettings.SETTINGS_DEBUG;
        boolean autoflag = sharedPreferences.getBoolean(flag.getId(), (boolean) flag.getDefaultValue());
        return autoflag;
    }
}
