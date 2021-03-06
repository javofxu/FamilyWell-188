package me.hekr.sthome.model;

import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.text.TextUtils;

import com.litesuits.android.log.Log;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import me.hekr.sthome.DragFolderwidget.ApplicationInfo;
import me.hekr.sthome.crc.CoderUtils;
import me.hekr.sthome.model.modelbean.DataSwitchSubBean;
import me.hekr.sthome.model.modelbean.DataSwitchType;
import me.hekr.sthome.model.modelbean.EquipmentBean;
import me.hekr.sthome.model.modelbean.SceneBean;
import me.hekr.sthome.model.modelbean.ShortcutBean;
import me.hekr.sthome.model.modelbean.SysModelBean;
import me.hekr.sthome.model.modelbean.TimerGatewayBean;
import me.hekr.sthome.model.modeldb.DataSwitchSubDAO;
import me.hekr.sthome.model.modeldb.EquipDAO;
import me.hekr.sthome.model.modeldb.PackDAO;
import me.hekr.sthome.model.modeldb.SceneDAO;
import me.hekr.sthome.model.modeldb.ShortcutDAO;
import me.hekr.sthome.model.modeldb.SysmodelDAO;
import me.hekr.sthome.model.modeldb.TimerDAO;
import me.hekr.sthome.tools.ByteUtil;
import me.hekr.sthome.tools.ConnectionPojo;
import me.hekr.sthome.tools.NameSolve;

/**
 * Created by Administrator on 2017/6/30.
 */

/*
@class ResolveData
@autor Administrator
@time 2017/6/30 18:59
@email xuejunju_4595@qq.com
协议解析类，负责数据解析
*/
public abstract class ResolveData {

    private static final String TAG  = "ResolveData";

    private  boolean sync_scene = false;
    private HashMap<Integer,String> GroupSceneMode;
    private HashMap<Integer,String> Scenedata;
    private int now_mode;
    private Context context;

    public ResolveData(Context context) {
        GroupSceneMode =  new HashMap<>();
        Scenedata = new HashMap<>();
        now_mode = -1;
        this.context = context;
    }



    public abstract void EndSyncScene();  //同步情景结束
    public abstract void EndSyncEq();    //同步设备状态结束
    public abstract void EndSyncEqName();//同步设备名称结束
    public abstract void EndSyncTimerInfo();//同步定时信息结束
    public abstract void Getinfolinstenr();  //同步时收取数据监听，为了计时判断同步超时;
    public abstract void reFreshCurrentMode();  //实时刷新当前模式;
    //开始同步情景
    public void StartSyncScene(){
        setSync_scene(true);
       if(GroupSceneMode!=null) GroupSceneMode.clear();
        if(Scenedata!=null) Scenedata.clear();
        now_mode = -1;
    }


    public boolean isSync_scene() {
        return sync_scene;
    }

    public void setSync_scene(boolean sync_scene) {
        this.sync_scene = sync_scene;
    }

    public int getNow_mode() {
        return now_mode;
    }

    public void setNow_mode(int now_mode,String deviceid) {
        this.now_mode = now_mode;
        if(isSync_scene()){
            Getinfolinstenr();
        }else{
            SysmodelDAO sysmodelDAO = new SysmodelDAO(context);
            sysmodelDAO.updateChoice(String.valueOf(now_mode),deviceid);
            reFreshCurrentMode();
        }

    }

    //同步获取情景模式组信息到hashmap
    public void putGroupMode(int group_id,String scene_content){
        try {
            GroupSceneMode.put(group_id,scene_content);
            Getinfolinstenr();
        }catch (Exception e){
            Log.i(TAG,"Groupdata is null");
        }
    }

    //同步获取情景列表信息到hashmap
    public void putScene(int mid,String scene_content){
        try {
            Scenedata.put(mid,scene_content);
            Getinfolinstenr();
        }catch (Exception e){
            Log.i(TAG,"Groupdata is null");
        }
    }

