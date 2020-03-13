package me.hekr.sthome.debugWindow;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ListView;


import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import me.hekr.sthome.R;
import me.hekr.sthome.tools.ECPreferenceSettings;
import me.hekr.sthome.tools.ECPreferences;


/**
 * Created by Administrator on 2017/10/16.
 */

public class ViewWindow {
    private static WindowManager.LayoutParams mLayoutParams;
    private static WindowManager mWindowManager;
    private static View mView;
    private static ListView lv;
    private static DebugAdapter adapter;
    private static List<LogBean> lists;
    public static boolean debug = false;
    public static int debug_number = 6;

    public static void initView(Context context) {

        SharedPreferences sharedPreferences = ECPreferences.getSharedPreferences();
        ECPreferenceSettings flag = ECPreferenceSettings.SETTINGS_DEBUG;
        debug = sharedPreferences.getBoolean(flag.getId(), (boolean) flag.getDefaultValue());
        if(debug){
            debug_number = Integer.valueOf(sharedPreferences.getString("debug_number", "10"));

            mWindowManager = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
            mLayoutParams = new WindowManager.LayoutParams(-2, -2, Build.VERSION.SDK_INT >= Build.VERSION_CODES.O?WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY:2005, 24, -3);
            mLayoutParams.gravity = 51;
            lists = new ArrayList<>();
            mView = LayoutInflater.from(context).inflate(R.layout.window_tasks, null);
            lv = (ListView) mView.findViewById(R.id.lv);
            adapter = new DebugAdapter(lists, context);
            lv.setAdapter(adapter);
        }

    }


    /**
     * debug窗口添加数据 必须在主线程
     */
    public static synchronized void showView(Context context, String str, int colorResId) {
        if (mWindowManager == null) {
            initView(context);
        }
        if (debug && lv != null && adapter != null) {
            while (lists.size() > debug_number - 1) {
                lists.remove(0);
            }
            long time = System.currentTimeMillis();
            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.getDefault());
            LogBean logBean;
            String text = TextUtils.concat(sdf.format(time), "\n", str).toString();
            logBean = colorResId == -1 ? new LogBean(text) : new LogBean(text, colorResId);
            lists.add(logBean);
            adapter.notifyDataSetChanged();
            try {
                mWindowManager.addView(mView, mLayoutParams);
            } catch (Exception e) {
            }
        }
    }


    /**
     * debug窗口添加数据 任意线程
     *
     * @param var1 String类型数据
     */
    public static synchronized void showView(String var1) {
        showView(var1, -1);
    }

    /**
     * debug窗口添加数据 任意线程
     *
     * @param var1 String类型数据
     */
    public static synchronized void showView(String var1, int colorResId) {
        if (debug) {
            EventBus.getDefault().post(new DebugEvent(var1, colorResId));
        }
    }

    /**
     * 移出窗口
     */
    public static void removeView() {
        try {
            mWindowManager.removeView(mView);
        } catch (Exception e) {
        }
    }


    /**
     * 登录切换clear
     */
    public static void clearView() {
        if (debug && lists != null && !lists.isEmpty() && adapter != null) {
            lists.clear();
            adapter.notifyDataSetChanged();
            removeView();
            try {
                mWindowManager.addView(mView, mLayoutParams);
            } catch (Exception e) {
            }
            mWindowManager = null;
        }
    }
}
