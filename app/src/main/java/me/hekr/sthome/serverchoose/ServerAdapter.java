package me.hekr.sthome.serverchoose;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.List;

import me.hekr.sthome.R;

/**
 *
 */
public class ServerAdapter extends BaseAdapter {
    private Context mContext;
    private List<String> mlists;
    private int check;



    public ServerAdapter(Context context) {
        this.mContext = context;
        mlists = new ArrayList<>();
        mlists.add(this.mContext.getResources().getString(R.string.europea_point));
        mlists.add(this.mContext.getResources().getString(R.string.china_point));
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
         String ac =mlists.get(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.cell_choose_server,null);
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.forward = (ImageView) convertView.findViewById(R.id.chosed);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
        }

        holder.name.setText(ac);
        if(check==position){
            holder.forward.setVisibility(View.VISIBLE);
        }else{
            holder.forward.setVisibility(View.GONE);
        }
        return convertView;
    }



    class ViewHolder{
        TextView name;
        ImageView forward;
    }

    public int getCheck() {
        return check;
    }

    public void setCheck(int check) {
        this.check = check;
        this.notifyDataSetChanged();
    }
}