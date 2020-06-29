package me.hekr.sthome.serverchoose;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.List;

import me.hekr.sdk.Hekr;
import me.hekr.sdk.HekrSDK;
import me.hekr.sdk.utils.CacheUtil;
import me.hekr.sthome.InitActivity;
import me.hekr.sthome.LoginActivity;
import me.hekr.sthome.R;
import me.hekr.sthome.common.TopbarSuperActivity;
import me.hekr.sthome.main.MainActivity;
import me.hekr.sthome.tools.SiterSDK;


public class ChooseServerActivity extends TopbarSuperActivity implements AdapterView.OnItemClickListener {
    private ListView sListView;
    private ServerAdapter msAdapter;

    @Override
    protected void onCreateInit() {
        sListView = (ListView) findViewById(R.id.eqlist);
        msAdapter = new ServerAdapter(this);
        sListView.setAdapter(msAdapter);
        sListView.setOnItemClickListener(this);
        getTopBarView().setTopBarStatus(1, 2, getResources().getString(R.string.choose_server), getResources().getString(R.string.ok), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String d = "";
                if(msAdapter.getCheck()==0){
                    d = "hekreu.me";

                }else{
                    d = "hekr.me";
                }
                CacheUtil.putString(SiterSDK.SETTINGS_CONFIG_REGION,d);
                HekrSDK.setOnlineSite(d);
                startActivity(new Intent(ChooseServerActivity.this, LoginActivity.class));
                finish();
            }
        });
        getTopBarView().getBackView().setVisibility(View.GONE);
        initChose();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_server_list;
    }

    private void initChose(){
       String de = CacheUtil.getString(SiterSDK.SETTINGS_CONFIG_REGION,"");
       if(TextUtils.isEmpty(de)){
           msAdapter.setCheck(0);
       }else{
           if(de.contains("hekreu.me")){
               msAdapter.setCheck(0);
           }else{
               msAdapter.setCheck(1);
           }
       }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        msAdapter.setCheck(i);
    }
}
