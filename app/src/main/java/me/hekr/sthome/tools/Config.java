package me.hekr.sthome.tools;


import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;


import com.loopj.android.http.AsyncHttpResponseHandler;

import cz.msebera.android.httpclient.Header;
import me.hekr.sdk.utils.CacheUtil;
import me.hekr.sthome.R;
import me.hekr.sthome.http.BaseHttpUtil;
import me.hekr.sthome.http.HekrCodeUtil;
import me.hekr.sthome.http.HekrUser;

public class Config {
	private static final String TAG = "Config";

	public final static String RootPath = "61.164.94.198:1415/app";
	public final static String ApkVerUrl = "http://61.164.94.198:1415/point/app/SiterAction!getAppUpdate.action";
	public final static String ApkUrl = "http://"+RootPath+"/Siterwell_GS188X_Series.apk";

	public static final String UPDATE_APKNAME="Siterwell_GS188X_Series.apk";


	public static int getVerCode(Context context,String packageName) {
		int verCode = -1;
		try {
			verCode = context.getPackageManager().getPackageInfo(
					packageName, 0).versionCode;
		} catch (NameNotFoundException e) {
			Log.e(TAG, e.getMessage());
		}
		return verCode;
	}
	
	public static String getVerName(Context context,String packageName) {
		String verName = "";
		try {
			verName = context.getPackageManager().getPackageInfo(
					packageName, 0).versionName;
		} catch (NameNotFoundException e) {
			Log.e(TAG, e.getMessage());
		}
		return verName;	

	}
	
	public static String getAppName(Context context) {
		String verName = context.getResources()
		.getText(R.string.app_name).toString();
		return verName;
	}

	/**
	 * 3.5 获取APP更新信息
	 *
	 * @param loginListener 回调接口
	 */

	public static void getUpdateInfo(Context mContext, final HekrUser.LoginListener loginListener) {
		String path= CacheUtil.getString(SiterSDK.SETTINGS_CONFIG_VER,"");
		BaseHttpUtil.getData(mContext, path, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int i, Header[] headers, byte[] bytes) {
				loginListener.loginSuccess(new String(bytes));
			}

			@Override
			public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
				loginListener.loginFail(HekrCodeUtil.getErrorCode(i, bytes));
			}
		});
	}


	public static void getWeatherInfo(Context mContext, final HekrUser.LoginListener loginListener,String url) {

		BaseHttpUtil.getData(mContext, url, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int i, Header[] headers, byte[] bytes) {
				loginListener.loginSuccess(new String(bytes));
			}

			@Override
			public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
				loginListener.loginFail(HekrCodeUtil.getErrorCode(i, bytes));
			}
		});
	}

	public static void getPlaceInfo(Context mContext, final HekrUser.LoginListener loginListener,String url) {

		BaseHttpUtil.getData(mContext, url, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int i, Header[] headers, byte[] bytes) {
				loginListener.loginSuccess(new String(bytes));
			}

			@Override
			public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
				loginListener.loginFail(HekrCodeUtil.getErrorCode(i, bytes));
			}
		});
	}


	public static class UpdateInfo {
		public int id;
		public int code;
		public String version;
        public String url;
		public String url_ex;
        public String en;
        public String zh;
		public String fr;
		public String de;
		public String es;
		public String fi;
		public String nl;
		public String it;
		public String sl;
		public String cs;
		public String da;
		public String sv;
		public String nb;
		public String ja;

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public int getCode() {
			return code;
		}

		public void setCode(int code) {
			this.code = code;
		}

		public String getVersion() {
			return version;
		}

		public void setVersion(String version) {
			this.version = version;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public String getEn() {
			return en;
		}

		public void setEn(String en) {
			this.en = en;
		}

		public String getZh() {
			return zh;
		}

		public void setZh(String zh) {
			this.zh = zh;
		}

		public String getFr() {
			return fr;
		}

		public void setFr(String fr) {
			this.fr = fr;
		}

		public String getDe() {
			return de;
		}

		public void setDe(String de) {
			this.de = de;
		}

		public String getEs() {
			return es;
		}

		public void setEs(String es) {
			this.es = es;
		}

		public String getFi() {
			return fi;
		}

		public void setFi(String fi) {
			this.fi = fi;
		}

		public String getNl() {
			return nl;
		}

		public void setNl(String nl) {
			this.nl = nl;
		}

		public String getIt() {
			return it;
		}

		public void setIt(String it) {
			this.it = it;
		}

		public String getSl() {
			return sl;
		}

		public void setSl(String sl) {
			this.sl = sl;
		}

		public String getCs() {
			return cs;
		}

		public void setCs(String cs) {
			this.cs = cs;
		}

		public String getUrl_ex() {
			return url_ex;
		}

		public void setUrl_ex(String url_ex) {
			this.url_ex = url_ex;
		}

		public String getDa() {
			return da;
		}

		public void setDa(String da) {
			this.da = da;
		}

		public String getSv() {
			return sv;
		}

		public void setSv(String sv) {
			this.sv = sv;
		}

		public String getNb() {
			return nb;
		}

		public void setNb(String nb) {
			this.nb = nb;
		}

		public String getJa() {
			return ja;
		}

		public void setJa(String ja) {
			this.ja = ja;
		}
	};
}