    //情景同步完后解析并操作数据库
    public void setScenes(String deviceid){
        SceneDAO sceneDAO = new SceneDAO(context);
        SysmodelDAO sysmodelDAO = new SysmodelDAO(context);
      if(isSync_scene()){
          Log.i(TAG,"Scenedata:=="+Scenedata.toString());
          Object[] key_arr = Scenedata.keySet().toArray();
          Arrays.sort(key_arr);
          for  (Object key : key_arr) {
              String value = Scenedata.get(key);
              String codeDetele = value.substring(0,4);
              if(value.length()>=11){
                  try {
                      String myname = value.substring(6,38);
                      String name = CoderUtils.getStringFromAscii(myname);
                      if(name!=null){
                          SceneBean bean = new SceneBean();
                          bean.setSid("-1");
                          bean.setMid(String.valueOf(key));
                          bean.setCode(value);
                          bean.setName(name);
                          bean.setDeviceid(deviceid);

                          List<SceneBean> sb = sceneDAO.findScenceListBySidAndMid(bean);
                          if(sb != null && sb.size()>0){
                              sceneDAO.updateByMid(bean);
                          }else {
                              sceneDAO.addScence(bean);
                          }
                      }
                  }catch (NumberFormatException e){
                      Log.i(TAG,"data is not Number");
                  }
              }else if("0000".equals(codeDetele)){

                  String mid = String.valueOf(Integer.parseInt(value.substring(4,6),16));

                  sceneDAO.deleteByMid(mid,deviceid);
              }


          }

          setSceneGroups(deviceid);
          if(now_mode>=0) sysmodelDAO.updateChoice(String.valueOf(now_mode),deviceid);
          now_mode = -1;
          setSync_scene(false);
          EndSyncScene();
      }else{
          Log.i(TAG,"is not Sync_scene");
      }

    }

    /*
    @method setSceneGroups
    @autor Administrator
    @time 2017/6/30 19:02
    @email xuejunju_4595@qq.com
    解析情景组
    */
    protected void setSceneGroups(String deviceid){
        SysmodelDAO sysmodelDAO = new SysmodelDAO(context);
        ShortcutDAO shortcutDAO = new ShortcutDAO(context);
        SceneDAO sceneDAO = new SceneDAO(context);
        Object[] key_arr2 = GroupSceneMode.keySet().toArray();
        Arrays.sort(key_arr2);
        Log.i(TAG,"setSceneGroups:=="+GroupSceneMode.toString());
        for  (Object key : key_arr2) {
            String value = GroupSceneMode.get(key);
            String codeDetele = value.substring(0,4);
            byte default_scene_code = 0;

            try {
                if(value.length()>=32){
                    String initname = value.substring(6,38);
                    int modeNo = Integer.parseInt(value.substring(38,40),16);
                    int listNo = Integer.parseInt(value.substring(40,42),16);
                    int totalLength = 42 + modeNo * 14 +  listNo * 2;
                    String color = "F"+ key;
                    if(value.length()>totalLength){
                        if("F".equals(value.substring(totalLength,totalLength+1))){
                            color = value.substring(totalLength,totalLength+2);
                        }
                        if(value.length()==(totalLength+4)){
                            String scene_code = value.substring(totalLength+2,totalLength+4);
                            default_scene_code = ByteUtil.hexStr2Bytes(scene_code)[0];
                        }
                    }
                    Log.i(TAG,"initname:=="+initname);
                    String name =  CoderUtils.getStringFromAscii(initname);
                    if(name != null){
                        SysModelBean bean = new SysModelBean();
                        bean.setSid(key.toString());
                        bean.setModleName(name);
                        bean.setChice("N");
                        bean.setColor(color);
                        bean.setDeviceid(deviceid);
                        sysmodelDAO.insertSysmodel(bean);
                        convertGroupData(deviceid,key.toString(),value,default_scene_code);
                    }
                }else if("0000".equals(codeDetele)){
                    String sid = value.substring(4,6);
                    if(Integer.parseInt(sid,16)>2){
                        sysmodelDAO.delete(String.valueOf(Integer.parseInt(sid)),deviceid);
                    }else{
                        sysmodelDAO.updateColor(String.valueOf(Integer.parseInt(sid)),"F"+Integer.parseInt(sid),deviceid);
                    }
                    shortcutDAO.deleteAllShortcurt(String.valueOf(Integer.parseInt(sid)),deviceid);

                    SceneBean bean1 = new SceneBean();
                    bean1.setMid("129");
                    bean1.setSid(String.valueOf(Integer.parseInt(sid)));
                    sceneDAO.deleteBySidAndMid(bean1,deviceid);
                    SceneBean bean2 = new SceneBean();
                    bean2.setMid("130");
                    bean2.setSid(String.valueOf(Integer.parseInt(sid)));
                    sceneDAO.deleteBySidAndMid(bean2,deviceid);
                    SceneBean bean3 = new SceneBean();
                    bean3.setMid("131");
                    bean3.setSid(String.valueOf(Integer.parseInt(sid)));
                    sceneDAO.deleteBySidAndMid(bean3,deviceid);

                }
            }catch (Exception e){
                Log.i(TAG,"data is null");
            }


        }
    }

