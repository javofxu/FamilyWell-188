package me.hekr.sthome.equipment;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.Arrays;
import java.util.List;

import me.hekr.sthome.R;
import me.hekr.sthome.common.TopbarSuperActivity;
import me.hekr.sthome.equipment.adapter.InstructionAdapter;
import me.hekr.sthome.equipment.data.InstructionsHm;

/**
 * @author skygge
 * @date 2020/10/22.
 * GitHub：javofxu@github.com
 * email：skygge@yeah.net
 * description：
 */
public class InstructionActivity extends TopbarSuperActivity {

    @Override
    protected void onCreateInit() {
        getTopBarView().setTopBarStatus(1, 1, getResources().getString(R.string.instrution), null, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        },null);
        RecyclerView mInstructionList = findViewById(R.id.rv_instruction_list);
        mInstructionList.setLayoutManager(new LinearLayoutManager(this));
        List<InstructionsHm> mList = Arrays.asList(InstructionsHm.values());
        InstructionAdapter mAdapter = new InstructionAdapter(this, mList);
        mInstructionList.setAdapter(mAdapter);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_instruction;
    }
}
