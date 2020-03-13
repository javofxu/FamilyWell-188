package me.hekr.sthome.model.modelbean;

import java.io.Serializable;

/**
 * Created by henry on 2019/8/6.
 */

public class DataSwitchSubBean implements Serializable {
    private int subid;
    private String eqid;
    private String deviceid;
    private int status;

    public int getSubid() {
        return subid;
    }

    public void setSubid(int subid) {
        this.subid = subid;
    }

    public String getEqid() {
        return eqid;
    }

    public void setEqid(String eqid) {
        this.eqid = eqid;
    }

    public String getDeviceid() {
        return deviceid;
    }

    public void setDeviceid(String deviceid) {
        this.deviceid = deviceid;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "DataSwitchSubBean{" +
                "subid=" + subid +
                ", eqid='" + eqid + '\'' +
                ", deviceid='" + deviceid + '\'' +
                ", status=" + status +
                '}';
    }
}