    /*
    @method convertGroupData
    @autor Administrator
    @time 2017/6/30 19:02
    @email xuejunju_4595@qq.com
    解析情景信息
    */
    private void convertGroupData(String deviceid,String group, @NotNull String code,byte default_scene_code){
        SceneDAO sceneDAO  = new SceneDAO(context);
        ShortcutDAO shortcutDAO = new ShortcutDAO(context);
        String buf = code;
//        if(code.length()>=32 && (code.length()%2)==0){ //old
        if(code.length()>=38 && (code.length()%2)==0){
            sceneDAO.deleteBySid(Integer.parseInt(group),deviceid);
//            String newcode = buf.substring(32);  //old

            int length_mode = Integer.parseInt(buf.substring(38,40),16);
            String newcode_mode = buf.substring(38+4,38+4+length_mode*14);
            for(int i=0;i<length_mode;i++){
                String data =  newcode_mode.substring(0,14);
                newcode_mode = newcode_mode.substring(14);

                String eqid =String.valueOf(Integer.parseInt(data.substring(0,4),16));
                String content =  String.valueOf(Integer.parseInt(data.substring(4,6),16));
                Log.i(TAG,"同步得到快捷键关联:eqid"+eqid+"  content:"+content+"  deviceid:"+deviceid+"  sid:"+group);
                ShortcutBean shortcutBean = new ShortcutBean();
                shortcutBean.setEqid(eqid);
                shortcutBean.setDes_sid(content);
                shortcutBean.setDelay(0);
                shortcutBean.setSrc_sid(group);
                shortcutBean.setDeviceid(deviceid);
                shortcutDAO.insertShortcut(shortcutBean);
            }

            int length = Integer.parseInt(buf.substring(40,42),16);
            String newcode = buf.substring(38+4+length_mode*14);
            for(int i=0;i<length;i++){
                String data =  newcode.substring(0,2);
                int ds = Integer.parseInt(data,16);
                newcode = newcode.substring(2);
                try {
                    SceneBean bean = sceneDAO.findScenceBymid(String.valueOf(ds),deviceid);
                    bean.setSid(group);
                    List<SceneBean> sb = sceneDAO.findScenceListBySidAndMid(bean);
                    if(sb != null && sb.size() == 0){
                        sceneDAO.addScence(bean);
                    }
                }catch (NullPointerException e){
                    sceneDAO.deleteByMid(String.valueOf(ds),deviceid);
                }



            }

            if((default_scene_code & 0x01)!=0){
                SceneBean bean2 =new SceneBean();
                bean2.setName("0");
                bean2.setMid("129");
                bean2.setCode("0");
                bean2.setSid(group);
                bean2.setDeviceid(deviceid);
                List<SceneBean> sb = sceneDAO.findScenceListBySidAndMid(bean2);
                if(sb != null && sb.size() == 0){
                    sceneDAO.addScence(bean2);
                }
            }else{
                SceneBean bean2 =new SceneBean();
                bean2.setMid("129");
                bean2.setSid(group);
                sceneDAO.deleteBySidAndMid(bean2,deviceid);
            }

            if((default_scene_code & 0x02)!=0){
                SceneBean bean2 =new SceneBean();
                bean2.setName("0");
                bean2.setMid("130");
                bean2.setCode("0");
                bean2.setSid(group);
                bean2.setDeviceid(deviceid);
                List<SceneBean> sb = sceneDAO.findScenceListBySidAndMid(bean2);
                if(sb != null && sb.size() == 0){
                    sceneDAO.addScence(bean2);
                }
            }else{
                SceneBean bean2 =new SceneBean();
                bean2.setMid("130");
                bean2.setSid(group);
                sceneDAO.deleteBySidAndMid(bean2,deviceid);
            }

            if((default_scene_code & 0x04)!=0){
                SceneBean bean2 =new SceneBean();
                bean2.setName("0");
                bean2.setMid("131");
                bean2.setCode("0");
                bean2.setSid(group);
                bean2.setDeviceid(deviceid);
                List<SceneBean> sb = sceneDAO.findScenceListBySidAndMid(bean2);
                if(sb != null && sb.size() == 0){
                    sceneDAO.addScence(bean2);
                }
            }else{
                SceneBean bean2 =new SceneBean();
                bean2.setMid("131");
                bean2.setSid(group);
                sceneDAO.deleteBySidAndMid(bean2,deviceid);
            }


        }else{
            Log.i(TAG,"data is illegal!");

        }
    }

