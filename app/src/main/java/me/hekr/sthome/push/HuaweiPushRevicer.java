/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2016. All rights reserved.
 * See LICENSE.txt for this sample's licensing information.
 */

package me.hekr.sthome.push;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.huawei.android.pushagent.PushReceiver;

import org.greenrobot.eventbus.EventBus;

import java.io.InvalidClassException;

import me.hekr.sdk.utils.CacheUtil;
import me.hekr.sthome.event.STEvent;
import me.hekr.sthome.tools.ECPreferenceSettings;
import me.hekr.sthome.tools.ECPreferences;
import me.hekr.sthome.tools.SiterSDK;

/**
 * 应用需要创建一个子类继承com.huawei.hms.support.api.push.PushReceiver，
 * 实现onToken，onPushState ，onPushMsg，onEvent，这几个抽象方法，用来接收token返回，push连接状态，透传消息和通知栏点击事件处理。
 * onToken 调用getToken方法后，获取服务端返回的token结果，返回token以及belongId
 * onPushState 调用getPushState方法后，获取push连接状态的查询结果
 * onPushMsg 推送消息下来时会自动回调onPushMsg方法实现应用透传消息处理。本接口必须被实现。 在开发者网站上发送push消息分为通知和透传消息
 *           通知为直接在通知栏收到通知，通过点击可以打开网页，应用 或者富媒体，不会收到onPushMsg消息
 *           透传消息不会展示在通知栏，应用会收到onPushMsg
 * onEvent 该方法会在设置标签、点击打开通知栏消息、点击通知栏上的按钮之后被调用。由业务决定是否调用该函数。
 */
public class HuaweiPushRevicer extends PushReceiver {

	public static final String TAG = "HuaweiPushRevicer";

	public static final String ACTION_UPDATEUI = "action.updateUI";
    @Override
    public void onToken(Context context, String token, Bundle extras) {
    	String belongId = extras.getString("belongId");
        Log.i(TAG, "belongId为:" + belongId);
        Log.i(TAG, "Token为:" + token);
        String de  = CacheUtil.getString(SiterSDK.SETTINGS_CONFIG_REGION,"");
        if(TextUtils.isEmpty(FirebaseInstanceId.getInstance().getToken())||de.contains("hekr.me")){
            STEvent stEvent = new STEvent();
            stEvent.setRefreshevent(12);
            stEvent.setFcm_token(token);
            EventBus.getDefault().post(stEvent);
            try {
                ECPreferences.savePreference(ECPreferenceSettings.SETTINGS_HUAWEI_TOKEN, token, true);
            } catch (InvalidClassException e) {
                e.printStackTrace();
            }
        }else{
            STEvent stEvent = new STEvent();
            stEvent.setRefreshevent(14);
            stEvent.setFcm_token(token);
            EventBus.getDefault().post(stEvent);
        }




    }

    @Override
    public boolean onPushMsg(Context context, byte[] msg, Bundle bundle) {
        try {
        	//CP可以自己解析消息内容，然后做相应的处理
            String content = new String(msg, "UTF-8");
            Log.i(TAG, "收到PUSH透传消息,消息内容为:" + content);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void onEvent(Context context, Event event, Bundle extras) {
        if (Event.NOTIFICATION_OPENED.equals(event) || Event.NOTIFICATION_CLICK_BTN.equals(event)) {
            int notifyId = extras.getInt(BOUND_KEY.pushNotifyId, 0);
        	Log.i(TAG, "收到通知栏消息点击事件,notifyId:" + notifyId);
            if (0 != notifyId) {
                NotificationManager manager = (NotificationManager) context
                        .getSystemService(Context.NOTIFICATION_SERVICE);
                manager.cancel(notifyId);
            }
        }
        
        String message = extras.getString(BOUND_KEY.pushMsgKey);
        super.onEvent(context, event, extras);
    }

    @Override
    public void onPushState(Context context, boolean pushState) {
    	Log.i("TAG", "Push连接状态为:" + pushState);
    	
        Intent intent = new Intent();
        intent.setAction(ACTION_UPDATEUI);
        intent.putExtra("type", 2); 
        intent.putExtra("pushState", pushState);  
        context.sendBroadcast(intent);  
    }
}
