package me.hekr.sthome.tools;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.support.annotation.RequiresApi;

import java.io.IOException;

import me.hekr.sthome.MyApplication;

/**
 * @author skygge
 * @date 2020/7/28.
 * GitHub：javofxu@github.com
 * email：skygge@yeah.net
 * description：
 */
public class NetWorkStatusUtil {

    private volatile static NetWorkStatusUtil instance;

    public static NetWorkStatusUtil getInstance() {
        if (instance == null){
            synchronized (NetWorkStatusUtil.class){
                instance = new NetWorkStatusUtil();
            }
        }
        return instance;
    }

    public boolean getNetWorkStatus(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ConnectivityManager manager = (ConnectivityManager) MyApplication.getAppContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkCapabilities networkCapabilities = manager.getNetworkCapabilities(manager.getActiveNetwork());
            return networkCapabilities != null && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
        }else {
            Runtime runtime = Runtime.getRuntime();
            try {
                Process p = runtime.exec("ping -c 3 www.baidu.com");
                int ret = p.waitFor();
                return ret == 0;
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                return false;
            }
        }
    }
}
