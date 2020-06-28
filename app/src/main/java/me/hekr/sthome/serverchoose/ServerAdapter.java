package me.hekr.sthome.serverchoose;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.litesuits.android.log.Log;
import java.util.List;

import me.hekr.sthome.R;
import me.hekr.sthome.model.modelbean.SysModelBean;
import me.hekr.sthome.model.modeldb.SysmodelDAO;
import me.hekr.sthome.tools.ConnectionPojo;

/**
 *
 */
public class ServerAdapter extends BaseAdapter {
    private Context mContext;
    private List<SysModelBean> mlists;
    private boolean[] check;



    public ServerAdapter(Context context, List<SysModelBean> lists) {
        this.mContext = context;
        this.mlists = lists;
        initData();
    }

    private void initData() {
        try {
            SysmodelDAO dao = new SysmodelDAO(mContext);
            String sid = dao.findIdByChoice(ConnectionPojo.getInstance().deviceTid).getSid();
            check = new boolean[mlists.size()];
            if(sid != null){
                for(int i =0; i<mlists.size();i++){
                    if( mlists.get(i).getSid().equals(sid)){
                        check[i] = true;
                    }else{
                        check[i] = false;
                    }
                }
            }
        }catch (Exception e){
            Log.i("ceshi","无选中的情景组");
        }


    }

    public int getCount() {
        return mlists.size();
    }
    public Object getItem(int position) {
        return mlists.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder = new ViewHolder();
        Resources r = mContext.getResources();
        SysModelBean ac =mlists.get(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.cell_scence,null);
            holder.image = (TextView) convertView.findViewById(R.id.cellScenceImage);
            holder.text1 = (TextView) convertView.findViewById(R.id.cellScenceName);
            holder.forward = (ImageView) convertView.findViewById(R.id.cellScenceForward);

            holder.del = (ImageView)convertView.findViewById(R.id.dele);
            holder.tv_ico = (LinearLayout)convertView.findViewById(R.id.ddd);
            holder.btn_del = (Button)convertView.findViewById(R.id.shanchu);
            holder.arrow = (ImageView)convertView.findViewById(R.id.arrow);
            holder.touchForward = (LinearLayout)convertView.findViewById(R.id.touchForward);
            holder.gatewaycolor = (ImageView)convertView.findViewById(R.id.gatewaycolor);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
        }

        return convertView;
    }



    class ViewHolder{
        TextView image;
        ImageView forward;
        ImageView gatewaycolor;
        ImageView del;
        TextView text1;
        LinearLayout tv_ico;
        Button btn_del;
        ImageView arrow;
        LinearLayout touchForward;
    }



}