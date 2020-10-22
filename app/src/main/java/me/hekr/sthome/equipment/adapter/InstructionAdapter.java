package me.hekr.sthome.equipment.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

import me.hekr.sthome.R;
import me.hekr.sthome.equipment.WebViewActivity;
import me.hekr.sthome.equipment.data.InstructionsHm;

/**
 * @author skygge
 * @date 2020-10-22.
 * GitHub：javofxu@github.com
 * email：skygge@yeah.net
 * description：产品说明书适配器
 */
public class InstructionAdapter extends RecyclerView.Adapter<InstructionAdapter.ItemHolder> {

    private Context mContext;
    private List<InstructionsHm> mList;

    private String space;
    private String SMART_HOME_INSTRUCTIONS = "http://61.164.94.198:1415/instruction/";

    public InstructionAdapter(Context context, List<InstructionsHm> mList) {
        this.mContext = context;
        this.mList = mList;
        Locale locale = Locale.getDefault();
        String mLanguage = locale.getLanguage();
        if("zh".equals(mLanguage)){
            space =  "";
        }else {
            space =  "\t";
        }
    }

    @NonNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View mView = LayoutInflater.from(mContext).inflate(R.layout.item_product_instruction, viewGroup, false);
        return new ItemHolder(mView);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemHolder itemHolder, int i) {
        final InstructionsHm instructionsHm = mList.get(i);
        final String name = instructionsHm.getModel() + "\t\t" + mContext.getString(instructionsHm.getProductRes()) + space + mContext.getString(R.string.instrution);
        itemHolder.mIcon.setImageResource(instructionsHm.getDrawableRes());
        itemHolder.mTitle.setText(name);
        itemHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = SMART_HOME_INSTRUCTIONS + instructionsHm.getLink();
                Intent intent = new Intent();
                intent.setClass(mContext, WebViewActivity.class);
                intent.putExtra("instructions_name", name);
                intent.putExtra("instructions_urls", url);
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }

    static class ItemHolder extends RecyclerView.ViewHolder {
        ImageView mIcon;
        TextView mTitle;
        ItemHolder(@NonNull View itemView) {
            super(itemView);
            mIcon = itemView.findViewById(R.id.iv_instruction_icon);
            mTitle = itemView.findViewById(R.id.tv_instruction_title);
        }
    }
}
