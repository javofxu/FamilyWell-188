package me.hekr.sthome.commonBaseView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;

public class LetterView extends View {
    private String[] mLetters = null;
    private int mChoose = -1;
    private Paint mPaint = new Paint();
    private boolean mShowBg = false;

    private PopupWindow mPopupWindow;
    private TextView mPopupTextView;

    private Handler mHandler = new Handler();

    private OnLetterClickListener mOnLetterClickListener;

    private static final long POPUP_DELAY_MILLIS = 800;

    public LetterView(Context context) {
        super(context);
        init(context);
    }

    public LetterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public LetterView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public void setOnLetterClickListener(OnLetterClickListener listener) {
        mOnLetterClickListener = listener;
    }

    private void init(Context context) {
        mLetters = new String[]{"热门", "A", "B", "C", "D", "E", "F", "G", "H", "J", "K", "L", "M", "N", "P", "Q",
                "R", "S", "T", "W", "X", "Y", "Z"};
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mShowBg) {
            canvas.drawColor(Color.parseColor("#40000000"));
        }

        int height = getHeight() - getPaddingTop() - getPaddingBottom();
        int width = getWidth();
        int singleHeight = height / mLetters.length;
        for (int i = 0; i < mLetters.length; i++) {
            mPaint.setColor(Color.BLACK);
            mPaint.setTypeface(Typeface.DEFAULT_BOLD);
            mPaint.setFakeBoldText(true);
            mPaint.setAntiAlias(true);
            mPaint.setTextSize(30);
            if (i == mChoose) {
                mPaint.setColor(Color.parseColor("#197eee"));
            } else {
                mPaint.setColor(Color.parseColor("#999999"));
            }
            float xPos = width / 2 - mPaint.measureText(mLetters[i]) / 2;
            float yPos = singleHeight * i + singleHeight * 0.75f;
            canvas.drawText(mLetters[i], xPos, yPos, mPaint);
            mPaint.reset();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        final int action = event.getAction();
        final float y = event.getY();
        final int oldChoose = mChoose;
        final int c = (int) (y / getHeight() * mLetters.length);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mShowBg = true;
                if (oldChoose != c) {
                    if (c >= 0 && c < mLetters.length) {
                        performItemClicked(c);
                        mChoose = c;
                        invalidate();
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (oldChoose != c) {
                    if (c >= 0 && c < mLetters.length) {
                        performItemClicked(c);
                        mChoose = c;
                        invalidate();
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                mShowBg = false;
                mChoose = -1;
                dismissPopup();
                invalidate();
                break;
        }
        return true;
    }

    private void showPopup(int item) {
        if (mPopupWindow == null) {
            mHandler.removeCallbacks(mDismissRunnable);
            mPopupTextView = new TextView(getContext());
            mPopupTextView.setBackgroundColor(Color.parseColor("#b4000000"));
            mPopupTextView.setGravity(Gravity.CENTER);
            mPopupTextView.setTextColor(Color.WHITE);
            mPopupWindow = new PopupWindow(mPopupTextView);
        }

        String text;
        if (item == 0) {
            text = "热门";
        } else {
            text = mLetters[item];
        }
        mPopupTextView.setText(text);
        mPopupTextView.setTextSize(28);

        if (mPopupWindow.isShowing()) {
            mPopupWindow.update(200, 200);
        } else {
            mPopupWindow.setWidth(200);
            mPopupWindow.setHeight(200);
            mPopupWindow.showAtLocation(getRootView(), Gravity.CENTER, 0, 0);
        }
    }

    private void dismissPopup() {
        mHandler.postDelayed(mDismissRunnable, POPUP_DELAY_MILLIS);
    }

    private Runnable mDismissRunnable = new Runnable() {
        @Override
        public void run() {
            if (mPopupWindow != null) {
                mPopupWindow.dismiss();
            }
        }
    };

    private void performItemClicked(int item) {
        if (mOnLetterClickListener != null) {
            String s;
            if (item == 0) {
                s = "#";// “热门城市”的占位符设定的为"#"
            } else {
                s = mLetters[item];
            }
            mOnLetterClickListener.onLetterClick(s);
            showPopup(item);
        }
    }

    public interface OnLetterClickListener {
        void onLetterClick(String letter);
    }
}
