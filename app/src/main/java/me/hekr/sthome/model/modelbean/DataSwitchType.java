package me.hekr.sthome.model.modelbean;

import me.hekr.sthome.R;

/**
 * Created by henry on 2019/4/17.
 */

public enum DataSwitchType {
    // 火灾报警器报警
    STATUS_FIRE_ALARMER_WARN(3,
            R.drawable.e8,R.string.data_switch_sm_warn),
    //火灾报警器低压
    STATUS_FIRE_ALARMER_LOW_VOLTAGE(10,
            R.drawable.y8,R.string.data_switch_sm_low_voltage),
    //火灾报警器正常
    STATUS_FIRE_ALARMER_NORMAL(5,
            R.drawable.g8,R.string.data_switch_sm_normal),
    //gas报警器报警
    STATUS_GAS_ALARMER_WARN(6,
            R.drawable.e3,R.string.data_switch_gas_warn),
    //gas报警器正常
    STATUS_GAS_ALARMER_NORMAL(7,
            R.drawable.g3,R.string.data_switch_gas_normal),
    //水浸报警器报警
    STATUS_WATER_ALARMER_WARN(11,
            R.drawable.e5,R.string.data_switch_water_warn),
    //水浸报警器正常
    STATUS_WATER_ALARMER_NORMAL(13,
            R.drawable.g5,R.string.data_switch_water_normal),
    //设备删除
    STATUS_DEVICE_DELETE(14,
            R.drawable.g5,R.string.data_switch_water_normal),
    //未知状态
    STATUS_UNKNOWN(30,
            R.drawable.error,R.string.error_e);


    private int statusId;
    private int drawResId;
    private int strId;

    DataSwitchType(int statusid, int iconid,int strId) {
        this.statusId = statusid;
        this.drawResId = iconid;
        this.strId = strId;
    }

    public int getStatusId() {
        return statusId;
    }

    public void setStatusId(int statusId) {
        this.statusId = statusId;
    }

    public int getDrawResId() {
        return drawResId;
    }

    public void setDrawResId(int drawResId) {
        this.drawResId = drawResId;
    }

    public int getStrId() {
        return strId;
    }

    public void setStrId(int strId) {
        this.strId = strId;
    }

    public static DataSwitchType getType(int statusId) {
        for (DataSwitchType subDevType : DataSwitchType.values()) {
            if (subDevType.getStatusId()==statusId) {
                return subDevType;
            }
        }
        return STATUS_UNKNOWN;
    }
}
