package me.hekr.sthome.equipment.data;

import me.hekr.sthome.R;

/**
 * @author skygge
 * @date 2020-04-08.
 * GitHub：javofxu@github.com
 * email：skygge@yeah.net
 * description：系统说明书
 */
public enum InstructionsHm {

    GATEWAY("GS188A", R.string.gateway, R.mipmap.gs188,"GS188A User Manual_V2.01.pdf"),

    CO_ALARM("GS816A", R.string.coalarm, R.mipmap.gs816,"GS816A CO Alarm User Manual_Ver01.01.pdf"),

    THERMAL_ALARM("GS412A", R.string.thermalalarm, R.mipmap.gs412,"GS412A Heat Alarm User Manual_Ver01.01.pdf"),

    WATER_ALARM("GS156A", R.string.wt, R.mipmap.gs156,"GS156A Wtater Alarm User Manual_Ver01.01.pdf"),

    SX_SM_ALARM("GS559A", R.string.cxsmalarm, R.mipmap.gs592,"GS559A Photoelectric Smoke Alarm User Manual_Ver01.01.pdf"),

    SX_SM_ALARM2("GS592A", R.string.cxsmalarm, R.mipmap.gs592,"GS559A Photoelectric Smoke Alarm User Manual_Ver01.01.pdf"),

    PIR("GS300A", R.string.pir, R.mipmap.gs300d,"GS300A Motion Detector User Manual_Ver01.01.pdf"),

    DOOR("GS320A", R.string.door, R.mipmap.gs320d,"GS320A Door Sensor User Manual_Ver01.01.pdf"),

    SOS("GS390A", R.string.soskey, R.mipmap.gs390,"GS390A SOS Emergency Button User Manual_Ver01.01.pdf"),

    OUTDOOR("GS380A", R.string.outdoor_siren, R.mipmap.gs380,"GS380A Outdoor Siren Manual_Ver01.00.pdf"),

    PIC("GS290", R.string.camera, R.mipmap.gs290,"GS290A IP Camera User Manual_Ver01.01.pdf"),

    LOCK("GS921A", R.string.lock, R.mipmap.gs920,"GS920A Door Lock User Manual_Ver01.01.pdf"),

    SOCKET("GS350A", R.string.socket, R.mipmap.gs350,"GS350A Smart Socket User Manual_Ver01.01.pdf"),

    BUTTON("GS585A", R.string.button, R.mipmap.gs585,"GS585A Custom Button User Manual_Ver01.00.pdf"),

    MODE_BUTTON("GS584A", R.string.mode_button, R.mipmap.gs584,"GS584A Scene Switch Button User Manual_Ver01.00.pdf"),

    HUMITURE("GS240A", R.string.thcheck, R.mipmap.gs240d,"GS240A Hygrothermograph User Manual_Ver01.00.pdf"),

    THERMOSTAT("GS361A", R.string.temp_controler, R.mipmap.gs361,"GS361A Radiator Thermostat User Manual_Ver01.00.pdf");

    private String model;

    private int drawableRes;

    private int productRes;

    private String link;

    InstructionsHm(String model, int product, int icon, String link) {
        this.model = model;
        this.productRes = product;
        this.drawableRes = icon;
        this.link = link;
    }

    public String getModel() {
        return model;
    }

    public int getProductRes() {
        return productRes;
    }

    public int getDrawableRes() {
        return drawableRes;
    }

    public String getLink() {
        return link;
    }
}
