package me.hekr.sthome.common;

import android.content.Context;

import java.util.Locale;

/**
 * @author skygge
 * @date 2020/10/22.
 * GitHub：javofxu@github.com
 * email：skygge@yeah.net
 * description：
 */
public class ConfigureUtil {

    /**
     * 隐私政策网址
     */
    public static String getPrivacyPolicy(Context context){
        Locale locale = context.getResources().getConfiguration().locale;
        String lan = locale.getLanguage();
        switch (lan) {
            case "zh":
                return "file:///android_asset/privacy/privacy_zh.html";
            case "fr":
                return "file:///android_asset/privacy/privacy_fr.html";
            case "de":
                return "file:///android_asset/privacy/privacy_de.html";
            case "es":
                return "file:///android_asset/privacy/privacy_es.html";
            case "en":
            default:
                return "file:///android_asset/privacy/privacy_en.html";
        }
    }

    /**
     * 用户协议网址
     */
    public static String getUserAgreement(Context context){
        Locale locale = context.getResources().getConfiguration().locale;
        String lan = locale.getLanguage();
        switch (lan) {
            case "zh":
                return "file:///android_asset/agreement/user_protocol_zh.html";
            case "fr":
                return "file:///android_asset/agreement/user_protocol_fr.html";
            case "de":
                return "file:///android_asset/agreement/user_protocol_de.html";
            case "es":
                return "file:///android_asset/agreement/user_protocol_es.html";
            case "en":
            default:
                return "file:///android_asset/agreement/user_protocol_en.html";
        }
    }
}
