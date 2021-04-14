package me.hekr.sthome.main;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Matrix;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.google.android.gms.tasks.OnSuccessListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.message.BasicHeader;
import me.hekr.sdk.Constants;
import me.hekr.sdk.utils.LogUtil;
import me.hekr.sthome.CarouselView.IpcViewPager;
import me.hekr.sthome.CarouselView.ViewFactory;
import me.hekr.sthome.CarouselView.WeatherViewPager;
import me.hekr.sthome.CarouselView.WeatherViewPager.WeatherInfo;
import me.hekr.sthome.DeviceListActivity;
import me.hekr.sthome.MyApplication;
import me.hekr.sthome.R;
import me.hekr.sthome.common.CCPAppManager;
import me.hekr.sthome.common.StatusBarUtil;
import me.hekr.sthome.commonBaseView.ECAlertDialog;
import me.hekr.sthome.commonBaseView.MenuDialog;
import me.hekr.sthome.commonBaseView.MultiDirectionSlidingDrawer;
import me.hekr.sthome.commonBaseView.PullListView;
import me.hekr.sthome.equipment.ConfigActivity;
import me.hekr.sthome.event.InitGPSEvent;
import me.hekr.sthome.event.LogoutEvent;
import me.hekr.sthome.event.STEvent;
import me.hekr.sthome.event.ThcheckEvent;
import me.hekr.sthome.event.VideoPagerUpdateEvent;
import me.hekr.sthome.history.GatewayLogoutHistoryAdapter;
import me.hekr.sthome.history.HistoryAdapter;
import me.hekr.sthome.http.HekrUser;
import me.hekr.sthome.http.HekrUserAction;
import me.hekr.sthome.model.modelbean.ClientUser;
import me.hekr.sthome.model.modelbean.EquipmentBean;
import me.hekr.sthome.model.modelbean.MyDeviceBean;
import me.hekr.sthome.model.modelbean.NoticeBean;
import me.hekr.sthome.model.modelbean.SysModelBean;
import me.hekr.sthome.model.modelbean.WeatherInfoBean;
import me.hekr.sthome.model.modeldb.DeviceDAO;
import me.hekr.sthome.model.modeldb.EquipDAO;
import me.hekr.sthome.model.modeldb.NoticeDAO;
import me.hekr.sthome.model.modeldb.SysmodelDAO;
import me.hekr.sthome.tools.Config;
import me.hekr.sthome.tools.ConnectionPojo;
import me.hekr.sthome.tools.ECPreferenceSettings;
import me.hekr.sthome.tools.ECPreferences;
import me.hekr.sthome.tools.MyLocationListener;
import me.hekr.sthome.tools.SendCommand;
import me.hekr.sthome.tools.SendSceneGroupData;
import me.hekr.sthome.tools.SystemTintManager;
import me.hekr.sthome.tools.UnitTools;
import me.hekr.sthome.wheelwidget.helper.Common;


/**
 * Created by xjj on 2016/12/6.
 */

