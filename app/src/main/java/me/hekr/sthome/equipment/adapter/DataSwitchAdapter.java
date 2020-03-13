package me.hekr.sthome.equipment.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

import me.hekr.sthome.R;
import me.hekr.sthome.model.modelbean.DataSwitchSubBean;
import me.hekr.sthome.model.modelbean.DataSwitchType;
import me.hekr.sthome.model.modelbean.EquipmentBean;
import me.hekr.sthome.model.modelbean.ShortcutBean;
import me.hekr.sthome.model.modelbean.SysModelBean;
import me.hekr.sthome.model.modeldb.ShortcutDAO;
import me.hekr.sthome.model.modeldb.SysmodelDAO;

/**
 * Created by jishu0001 on 2016/10/21.
 */
public class DataSwitchAdapter extends BaseAdapter {
    private Context context;
    private List<DataSwitchSubBean> lists;
    private ViewHolder holder;

    public DataSwitchAdapter(Context context, List<DataSwitchSubBean> lists){
        this.context = context;
        this.lists = lists;
    }

    private class ViewHolder {
        ImageView src_imageView;
        TextView src_textView;
        TextView index_textView;
    }
    public Context getContext(){
        return context;
    }

    public void add(DataSwitchSubBean eq){
        lists.add(eq);
    }

    public void remove(String eq){
        lists.remove(eq);
    }

    @Override
    public int getCount() {
        return lists.size();
    }

    @Override
    public DataSwitchSubBean getItem(int position) {
        return lists.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        holder = null;
        final DataSwitchSubBean eq = lists.get(position);
        if( convertView == null){
            holder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.cell_data_switch,null);
            holder.src_imageView= (ImageView) convertView.findViewById(R.id.cellScenceImage);
            holder.src_textView = (TextView) convertView.findViewById(R.id.cellScenceName);
            holder.index_textView = (TextView)convertView.findViewById(R.id.index);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        holder.src_imageView.setImageResource(DataSwitchType.getType(eq.getStatus()).getDrawResId());
        holder.src_textView.setText(DataSwitchType.getType(eq.getStatus()).getStrId());
        String dsa = Integer.toHexString(eq.getSubid());
        int leng = dsa.length();
        for(int i=0;i<5-leng;i++){
            dsa = "0"+dsa;
        }
        holder.index_textView.setText(dsa.toUpperCase());
        return convertView;
    }

    public void refresh(List<DataSwitchSubBean> lists){
        this.lists = lists;
        notifyDataSetChanged();
    }
}