package me.hekr.sthome.event;

import java.util.List;

import me.hekr.sthome.model.modelbean.DataSwitchSubBean;

/**
 * Created by henry on 2019/8/9.
 */

public class DataSwitchRefreshEvent {
    private String eqid;
    private List<DataSwitchSubBean> list;
    private String deviceid;

    public String getEqid() {
        return eqid;
    }

    public void setEqid(String eqid) {
        this.eqid = eqid;
    }

    public List<DataSwitchSubBean> getList() {
        return list;
    }

    public void setList(List<DataSwitchSubBean> list) {
        this.list = list;
    }

    public String getDeviceid() {
        return deviceid;
    }

    public void setDeviceid(String deviceid) {
        this.deviceid = deviceid;
    }
}
