package me.hekr.sthome.service;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import me.hekr.sthome.autoudp.ControllerWifi;
import me.hekr.sthome.crc.CoderUtils;
import me.hekr.sthome.tools.ByteUtil;
import me.hekr.sthome.tools.ConnectionPojo;

/**
 * Created by Administrator on 2016/12/22 0022.
 */

public class UDPRecData implements Runnable {
    private static final String TAG = "UDPRecData";
    private static final int PORT = 1025;
    private InetAddress hostip;
    private DatagramSocket datagramSocket;
    private DatagramPacket datagramPacket;
    private byte[] bytes;
    private InetAddress hostAdd;
    private Context context;
    private boolean enudp = true;
    private int type = 0; //0代表接收，1代表绑定，2代表切换网关后台

    UDPRecData(DatagramSocket ds, InetAddress hostAdd, Context context, int type){
        this.datagramSocket = ds;
        this.hostAdd = hostAdd;
        this.context = context;
        this.enudp = true;
        setType(type);
    }

    @Override
    public void run() {
        while (enudp){
            bytes = new byte[512];
            datagramPacket = new DatagramPacket(bytes,bytes.length,hostAdd,PORT);
            try {
                Log.i(TAG," start to receive");
                datagramSocket.receive(datagramPacket);
                String msg;
                if("7b".equals(ByteUtil.getAllDescryption(datagramPacket.getData()).substring(0,2))){
                    ConnectionPojo.getInstance().encryption = true;
                    msg = CoderUtils.getStringFromAscii2(ByteUtil.getAllDescryption(datagramPacket.getData()));
                    Log.i(TAG,"get en udp message:"+msg);
                }else {
                    ConnectionPojo.getInstance().encryption = false;
                    msg = new String(datagramPacket.getData());
                    Log.i(TAG,"get udp message:"+msg);
                }
                hostip = datagramPacket.getAddress();
                resolveData(msg);
            } catch (IOException e) {
                Log.e(TAG," receive failed  Socket closed");
                break;
            }catch (NullPointerException e){
                e.printStackTrace();
                Log.e(TAG," receive failed NullPointerException");
            }
        }
    }

