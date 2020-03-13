package me.hekr.sthome.model.modeladapter;

import java.util.Arrays;

import android.widget.SectionIndexer;

public class SceneSectionIndexer implements android.widget.SectionIndexer {
    private String[] mSections;
    private int[] mPositions;
    private int mCount;

    public SceneSectionIndexer(String[] sections, int[] counts) {
        if (sections == null || counts == null) {
            throw new NullPointerException();
        }
        if (sections.length != counts.length) {
            throw new IllegalArgumentException("The sections and counts arrays must have the same length");
        }
        this.mSections = sections;
        mPositions = new int[counts.length];
        int position = 0;
        for (int i = 0; i < counts.length; i++) {
            mPositions[i] = position;
            position += counts[i];
        }
        mCount = position;
    }

    @Override
    public String[] getSections() {
        return mSections;
    }

    // 根据section的索引，获取该分组中的第一个item在listview中的位置
    // 如在该项目中：sectionIndex为1时，返回9；sectionIndex为2时，返回20。
    @Override
    public int getPositionForSection(int sectionIndex) {
        if (sectionIndex < 0 || sectionIndex >= mSections.length) {
            return -1;
        }
        return mPositions[sectionIndex];
    }

    // 根据item在listview中的位置，获取该item所在的分组的索引
    // 如在该项目中：北京、保定都属于字母B，都返回2；大理、大同都属于字母D，都返回4。
    @Override
    public int getSectionForPosition(int position) {
        if (position < 0 || position >= mCount) {
            return -1;
        }
        int index = Arrays.binarySearch(mPositions, position);
        return index >= 0 ? index : -index - 2;
    }
}