    /*
    @method updateEq
    @autor Administrator
    @time 2017/6/30 19:02
    @email xuejunju_4595@qq.com
    解析设备信息
    */
    public void updateEq(String devid,String eqid,String devtype, String status){

        ShortcutDAO shortcutDAO = new ShortcutDAO(context);
        EquipDAO equipDAO = new EquipDAO(context);
        DataSwitchSubDAO dataSwitchSubDAO = new DataSwitchSubDAO(context);
        try {
            if("STATUES".equals(devtype)){
                EndSyncEq();
            }else{
                Getinfolinstenr();
                ApplicationInfo applicationInfo = new ApplicationInfo();
                applicationInfo.setDeviceid(devid);
                applicationInfo.setEqid(eqid);
                applicationInfo.setEquipmentDesc(devtype);
                applicationInfo.setState(status);
                int i = equipDAO.isIdExists(eqid,devid);

                //判断是添加还是更新
                if(i<1){
                    applicationInfo.setPackId(ConnectionPojo.getInstance().folderid);
                    applicationInfo.setOrder(Integer.parseInt(eqid));
                    applicationInfo.setEquipmentName("");
                    equipDAO.addEq(applicationInfo);
                }else{
                    if("DEL".equals(devtype)&&"00000000".equals(status)){
                        Log.i(TAG," ED.deleteByEqid(eq1);"+status);
                        equipDAO.deleteByEqid(applicationInfo);
                        shortcutDAO.deleteShortcurtByEqid(applicationInfo.getDeviceid(),applicationInfo.getEqid());
                        dataSwitchSubDAO.deleteSubdevice(applicationInfo.getEqid(),applicationInfo.getDeviceid());
                    }else{
                        equipDAO.update(applicationInfo);
                    }
                }

                if(NameSolve.DATA_SWITCH.equals(NameSolve.getEqType(applicationInfo.getEquipmentDesc()))){
                    if(applicationInfo.getState().length()>=10){
                        String d = applicationInfo.getState();
                        int statusa = Integer.parseInt(d.substring(9,10),16);
                        DataSwitchType dataSwitchType = DataSwitchType.getType(statusa);
                        if(dataSwitchType == DataSwitchType.STATUS_DEVICE_DELETE){
                            int subeqid = Integer.parseInt(d.substring(4,9),16);
                            dataSwitchSubDAO.deleteOneSubdevice(subeqid,eqid,devid);
                        }
                        else if(statusa==0){
                            int subeqid = Integer.parseInt(d.substring(4,9),16);
                            if(subeqid==0){
                                List<DataSwitchSubBean> list = dataSwitchSubDAO.findAllSubDevice(eqid,devid);
                                for(DataSwitchSubBean dataSwitchSubBean:list){
                                    if(dataSwitchSubBean.getStatus()==DataSwitchType.STATUS_FIRE_ALARMER_WARN.getStatusId()
                                            ||dataSwitchSubBean.getStatus()==DataSwitchType.STATUS_FIRE_ALARMER_LOW_VOLTAGE.getStatusId()){
                                        dataSwitchSubDAO.updateNormal(dataSwitchSubBean.getDeviceid(),dataSwitchSubBean.getEqid(),dataSwitchSubBean.getSubid(),DataSwitchType.STATUS_FIRE_ALARMER_NORMAL.getStatusId());
                                    }else if(dataSwitchSubBean.getStatus()==DataSwitchType.STATUS_GAS_ALARMER_WARN.getStatusId()){
                                        dataSwitchSubDAO.updateNormal(dataSwitchSubBean.getDeviceid(),dataSwitchSubBean.getEqid(),dataSwitchSubBean.getSubid(),DataSwitchType.STATUS_GAS_ALARMER_NORMAL.getStatusId());
                                    }else if(dataSwitchSubBean.getStatus()==DataSwitchType.STATUS_WATER_ALARMER_WARN.getStatusId()){
                                        dataSwitchSubDAO.updateNormal(dataSwitchSubBean.getDeviceid(),dataSwitchSubBean.getEqid(),dataSwitchSubBean.getSubid(),DataSwitchType.STATUS_WATER_ALARMER_NORMAL.getStatusId());
                                    }

                                }

                            }
                        }
                        else if(dataSwitchType!=DataSwitchType.STATUS_UNKNOWN){
                            DataSwitchSubBean dataSwitchSubBean = new DataSwitchSubBean();
                            dataSwitchSubBean.setDeviceid(devid);
                            dataSwitchSubBean.setEqid(eqid);
                            int subeqid = Integer.parseInt(d.substring(4,9),16);
                            dataSwitchSubBean.setSubid(subeqid);
                            dataSwitchSubBean.setStatus(statusa);
                            dataSwitchSubDAO.insertSubDevice(dataSwitchSubBean);
                        }
                    }else if(applicationInfo.getState().length()==8){
                        if(applicationInfo.getState().endsWith("0000")){
                            List<DataSwitchSubBean> list = dataSwitchSubDAO.findAllSubDevice(eqid,devid);
                            for(DataSwitchSubBean dataSwitchSubBean:list){
                                if(dataSwitchSubBean.getStatus()==DataSwitchType.STATUS_FIRE_ALARMER_WARN.getStatusId()
                                        ||dataSwitchSubBean.getStatus()==DataSwitchType.STATUS_FIRE_ALARMER_LOW_VOLTAGE.getStatusId()){
                                    dataSwitchSubDAO.updateNormal(dataSwitchSubBean.getDeviceid(),dataSwitchSubBean.getEqid(),dataSwitchSubBean.getSubid(),DataSwitchType.STATUS_FIRE_ALARMER_NORMAL.getStatusId());
                                }else if(dataSwitchSubBean.getStatus()==DataSwitchType.STATUS_GAS_ALARMER_WARN.getStatusId()){
                                    dataSwitchSubDAO.updateNormal(dataSwitchSubBean.getDeviceid(),dataSwitchSubBean.getEqid(),dataSwitchSubBean.getSubid(),DataSwitchType.STATUS_GAS_ALARMER_NORMAL.getStatusId());
                                }else if(dataSwitchSubBean.getStatus()==DataSwitchType.STATUS_WATER_ALARMER_WARN.getStatusId()){
                                    dataSwitchSubDAO.updateNormal(dataSwitchSubBean.getDeviceid(),dataSwitchSubBean.getEqid(),dataSwitchSubBean.getSubid(),DataSwitchType.STATUS_WATER_ALARMER_NORMAL.getStatusId());
                                }

                            }
                        }
                    }

                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public ApplicationInfo updateEqFromPush(String info, String deviceid){

        try {
            String eqid     = String.valueOf(Integer.parseInt(info.substring(6,10),16));
            String device_type   = info.substring(10,14);
            String device_status = info.substring(14,22);
            if(NameSolve.DATA_SWITCH.equals(NameSolve.getEqType(device_type))){
                device_status = info.substring(14,24);
            }

            ApplicationInfo applicationInfo = new ApplicationInfo();
            applicationInfo.setDeviceid(deviceid);
            applicationInfo.setEqid(eqid);
            applicationInfo.setEquipmentDesc(device_type);
            applicationInfo.setState(device_status);

            return applicationInfo;
        }catch (Exception e){
            return null;
        }


    }




    public void resolveEqName(String info, String deviceid){

         try {
             EquipDAO equipDAO = new EquipDAO(context);
             if("NAME_OVER".equals(info)){
                 EndSyncEqName();
             }else{
                 Getinfolinstenr();
                 String eqid = info.substring(0, 4);
                 int ds = Integer.parseInt(eqid, 16);
                 String value = info.substring(4);
                 if (value.length() == 32) {
                     String lastname = CoderUtils.getStringFromAscii(value);
                     Log.i(TAG, "name+++++" + lastname);

                     try {
                         EquipmentBean equipmentBean = new EquipmentBean();
                         equipmentBean.setEqid(String.valueOf(ds));
                         equipmentBean.setEquipmentName(lastname);
                         equipmentBean.setDeviceid(deviceid);
                         equipDAO.updateName(equipmentBean);
                     } catch (SQLiteConstraintException e) {
                         Log.i(TAG, "name is repeat");
                     }


                 }else{
                     Log.i(TAG, "name format is error");
                     try {
                         EquipmentBean equipmentBean = new EquipmentBean();
                         equipmentBean.setEqid(String.valueOf(ds));
                         equipmentBean.setEquipmentName("");
                         equipmentBean.setDeviceid(deviceid);
                         equipDAO.updateName(equipmentBean);
                     } catch (SQLiteConstraintException e) {
                         Log.i(TAG, "name is repeat");
                     }
                 }
             }
         }catch (Exception e){
             e.printStackTrace();
         }
    }

    public void resolveTimer(String timerinfo,String deviceid){

        TimerDAO timerDAO = new TimerDAO(context);
            if("TIMER_OVER".equals(timerinfo)){
               EndSyncTimerInfo();
            }else{
                if(timerinfo.indexOf("DEL")!=-1){
                    String id = timerinfo.substring(0,2);
                    int timerid = Integer.parseInt(id,16);
                    timerDAO.delete(String.valueOf(timerid), deviceid);
                }
                else{
                    ResolveTimer resolveTimer = new ResolveTimer(timerinfo,deviceid);
                    if(resolveTimer.isTarget()){
                        TimerGatewayBean timerGatewayBean = resolveTimer.getTimerGatewayBean();
                        timerDAO.insertTimer(timerGatewayBean);
                    }else{
                        Log.i(TAG,"timer data format is error");
                    }

                }

            }

    }

    public void insertDataSwitchList(List<DataSwitchSubBean> list){

        DataSwitchSubDAO dataSwitchSubDAO = new DataSwitchSubDAO(context);
        if(list!=null && list.size()>0){
            String eqid = list.get(0).getEqid();
            String deviceid = list.get(0).getDeviceid();
            dataSwitchSubDAO.deleteSubdevice(eqid,deviceid);
        }
        dataSwitchSubDAO.insertSubDeviceList(list);
    }
}