    private void resolveData(String msg){
        String deviceTid = "",bind="",ctrlkey="";
        if(type == 2){
            if(msg.contains("ST_answer_OK") ||  msg.contains("answer_yes_or_no")){
                ControllerWifi.getInstance().switch_server_ok = true;
            }
            return;
        }

        if(!"{".equals(msg.substring(0,1))){
            if(msg.length()>20){
                try{
                    deviceTid = msg.substring(msg.indexOf(":") + 1, msg.indexOf("\n"));
                    Log.i(TAG +"name", "===" + deviceTid);
                }catch (StringIndexOutOfBoundsException e){
                    Log.i(TAG,"get rabish deviceTid");
                }

                try{
                    msg = msg.substring(msg.indexOf("\n") + 1);
                    bind = msg.substring(msg.indexOf(":") + 1, msg.indexOf("\n"));
                    Log.i(TAG +"bind", "===" + bind);
                }catch (StringIndexOutOfBoundsException e){
                    Log.i(TAG,"get rabish bind");
                }

                try{
                    msg = msg.substring(msg.indexOf("\n") + 1);
                    ctrlkey = msg.substring(msg.indexOf(":") + 1, msg.indexOf("\n"));
                    Log.i(TAG +"key", "===" + ctrlkey);
                }catch (StringIndexOutOfBoundsException e){
                    Log.i(TAG,"get rabish ctrlkey");
                }

                if(type==1){

                    if( !TextUtils.isEmpty(ConnectionPojo.getInstance().deviceTid) && ConnectionPojo.getInstance().deviceTid.equals(deviceTid)){
                        ControllerWifi.getInstance().wifiTag = true;
                    }
                    ControllerWifi.getInstance().targetip = hostip;
                    ControllerWifi.getInstance().deviceTid = deviceTid;
                    ControllerWifi.getInstance().bind = bind;
                    ControllerWifi.getInstance().ctrlKey = ctrlkey;
                    Log.i(TAG," LAN.targetip="+ hostip.toString());
                    Log.i(TAG," LAN.deviceTid="+ deviceTid);
                    Log.i(TAG," LAN.bind="+ bind);
                    Log.i(TAG," LAN.ctrlKey="+ ctrlkey);
                }else{
                    if( !TextUtils.isEmpty(ConnectionPojo.getInstance().deviceTid) && ConnectionPojo.getInstance().deviceTid.equals(deviceTid)){
                        ControllerWifi.getInstance().wifiTag = true;
                        ControllerWifi.getInstance().targetip = hostip;
                        ControllerWifi.getInstance().deviceTid = deviceTid;
                        ControllerWifi.getInstance().bind = bind;
                        ControllerWifi.getInstance().ctrlKey = ctrlkey;
                        Log.i(TAG," LAN.targetip="+ hostip.toString());
                        Log.i(TAG," LAN.deviceTid="+ deviceTid);
                        Log.i(TAG," LAN.bind="+ bind);
                        Log.i(TAG," LAN.ctrlKey="+ ctrlkey);
                    }
                }
            }
        }else{
            try {
                JSONObject jsonObject = new JSONObject(msg);
                if(jsonObject.has("NAME")&&jsonObject.has("KEY")){
                     String devTid2 = jsonObject.getString("NAME");
                     String bindkey2 = jsonObject.getString("BIND");
                     String ctrlkey2 = jsonObject.getString("KEY");
                    if(type==1){
                        if( !TextUtils.isEmpty(ConnectionPojo.getInstance().deviceTid) && ConnectionPojo.getInstance().deviceTid.equals(devTid2)){
                            ControllerWifi.getInstance().wifiTag = true;
                        }
                        ControllerWifi.getInstance().targetip = hostip;
                        ControllerWifi.getInstance().deviceTid = devTid2;
                        ControllerWifi.getInstance().bind = bindkey2;
                        ControllerWifi.getInstance().ctrlKey = ctrlkey2;
                        Log.i(TAG,"en: LAN.targetip="+ hostip.toString());
                        Log.i(TAG,"en: LAN.deviceTid="+ devTid2.toString());
                        Log.i(TAG,"en: LAN.bind="+ bindkey2.toString());
                        Log.i(TAG,"en: LAN.ctrlKey="+ ctrlkey2.toString());
                    }else{
                        if( !TextUtils.isEmpty(ConnectionPojo.getInstance().deviceTid) && ConnectionPojo.getInstance().deviceTid.equals(devTid2)){
                            ControllerWifi.getInstance().wifiTag = true;
                            ControllerWifi.getInstance().targetip = hostip;
                            ControllerWifi.getInstance().deviceTid = devTid2;
                            ControllerWifi.getInstance().bind = bindkey2;
                            ControllerWifi.getInstance().ctrlKey = ctrlkey2;
                            Log.i(TAG,"en: LAN.targetip="+ hostip.toString());
                            Log.i(TAG,"en: LAN.deviceTid="+ devTid2.toString());
                            Log.i(TAG,"en: LAN.bind="+ bindkey2.toString());
                            Log.i(TAG,"en: LAN.ctrlKey="+ ctrlkey2.toString());
                        }
                    }
                }else {
                    if(ControllerWifi.getInstance().wifiTag){
                        Intent intent = new Intent(SiterService.UDP_BROADCAST);
                        intent.putExtra("message",msg);
                        context.sendBroadcast(intent);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                if(ControllerWifi.getInstance().wifiTag){
                    Intent intent = new Intent(SiterService.UDP_BROADCAST);
                    intent.putExtra("message",msg);
                    context.sendBroadcast(intent);
                }
            }
        }
    }


    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void  close(){
        try {
            enudp = false;
            datagramSocket.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
