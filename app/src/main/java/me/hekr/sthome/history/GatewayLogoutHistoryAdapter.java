package me.hekr.sthome.history;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import me.hekr.sthome.R;
import me.hekr.sthome.model.modelbean.NoticeBean;

/**
 * Created by ST-020111 on 2017/4/1.
 */

public class GatewayLogoutHistoryAdapter extends BaseAdapter {
    private Context mContext;
    private List<Long> mLists;
    private String dataFormat = "yyyy-MM-dd HH:mm:ss";
    private SimpleDateFormat sdf = new SimpleDateFormat(dataFormat);


    public GatewayLogoutHistoryAdapter(Context context, List<Long> lists ){
        this.mContext =context;
        this.mLists = lists;
    }

    public int getCount(){
        return mLists.size();
    }



    public Long getItem(int position){
        return mLists.get(position);
    }

    public long getItemId(int position){
        return position;
    }
    public View getView(int position, View convertView, ViewGroup parent){
        View tv = null;
        GatewayLogoutHistoryAdapter.ViewHolder holder;
        if(convertView == null){
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.cell_alarm_message, parent,
                    false);
            holder = new GatewayLogoutHistoryAdapter.ViewHolder();
            holder.inName = (TextView)convertView.findViewById(R.id.name);
            holder.activitytime = (TextView)convertView.findViewById(R.id.time);
            convertView.setTag(holder);
        }else{
            tv = convertView;
            holder = (GatewayLogoutHistoryAdapter.ViewHolder) tv.getTag();
        }
        try {
            Date date = new Date(mLists.get(position));
            holder.activitytime.setText(sdf.format(date));
            holder.inName.setText("网关离线");
        }catch (Exception e){
            e.printStackTrace();
        }


        return convertView;
    }


    private class ViewHolder {

        TextView inName;
        TextView activitytime;
    }

    public void refreshList(List<Long> mlists){
        this.mLists = mlists;
        notifyDataSetChanged();
    }
}
