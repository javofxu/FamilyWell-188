package me.hekr.sthome.commonBaseView;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

public class PinnedHeaderListView extends ListView {
    public interface PinnedHeaderAdapter {
        // Header View状态：不显示
        int PINNED_HEADER_GONE = 0;

        // Header View状态：显示在ListView顶部
        int PINNED_HEADER_VISIBLE = 1;

        // Header View状态：向上推出
        int PINNED_HEADER_PUSHED_UP = 2;

        int getPinnedHeaderState(int position);

        void configurePinnedHeader(View header, int position, int alpha);
    }

    private PinnedHeaderAdapter mAdapter;
    private View mHeaderView;
    private int mHeaderViewWidth;
    private int mHeaderViewHeight;
    private boolean mHeaderViewVisible;
    private static final int MAX_ALPHA = 255;

    public PinnedHeaderListView(Context context) {
        super(context);
    }

    public PinnedHeaderListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PinnedHeaderListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (mHeaderView != null) {
            // 布局Header View的位置
            mHeaderView.layout(0, 0, mHeaderViewWidth, mHeaderViewHeight);
            configureHeaderView(getFirstVisiblePosition());
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // 获取Header View的宽和高
        if (mHeaderView != null) {
            measureChild(mHeaderView, widthMeasureSpec, heightMeasureSpec);
            mHeaderViewWidth = mHeaderView.getMeasuredWidth();
            mHeaderViewHeight = mHeaderView.getMeasuredHeight();
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        // 绘制Header View
        if (mHeaderViewVisible) {
            drawChild(canvas, mHeaderView, getDrawingTime());
        }
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        super.setAdapter(adapter);
        mAdapter = (PinnedHeaderAdapter) adapter;
    }

    public void setPinnedHeaderView(View view) {
        mHeaderView = view;
        if (mHeaderView != null) {
            // 设置边缘渐变长度为0
            setFadingEdgeLength(0);
        }
        requestLayout();
    }

    public void configureHeaderView(int position) {
        if (mHeaderView == null) {
            return;
        }
        int state = mAdapter.getPinnedHeaderState(position);
        switch (state) {
            case PinnedHeaderAdapter.PINNED_HEADER_GONE:
                mHeaderViewVisible = false;
                break;
            case PinnedHeaderAdapter.PINNED_HEADER_VISIBLE:
                mAdapter.configurePinnedHeader(mHeaderView, position, MAX_ALPHA);
                if (mHeaderView.getTop() != 0) {
                    mHeaderView.layout(0, 0, mHeaderViewWidth, mHeaderViewHeight);
                }
                mHeaderViewVisible = true;
                break;
            case PinnedHeaderAdapter.PINNED_HEADER_PUSHED_UP:
                View firstView = getChildAt(0);
                int bottom = firstView.getBottom();
                int headerHeight = mHeaderView.getHeight();
                int y;
                int alpha;
                if (bottom < headerHeight) {
                    y = (bottom - headerHeight);
                    alpha = MAX_ALPHA * (headerHeight + y) / headerHeight;
                } else {
                    y = 0;
                    alpha = MAX_ALPHA;
                }
                mAdapter.configurePinnedHeader(mHeaderView, position, alpha);
                if (mHeaderView.getTop() != y) {
                    mHeaderView.layout(0, y, mHeaderViewWidth, mHeaderViewHeight + y);
                }
                mHeaderViewVisible = true;
                break;
        }
    }
}
