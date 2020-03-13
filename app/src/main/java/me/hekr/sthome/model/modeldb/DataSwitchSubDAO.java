package me.hekr.sthome.model.modeldb;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import me.hekr.sthome.model.modelbean.DataSwitchSubBean;
import me.hekr.sthome.model.modelbean.NoticeBean;

/**
 * Created by jishu0001 on 2017/3/1.
 */

public class DataSwitchSubDAO {
    private static final String TAG = DataSwitchSubDAO.class.getName();
    private SysDB sys;
    private Context context;
    public DataSwitchSubDAO(Context context){

        try {
            this.context = context;
            this.sys = new SysDB(context);
        }catch (NullPointerException e){
            e.printStackTrace();
        }
    }




    /**
     * 插入批量设备到数据库
     *
     * @param dataSwitchSubBeans
     * @return
     */
    public ArrayList<Long> insertSubDeviceList(List<DataSwitchSubBean> dataSwitchSubBeans) {
        SQLiteDatabase db = this.sys.getWritableDatabase();
        ArrayList<Long> rows = new ArrayList<Long>();
        try {

            db.beginTransaction();
            for (DataSwitchSubBean c : dataSwitchSubBeans) {
                long rowId = insertSubDevice(c,db);
                if (rowId != -1L) {
                    rows.add(rowId);
                }
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
            db.close();
        }
        return rows;
    }

    /**
     * 查找所有报警信息
     * @return
     */
    public List<DataSwitchSubBean> findAllSubDevice(String eqid,String deviceid){
        List<DataSwitchSubBean> list = new ArrayList<DataSwitchSubBean>();
        DataSwitchSubBean nb = null;
        SQLiteDatabase db = this.sys.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from dataswitcheqTable where eqid = '"+eqid+"' and deviceid = '"+deviceid+"' order by subeqid ",null);
        while (cursor.moveToNext()){
            nb = new DataSwitchSubBean();
            nb.setSubid(cursor.getInt(cursor.getColumnIndex("subeqid")));
            nb.setStatus(cursor.getInt(cursor.getColumnIndex("status")));
            nb.setEqid(cursor.getString(cursor.getColumnIndex("eqid")));
            nb.setDeviceid(cursor.getString(cursor.getColumnIndex("deviceid")));
            list.add(nb);
        }
        db.close();
        return list;
    }

    //判断是否有该设备
    public boolean isHasDevice(String eqid,String deviceid,int subid, SQLiteDatabase db){

        DataSwitchSubBean deviceBean = findSubDeviceById(eqid,deviceid,subid,db);
        try {
            if(deviceBean == null){
                return false;
            }else{
                return true;
            }

        }catch (NullPointerException e){
            return false;
        }

    }

    public long insertSubDevice(DataSwitchSubBean noticeBean, SQLiteDatabase db) {

        long result = -1;
        if (noticeBean == null || TextUtils.isEmpty(noticeBean.getDeviceid())|| TextUtils.isEmpty(noticeBean.getEqid())) {
            return -1;
        }
        try {
            ContentValues values = new ContentValues();

            values.put("subeqid", noticeBean.getSubid());
            values.put("status",noticeBean.getStatus());
            values.put("deviceid",noticeBean.getDeviceid());
            values.put("eqid",noticeBean.getEqid());
            if (!isHasDevice(noticeBean.getEqid(),noticeBean.getDeviceid(),noticeBean.getSubid(),db)) {
                result = db.insert("dataswitcheqTable", null, values);
            }else{
                result = db.update("dataswitcheqTable", values, "deviceid = '" + noticeBean.getDeviceid() + "' and eqid = '"+noticeBean.getEqid()+"' and subeqid ="+noticeBean.getSubid(), null);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            return result;
        }

    }

    public long insertSubDevice(DataSwitchSubBean dataSwitchSubBean) {
        SQLiteDatabase db = this.sys.getWritableDatabase();
        long result = -1;
        if (dataSwitchSubBean == null || TextUtils.isEmpty(dataSwitchSubBean.getDeviceid())|| TextUtils.isEmpty(dataSwitchSubBean.getEqid())) {
            return -1;
        }
        try {
            ContentValues values = new ContentValues();

            values.put("subeqid", dataSwitchSubBean.getSubid());
            values.put("status",dataSwitchSubBean.getStatus());
            values.put("deviceid",dataSwitchSubBean.getDeviceid());
            values.put("eqid",dataSwitchSubBean.getEqid());
            if (!isHasDevice(dataSwitchSubBean.getEqid(),dataSwitchSubBean.getDeviceid(),dataSwitchSubBean.getSubid(),db)) {
                result = db.insert("dataswitcheqTable", null, values);
            }else{
                result = db.update("dataswitcheqTable", values, "deviceid = '" + dataSwitchSubBean.getDeviceid() + "' and eqid = '"+dataSwitchSubBean.getEqid()+"' and subeqid = "+dataSwitchSubBean.getSubid(), null);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            return result;
        }

    }


    /**
     * @return DeviceBean
     */
    public DataSwitchSubBean findSubDeviceById(String id,String deviceid,int subid, SQLiteDatabase db){
        DataSwitchSubBean noticeBean = null;
        try {

            Cursor cursor = db.rawQuery("select * from dataswitcheqTable where eqid = '"+id+"' and deviceid = '"+deviceid+"' and subeqid ="+subid,null);
            if(cursor.moveToFirst()) {
                noticeBean = new DataSwitchSubBean();
                noticeBean.setDeviceid(cursor.getString(cursor.getColumnIndex("deviceid")));
                noticeBean.setSubid(cursor.getInt(cursor.getColumnIndex("subeqid")));
                noticeBean.setEqid(cursor.getString(cursor.getColumnIndex("eqid")));
                noticeBean.setStatus(cursor.getInt(cursor.getColumnIndex("status")));
            }
        }catch (NullPointerException e){
             e.printStackTrace();
        }finally {
            return noticeBean;
        }
    }


    /**
     * @param
     */
    public void deleteAllSubdevice()
    {
        SQLiteDatabase db = this.sys.getWritableDatabase();
        try {
            String where = "1 = 1";
            int row = db.delete("dataswitcheqTable", where, null);
        }catch (NullPointerException e){
            e.printStackTrace();
        }finally {
            db.close();
        }
    }

    /**
     * @param
     */
    public void deleteSubdevice(String eqid,String deviceid)
    {
        SQLiteDatabase db = this.sys.getWritableDatabase();
        try {
            String where = "eqid = '"+eqid+"' and deviceid = '"+deviceid+"'";
            int row = db.delete("dataswitcheqTable", where, null);
        }catch (NullPointerException e){
            e.printStackTrace();
        }finally {
            db.close();
        }
    }

    /**
     * @param
     */
    public void deleteOneSubdevice(int subid, String eqid,String deviceid)
    {
        SQLiteDatabase db = this.sys.getWritableDatabase();
        try {
            String where = "subeqid = "+subid+" and eqid = '"+eqid+"' and deviceid = '"+deviceid+"'";
            int row = db.delete("dataswitcheqTable", where, null);
        }catch (NullPointerException e){
            e.printStackTrace();
        }finally {
            db.close();
        }
    }

    public void updateNormal(String deviceid,String eqid,int subeqid, int status){
        SQLiteDatabase db = this.sys.getWritableDatabase();
        String where = "deviceid = ? and eqid = ? and subeqid = ?";
//        String[] whereValue = {Integer.toString(eq.getEquipmentId())};
        String[] whereValue = {deviceid,eqid,String.valueOf(subeqid)};
        ContentValues cv = new ContentValues();
        cv.put("status", status);
        db.update("dataswitcheqTable", cv, where, whereValue);
        db.close();
    }

}