@SuppressLint("ValidFragment")
public class HomeFragment extends Fragment implements View.OnClickListener,
        MultiDirectionSlidingDrawer.OnDrawerOpenListener,
        MultiDirectionSlidingDrawer.OnDrawerCloseListener,
        MenuDialog.Dissmins,
        PullListView.IXListViewListener {

    private static final String DEV_LIST = "device_list";

    private PullListView listView,listView2;
    private View view = null;
    private static final String TAG = HomeFragment.class.getSimpleName();
    private RadioButton title_view;
    private ImageButton setting_btn;
    private RelativeLayout total_linearlayout;
    private LinearLayout topp;
    private LinearLayout alarm_content;
    private LinearLayout nowmode;
    private ImageView btn_cancel;
    private ImageButton btn_clear;
    private MultiDirectionSlidingDrawer drawer;
    private SystemTintManager tintManager;

    private WeatherViewPager weatherViewPager;
    private IpcViewPager ipcViewPager;

    private WeatherInfo weatherInfo = new WeatherInfo();

    private SendSceneGroupData ssgd;
    private int nowmodeindex = -1;
    private MenuDialog menuDialog;
    private SysmodelDAO sysmodelDAO;
    private DeviceDAO deviceDAO;
    private EquipDAO equipDAO;
    private NoticeDAO noticeDAO;
    private List<NoticeBean> reslt;
    private List<Long> reslt2;
    private HistoryAdapter historyAdapter;
    private GatewayLogoutHistoryAdapter gatewayLogoutHistoryAdapter;
    private View empty;
    private View empty2;
    private int page = 0;
    private int page2 = 0;
    private UnitTools unitTools;
    public static LocationClient mLocationClient;
    private MyLocationListener myLocationListener;
    private TextView textView_warn,textView_log;

    private int offset = 0;// 动画图片偏移量
    private int currIndex = 0;// 当前页卡编号
    private int bmpW;// 动画图片宽度
    private ImageView imageView_line;
    private ImageView imageView_log_line;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_home, null);
            EventBus.getDefault().register(this);
            initGuider();
            initGps();
            InitImageView();
        }
        //缓存的rootView需要判断是否已经被加载过parent,如果有parent需要从parent删除，要不然会发生这个rootview已经有parent的错误
        ViewGroup viewparent = (ViewGroup) view.getParent();
        if (viewparent != null) {
            viewparent.removeView(view);
        }

        return view;
    }

    /**
     * 初始化动画，这个就是页卡滑动时，下面的横线也滑动的效果，在这里需要计算一些数据
     */

    private void InitImageView() {
        imageView_line = (ImageView) view.findViewById(R.id.cursor);
        imageView_log_line = (ImageView)view.findViewById(R.id.log_line);
        offset = 0;// 计算偏移量--(屏幕宽度/页卡总数-图片实际宽度)/2
        // = 偏移量
        Matrix matrix = new Matrix();
        matrix.postTranslate(offset, 0);
        imageView_line.setImageMatrix(matrix);// 设置动画初始位置

        textView_warn = (TextView)view.findViewById(R.id.title3);
        textView_log = (TextView)view.findViewById(R.id.title2);
        textView_warn.setOnClickListener(this);
        textView_log.setOnClickListener(this);
        if(isDebugMode()){
            textView_log.setVisibility(View.VISIBLE);
            imageView_log_line.setVisibility(View.VISIBLE);
        }else {
            textView_log.setVisibility(View.GONE);
            imageView_log_line.setVisibility(View.GONE);
        }
    }


    private void initGuider() {
        unitTools = new UnitTools(this.getActivity());
        sysmodelDAO = new SysmodelDAO(this.getActivity());
        deviceDAO = new DeviceDAO(this.getActivity());
        noticeDAO = new NoticeDAO(this.getActivity());
        equipDAO  = new EquipDAO(this.getActivity());
        ssgd = new SendSceneGroupData(this.getActivity()) {
            @Override
            protected void sendEquipmentDataFailed() {

            }

            @Override
            protected void sendEquipmentDataSuccess() {

            }
        };
//        cycleViewPager = new CycleViewPager(getActivity());
        nowmode = view.findViewById(R.id.nowmode);
        nowmode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ConnectionPojo.getInstance().deviceTid == null) {
                    return;
                }

                DeviceDAO DDO = new DeviceDAO(getContext());
                MyDeviceBean gateway = DDO.findByDeviceid(ConnectionPojo.getInstance().deviceTid);
                if (gateway != null && gateway.isOnline()) {
                    SysmodelDAO dao = new SysmodelDAO(HomeFragment.this.getActivity());
                    final List<SysModelBean> listnow = dao.findAllSys(ConnectionPojo.getInstance().deviceTid);
                    MenuDialog.Builder builder = new  MenuDialog.Builder(HomeFragment.this.getActivity());
                    builder.setSysModellist(listnow);
                    builder.setDissmins(HomeFragment.this);
                    menuDialog =  builder.create();
                    menuDialog.show();
                } else {
                    StringBuilder sb = new StringBuilder();
                    sb.append(getString(R.string.current_gateway)).append(" ").append(getString(R.string.off_line));
                    Toast.makeText(getContext(), sb.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });
        tintManager = new SystemTintManager(this.getActivity());// 创建状态栏的管理实例
        drawer =(MultiDirectionSlidingDrawer)view.findViewById(R.id.drawer1);
        listView = (PullListView) drawer.findViewById(R.id.logs);
        listView2 = (PullListView)drawer.findViewById(R.id.logs2);
        empty = view.findViewById(R.id.empty);
        empty2 = view.findViewById(R.id.empty2);
        listView.setEmptyView(empty);
        listView2.setEmptyView(empty2);
        empty.setBackgroundColor(getResources().getColor(R.color.white));
        empty2.setBackgroundColor(getResources().getColor(R.color.white));
        reslt = new ArrayList<>();
        reslt2 = new ArrayList<>();
        historyAdapter = new HistoryAdapter(HomeFragment.this.getActivity(),reslt);
        gatewayLogoutHistoryAdapter = new GatewayLogoutHistoryAdapter(HomeFragment.this.getActivity(),reslt2);
        listView.setAdapter(historyAdapter);
        listView2.setAdapter(gatewayLogoutHistoryAdapter);
        listView.setPullLoadEnable(true);
        listView.setXListViewListener(this);
        drawer.setOnDrawerOpenListener(this);
        drawer.setOnDrawerCloseListener(this);
        listView2.setPullLoadEnable(true);
        listView2.setXListViewListener(new PullListView.IXListViewListener() {
            @Override
            public void onLoadMore() {
                gatewayHistoryShow();
            }
        });

        btn_cancel = (ImageView)view.findViewById(R.id.cancel);
        btn_clear  = (ImageButton)view.findViewById(R.id.clear);
        btn_cancel.setOnClickListener(this);
        btn_clear.setOnClickListener(this);
        title_view = (RadioButton) view.findViewById(R.id.title_home);
        title_view.setOnClickListener(this);

        setting_btn = (ImageButton)view.findViewById(R.id.detailEdit_img);
        setting_btn.setOnClickListener(this);


        //沉浸式设置支持API19
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

            total_linearlayout = (RelativeLayout) view.findViewById(R.id.totl);
            int top = UnitTools.getStatusBarHeight(getActivity());
            total_linearlayout.setPadding(0,top,0,0);
            Log.i(TAG,"top="+top);
            alarm_content = (LinearLayout)view.findViewById(R.id.content);
            alarm_content.setPadding(0,top,0,0);
        }


        refreshSysmode();
        refreshTitle();
        reslt =  noticeDAO.findAllNotice();
        historyAdapter.refreshList(reslt);
        initialize();
        initializeWeather();

    }

    public int getNowmodeindex() {
        return nowmodeindex;
    }

    public void setNowmodeindex(int nowmodeindex) {
        this.nowmodeindex = nowmodeindex;
    }

    public void refreshSysmode(){
        SysModelBean bean = sysmodelDAO.findIdByChoice(ConnectionPojo.getInstance().deviceTid);

        ImageView mode = (ImageView)view.findViewById(R.id.mode);
        TextView mainHome = (TextView) view.findViewById(R.id.zouma);
        mainHome.setSelected(true);
        try {
            if("0".equals(bean.getSid())){
                mode.setImageResource(R.mipmap.home_mode2);
                mainHome.setText(getResources().getString(R.string.home_mode));
            }else if("1".equals(bean.getSid())){
                mode.setImageResource(R.mipmap.out_mode2);
                mainHome.setText(getResources().getString(R.string.out_mode));
            }else if("2".equals(bean.getSid())){
                mode.setImageResource(R.mipmap.sleep_mode2);
                mainHome.setText(getResources().getString(R.string.sleep_mode));
            }else{
                mode.setImageResource(R.mipmap.home_mode2);
                mainHome.setText(bean.getModleName());
            }
        }catch (Exception e){
             LogUtil.i(TAG,"no choosed mode");
        }
    }


    public void closeTheAlarmList(){
        drawer.animateClose();
    }

    public void openTheAlarmList(){
        drawer.open();
    }

    public boolean isAlarmListOpened(){
        return (drawer != null && drawer.isOpened());
    }

    @Override
    public void onResume(){
        super.onResume();


            if(!TextUtils.isEmpty(HekrUserAction.getInstance(this.getActivity()).getJWT_TOKEN())){
            HekrUserAction.getInstance(this.getActivity()).getProfile(new HekrUser.GetProfileListener() {
                @Override
                public void getProfileSuccess(Object object) {
                    try {
                        JSONObject d = JSON.parseObject(object.toString());
                        ClientUser user = CCPAppManager.getClientUser();
                        user.setMonitor(d.getJSONObject("extraProperties").getString("monitor"));
                        CCPAppManager.setClientUser(user);

                        ipcViewPager.updateView();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

                @Override
                public void getProfileFail(int errorCode) {
                    if(errorCode==1){
                        LogoutEvent tokenTimeoutEvent = new LogoutEvent();
                        EventBus.getDefault().post(tokenTimeoutEvent);
                    }
                }
            });
        }

    }



    @Override
    public void onClick(View v) {
          switch (v.getId()){
              case R.id.cancel:
                  drawer.animateClose();
                  break;
              case R.id.title_home:
                  Intent intent = new Intent(HomeFragment.this.getActivity(),DeviceListActivity.class);
                  startActivity(intent);
                  break;
              case R.id.clear:
                  ECAlertDialog ecAlertDialog = ECAlertDialog.buildAlert(this.getActivity(), getResources().getString(R.string.alert_delete_all_history_log), new DialogInterface.OnClickListener() {
                      @Override
                      public void onClick(DialogInterface dialogInterface, int i) {
                          clearNotices();
                      }
                  });
                  ecAlertDialog.show();

                  break;
              case R.id.detailEdit_img:
                  startActivity(new Intent(HomeFragment.this.getActivity(), ConfigActivity.class));
                  break;
              case R.id.title3:
                  if(currIndex == 1){
                      Animation animation = new TranslateAnimation(bmpW * 1, bmpW
                              *0, 0, 0);// 显然这个比较简洁，只有一行代码。
                      animation.setFillAfter(true);// True:图片停在动画结束位置
                      animation.setDuration(300);
                      imageView_line.startAnimation(animation);
                      currIndex = 0;
                      listView.setVisibility(View.VISIBLE);
                      empty.setVisibility(View.VISIBLE);
                      listView2.setVisibility(View.GONE);
                      empty2.setVisibility(View.GONE);
                  }
                  break;
              case R.id.title2:
                  if(currIndex == 0){
                      Animation animation = new TranslateAnimation(bmpW * 0, bmpW
                              * 1, 0, 0);// 显然这个比较简洁，只有一行代码。
                      animation.setFillAfter(true);// True:图片停在动画结束位置
                      animation.setDuration(300);
                      imageView_line.startAnimation(animation);
                      currIndex = 1;
                      listView.setVisibility(View.GONE);
                      empty.setVisibility(View.GONE);
                      listView2.setVisibility(View.VISIBLE);
                      empty2.setVisibility(View.VISIBLE);
                  }
                  break;

              default:
                  break;
          }
    }


    @Override
    public void onDrawerOpened() {
        StatusBarUtil.setStatusBarDarkTheme(HomeFragment.this.getActivity(),true);
        page = 0;
        historyDataShow();

        bmpW = Common.toPx(this.getActivity(),100);
        Animation animation = new TranslateAnimation(bmpW * 1, bmpW
                *0, 0, 0);// 显然这个比较简洁，只有一行代码。
        animation.setFillAfter(true);// True:图片停在动画结束位置
        animation.setDuration(0);
        imageView_line.startAnimation(animation);
        currIndex = 0;
        listView.setVisibility(View.VISIBLE);
        empty.setVisibility(View.VISIBLE);
        listView2.setVisibility(View.GONE);
        empty2.setVisibility(View.GONE);

        if(isDebugMode()){
            page2 = 0;
            gatewayHistoryShow();
            textView_log.setVisibility(View.VISIBLE);
            imageView_log_line.setVisibility(View.VISIBLE);
        }else {
            textView_log.setVisibility(View.GONE);
            imageView_log_line.setVisibility(View.GONE);
        }


    }

    @Override
    public void onDrawerClosed() {
        StatusBarUtil.setStatusBarDarkTheme(HomeFragment.this.getActivity(),false);
    }


    private void historyDataShow() {
        DeviceDAO DDO = new DeviceDAO(this.getActivity());
        MyDeviceBean myDeviceBean = DDO.findByChoice(1);
        if(myDeviceBean!=null){
            HekrUserAction.getInstance(this.getActivity()).getAlarmHistoryList(this.getActivity(), myDeviceBean.getDevTid(), myDeviceBean.getCtrlKey(), myDeviceBean.getProductPublicKey(), page, new HekrUser.GetDeviceHistoryListener() {
                @Override
                public void getSuccess(List<NoticeBean> list, int pagenumber, boolean last) {
                    if(pagenumber == 0){
                        noticeDAO.deleteAllNotice();
                        reslt.clear();
                    }
                    if(!last&&list.size()==20){
                        //防止以前的告警数据造成解析失效
                        page = pagenumber + 1;
                    }

                    reslt.addAll(list);
                    noticeDAO.insertNoticeList(list);
                    historyAdapter.refreshList(reslt);
                    listView.stopLoadMore();
                    if(!last&&list.size()==20)        listView.setPullLoadEnable(true);
                    else listView.setPullLoadEnable(false);
                }

                @Override
                public void getFail(int errorCode) {
                    listView.stopLoadMore();
                    if(reslt.size()>0)        listView.setPullLoadEnable(true);
                    else listView.setPullLoadEnable(false);
                }
            });
        }else {
            Toast.makeText(HomeFragment.this.getActivity(),getResources().getString(R.string.please_choose_device),Toast.LENGTH_LONG).show();
        }


    }

    private void gatewayHistoryShow(){
        DeviceDAO DDO = new DeviceDAO(this.getActivity());
        MyDeviceBean myDeviceBean = DDO.findByChoice(1);
        HekrUserAction.getInstance(this.getActivity()).getLogoutHistory(myDeviceBean.getDevTid(), myDeviceBean.getCtrlKey(), page2, new HekrUser.getLogoutHistoryListener() {
            @Override
            public void getSuccess(List<Long> list, int pagenumber, boolean last) {
                if(pagenumber == 0){
                    reslt2.clear();
                }
                if(!last&&list.size()==20){
                    //防止以前的告警数据造成解析失效
                    page2 = pagenumber + 1;
                }

                reslt2.addAll(list);
                gatewayLogoutHistoryAdapter.refreshList(reslt2);
                listView2.stopLoadMore();
                if(!last&&list.size()==20)        listView2.setPullLoadEnable(true);
                else listView2.setPullLoadEnable(false);
            }

            @Override
            public void getFail(int errorCode) {
                listView2.stopLoadMore();
                if(reslt2.size()>0)        listView2.setPullLoadEnable(true);
                else listView2.setPullLoadEnable(false);
            }
        });
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public void refreshTitle(){
        try {
            MyDeviceBean d = deviceDAO.findByChoice(1);
            String status = d.isOnline()?getResources().getString(R.string.on_line):getResources().getString(R.string.off_line);
            if("报警器".equals(d.getDeviceName())){
                title_view.setText(getResources().getString(R.string.my_home) + " " + status);

            }else{
                title_view.setText(d.getDeviceName() + " " + status);
            }
        }catch (Exception e){
            e.printStackTrace();
            title_view.setText("");
        }
    }


    @SuppressLint("NewApi")
    private void initialize() {
        ipcViewPager = new IpcViewPager((MainActivity) getActivity(), view);
    }

    @SuppressLint("NewApi")
    private void initializeWeather() {
        weatherViewPager = new WeatherViewPager(getActivity(), view, weatherInfo, equipDAO);
    }

    @Override
    public void dmissListener() {
        menuDialog.dismiss();
    }

    @Override
    public void sendListener(int position) {
        final List<SysModelBean> listnow = sysmodelDAO.findAllSys(ConnectionPojo.getInstance().deviceTid);
        setNowmodeindex(Integer.parseInt(listnow.get(position).getSid()));
        SendCommand.Command = SendCommand.CHOOSE_SCENE_GROUP;
        ssgd.sceneGroupChose(nowmodeindex);
    }


    @Subscribe          //订阅事件FirstEvent
    public  void onEventMainThread(STEvent event){
        if(event.getRefreshevent()==1 || event.getRefreshevent() == 6){
             refreshTitle();
        }else if(event.getRefreshevent()==5){
            weatherViewPager.update(weatherInfo);
        }
    }


    @Subscribe          //订阅更新温湿度界面
    public  void onEventMainThread(ThcheckEvent event){
        weatherViewPager.update(weatherInfo);
    }


    @Subscribe          //执行定位初始化
    public  void onEventMainThread(InitGPSEvent event){
        InitLocation();
        initGps();
    }

    @Subscribe
    public  void onEventMainThread(VideoPagerUpdateEvent event){
        ipcViewPager.updateVideo();
    }


    private void clearNotices(){
        String url = Constants.UrlUtil.BASE_USER_URL+"api/v1/notification?"
                          +"ctrlKey="+ ConnectionPojo.getInstance().ctrlKey;
        BasicHeader header = new BasicHeader("X-Hekr-ProdPubKey", ConnectionPojo.getInstance().propubkey);
        HekrUserAction.getInstance(this.getActivity()).deleteHekrData(url,new Header[]{header}, new HekrUserAction.GetHekrDataListener() {
            @Override
            public void getSuccess(Object object) {
                noticeDAO.deleteAllNotice();
                reslt.clear();
                historyAdapter.refreshList(reslt);
                Toast.makeText(HomeFragment.this.getActivity(),getResources().getString(R.string.delete_success),Toast.LENGTH_LONG).show();
            }

            @Override
            public void getFail(int errorCode) {
                if(errorCode==1){
                    LogoutEvent tokenTimeoutEvent = new LogoutEvent();
                    EventBus.getDefault().post(tokenTimeoutEvent);
                }else{
                    Toast.makeText(HomeFragment.this.getActivity(), UnitTools.errorCode2Msg(HomeFragment.this.getActivity(),errorCode),Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    @Override
    public void onLoadMore() {
        historyDataShow();
    }


    @SuppressLint("MissingPermission")
    private void initGps(){
        myLocationListener = new MyLocationListener(this.getActivity()) {

            @Override
            public void logMsg(double lat,double lon) {
                // TODO Auto-generated method stub

                String address = "纬度：" + lat + "经度：" + lon;
                Log.i("ceshi", address);
                try {
                    if ("".equals(weatherInfo.weather_txt)) {

                        Config.getWeatherInfo(HomeFragment.this.getActivity(), new HekrUser.LoginListener() {
                            @Override
                            public void loginSuccess(String str) {
                                Log.i("ceshi", "天气数据:" + str);
                                try {
                                    JSONObject jsonObject = JSONObject.parseObject(str);
                                    JSONArray jsonArray = jsonObject.getJSONArray("weather");
                                    String weather = jsonArray.getJSONObject(0).getString("description");
                                    String weather_ico = jsonArray.getJSONObject(0).getString("icon");
                                    int hum = jsonObject.getJSONObject("main").getInteger("humidity");
                                    double temp_hua = jsonObject.getJSONObject("main").getDouble("temp");

                                    weatherInfo.weather_txt = weather;
                                    weatherInfo.gpshum = ""+hum;
                                    weatherInfo.temp = String.valueOf((int) ((temp_hua - 273)));
                                    weatherInfo.gpsweather_ico = weather_ico;
                                    weatherViewPager.update(weatherInfo);


                                } catch (Exception e) {
                                    e.printStackTrace();
                                }


                            }

                            @Override
                            public void loginFail(int errorCode) {
                                Log.i("ceshi", "errorCode:" + errorCode);
                            }
                        }, "http://api.openweathermap.org/data/2.5/weather?lat=" +
                                lat + "&lon=" + lon
                                + "&appid=b45eb4739891c226b7a36613ce3d1dbd&lang=" + unitTools.readLanguage());


                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        };
        mLocationClient.registerLocationListener(myLocationListener);
        mLocationClient.start();
    }


    private boolean isDebugMode(){

        SharedPreferences sharedPreferences = ECPreferences.getSharedPreferences();
        ECPreferenceSettings flag = ECPreferenceSettings.SETTINGS_DEBUG;
        boolean autoflag = sharedPreferences.getBoolean(flag.getId(), (boolean) flag.getDefaultValue());
        return autoflag;
    }


    private void InitLocation(){
        Log.i(TAG,"初始化了定位了哦");
        mLocationClient = new LocationClient(this.getContext());
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//设置定位模式
        option.setCoorType("gcj02");//返回的定位结果是百度经纬度，默认值gcj02
        option.setScanSpan(36000000);//设置发起定位请求的间隔时间为1000ms
        option.setIsNeedAddress(true);
        HomeFragment.mLocationClient.setLocOption(option);
    }

    public void refreshDeviceView(){
        if(ipcViewPager!=null){
            ipcViewPager.updateView();
        }
    }
}
