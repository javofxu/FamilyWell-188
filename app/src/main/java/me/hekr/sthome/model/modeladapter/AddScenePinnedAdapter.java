package me.hekr.sthome.model.modeladapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;


import java.util.List;

import me.hekr.sthome.R;
import me.hekr.sthome.commonBaseView.PinnedHeaderListView;
import me.hekr.sthome.model.modelbean.SceneBean;

public class AddScenePinnedAdapter extends BaseAdapter implements PinnedHeaderListView.PinnedHeaderAdapter, OnScrollListener {
    private Context mContext;
    private List<SceneBean> mScenelist;
    private ListView mListView;
    private SceneSectionIndexer mIndexer;
    private int mLocationPosition = -1;


    public AddScenePinnedAdapter(Context context, List<SceneBean> list, SceneSectionIndexer indexer, ListView listView) {
        this.mContext = context;
        this.mScenelist = list;
        this.mIndexer = indexer;
        this.mListView = listView;
    }


    @Override
    public int getCount() {
        return mScenelist.size();
    }

    @Override
    public SceneBean getItem(int position) {
        return mScenelist.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Holder holder;
        if (convertView == null) {
            holder = new Holder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.listitem_add_scene, null);
            holder.titleTextView = (TextView) convertView.findViewById(R.id.header_text);
            holder.checkBox = (ImageView)convertView.findViewById(R.id.checkboxo);
            holder.inName = (TextView)convertView.findViewById(R.id.cellScenceName);
            holder.numb = (TextView)convertView.findViewById(R.id.cellScenceImage);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }
        final SceneBean city = mScenelist.get(position);
        // 显示字母
        int section = mIndexer.getSectionForPosition(position);
        if (mIndexer.getPositionForSection(section) == position) {
            holder.titleTextView.setVisibility(View.VISIBLE);
            if (position == 0) {
                holder.titleTextView.setText(mContext.getResources().getString(R.string.default_scene));
            } else {
                holder.titleTextView.setText(mContext.getResources().getString(R.string.scene_list));
            }
        } else {
            holder.titleTextView.setVisibility(View.GONE);
        }
        if("129".equals(city.getMid())){
            holder.inName.setText(mContext.getResources().getString(R.string.pir_default_scene));
        }else if("130".equals(city.getMid())){
            holder.inName.setText(mContext.getResources().getString(R.string.door_default_scene));
        }else if("131".equals(city.getMid())){
            holder.inName.setText(mContext.getResources().getString(R.string.old_man_default_scene));
        }else{
            holder.inName.setText(city.getName());
        }
        holder.inName.setSelected(true);
        holder.numb.setText( position<9?("0"+String.valueOf(position+1)):String.valueOf(position+1));
        updataBackground(position,holder.checkBox);
        return convertView;
    }

    private class Holder {
        ImageView checkBox;
        TextView inName;
        TextView numb;
        TextView titleTextView;
    }

    @Override
    public int getPinnedHeaderState(int position) {
        int realPosition = position;
        if (realPosition < 0 || (mLocationPosition != -1 && mLocationPosition == realPosition)) {
            return PINNED_HEADER_GONE;
        }
        mLocationPosition = -1;
        int section = mIndexer.getSectionForPosition(realPosition);
        int nextSectionPosition = mIndexer.getPositionForSection(section + 1);
        if (nextSectionPosition != -1 && realPosition == nextSectionPosition - 1) {
            return PINNED_HEADER_PUSHED_UP;
        }
        return PINNED_HEADER_VISIBLE;
    }

    @Override
    public void configurePinnedHeader(View header, int position, int alpha) {
        int section = mIndexer.getSectionForPosition(position);
        String title = mIndexer.getSections()[section];
        if (section == 0) {
            title = mContext.getResources().getString(R.string.default_scene);
        }
        ((TextView) header.findViewById(R.id.header_text)).setText(title);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (view instanceof PinnedHeaderListView) {
            Log.i("xiaokk", "onScroll "+firstVisibleItem);
            ((PinnedHeaderListView) view).configureHeaderView(firstVisibleItem);
        }
    }


    @SuppressLint("NewApi")
    protected void updataBackground(int position, ImageView view){
        if(mListView.isItemChecked(position)){
            view.setBackgroundResource(R.drawable.g_select);
        }else{
            view.setBackgroundResource(R.drawable.g_unselect);
        }

    };
}
