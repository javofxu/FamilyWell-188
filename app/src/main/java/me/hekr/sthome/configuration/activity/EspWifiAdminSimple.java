package me.hekr.sthome.configuration.activity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import static android.content.Context.WIFI_SERVICE;

public class EspWifiAdminSimple {
	private static final String TAG = "EspWifiAdminSimple";
	private final Context mContext;
	private WifiManager wifiManager;
	
	public EspWifiAdminSimple(Context context) {
		mContext = context;
		wifiManager = (WifiManager) mContext.getApplicationContext().getSystemService(WIFI_SERVICE);
	}

	public String getWifiConnectedSsid() {
		WifiInfo mWifiInfo = getConnectionInfo();
		String ssid = null;
		if (mWifiInfo != null && isWifiConnected()) {
			int len = mWifiInfo.getSSID().length();
			if (mWifiInfo.getSSID().startsWith("\"")
					&& mWifiInfo.getSSID().endsWith("\"")) {
				ssid = mWifiInfo.getSSID().substring(1, len - 1);
			} else {
				ssid = mWifiInfo.getSSID();
			}

		}
		return ssid;
	}
	
	public String getWifiConnectedSsidAscii(String ssid) {
		final long timeout = 100;
		final long interval = 20;
		String ssidAscii = ssid;
		wifiManager.startScan();

		boolean isBreak = false;
		long start = System.currentTimeMillis();
		do {
			try {
				Thread.sleep(interval);
			} catch (InterruptedException ignore) {
				isBreak = true;
				break;
			}
			List<ScanResult> scanResults = wifiManager.getScanResults();
			for (ScanResult scanResult : scanResults) {
				if (scanResult.SSID != null && scanResult.SSID.equals(ssid)) {
					isBreak = true;
					try {
						Field wifiSsidfield = ScanResult.class
								.getDeclaredField("wifiSsid");
						wifiSsidfield.setAccessible(true);
						Class<?> wifiSsidClass = wifiSsidfield.getType();
						Object wifiSsid = wifiSsidfield.get(scanResult);
						Method method = wifiSsidClass
								.getDeclaredMethod("getOctets");
						byte[] bytes = (byte[]) method.invoke(wifiSsid);
						ssidAscii = new String(bytes, "ISO-8859-1");
					} catch (Exception e) {
						e.printStackTrace();
					}
					break;
				}
			}
		} while (System.currentTimeMillis() - start < timeout && !isBreak);

		return ssidAscii;
	}
	
	public String getWifiConnectedBssid() {
		WifiInfo mWifiInfo = getConnectionInfo();
		String bssid = null;
		if (mWifiInfo != null && isWifiConnected()) {
			bssid = mWifiInfo.getBSSID();
		}
		return bssid;
	}

	// get the wifi info which is "connected" in wifi-setting
	private WifiInfo getConnectionInfo() {
		return wifiManager.getConnectionInfo();
	}

	private boolean isWifiConnected() {
		NetworkInfo mWiFiNetworkInfo = getWifiNetworkInfo();
		boolean isWifiConnected = false;
		if (mWiFiNetworkInfo != null) {
			isWifiConnected = mWiFiNetworkInfo.isConnected();
		}
		return isWifiConnected;
	}

	private NetworkInfo getWifiNetworkInfo() {
		ConnectivityManager mConnectivityManager = (ConnectivityManager) mContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mWiFiNetworkInfo = mConnectivityManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		return mWiFiNetworkInfo;
	}

	boolean startScan(){
		return wifiManager.startScan();
	}

	ScanResult findWifiList() {
		List<ScanResult> scanWifiList = wifiManager.getScanResults();
		if (scanWifiList != null && scanWifiList.size() > 0) {
			for (int i = 0; i < scanWifiList.size(); i++) {
				ScanResult scanResult = scanWifiList.get(i);
				Log.e(TAG, "搜索的wifi-SSId:" + scanResult.SSID);
				if (scanResult.SSID.contains("ESP")){
					return scanResult;
				}
			}
		}else {
			Log.e(TAG, "没有搜索到wifi");
		}
		return null;
	}

	// 添加一个网络并连接
	boolean addNetwork(String SSId, String password, int type) {
		WifiConfiguration wcg = configWifiInfo(SSId, password, type);
		int netId = wcg.networkId;
		if (netId == -1){
			netId = wifiManager.addNetwork(wcg);
		}
		return wifiManager.enableNetwork(netId, true);
	}

	private WifiConfiguration configWifiInfo(String SSID, String password, int type) {
		WifiConfiguration config = null;
		if ( wifiManager!= null) {
			List<WifiConfiguration> existingConfigs = wifiManager.getConfiguredNetworks();
			for (WifiConfiguration existingConfig : existingConfigs) {
				if (existingConfig == null) continue;
				if (existingConfig.SSID.equals("\"" + SSID + "\"")  /*&&  existingConfig.preSharedKey.equals("\""  +  password  +  "\"")*/) {
					config = existingConfig;
					break;
				}
			}
		}
		if (config == null) {
			config = new WifiConfiguration();
		}
		config.allowedAuthAlgorithms.clear();
		config.allowedGroupCiphers.clear();
		config.allowedKeyManagement.clear();
		config.allowedPairwiseCiphers.clear();
		config.allowedProtocols.clear();
		config.SSID = "\"" + SSID + "\"";
		// 分为三种情况：0没有密码1用wep加密2用wpa加密
		if (type == 0) {// WIFICIPHER_NOPASSwifiCong.hiddenSSID = false;
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
		} else if (type == 1) {  //  WIFICIPHER_WEP
			config.hiddenSSID = true;
			config.wepKeys[0] = "\"" + password + "\"";
			config.allowedAuthAlgorithms
					.set(WifiConfiguration.AuthAlgorithm.SHARED);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
			config.allowedGroupCiphers
					.set(WifiConfiguration.GroupCipher.WEP104);
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			config.wepTxKeyIndex = 0;
		} else if (type == 2) {   // WIFICIPHER_WPA
			config.preSharedKey = "\"" + password + "\"";
			config.hiddenSSID = true;
			config.allowedAuthAlgorithms
					.set(WifiConfiguration.AuthAlgorithm.OPEN);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
			config.allowedPairwiseCiphers
					.set(WifiConfiguration.PairwiseCipher.TKIP);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
			config.allowedPairwiseCiphers
					.set(WifiConfiguration.PairwiseCipher.CCMP);
			config.status = WifiConfiguration.Status.ENABLED;
		}
		return config;
	}
}
