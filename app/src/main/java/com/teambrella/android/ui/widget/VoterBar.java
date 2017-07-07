package com.teambrella.android.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.teambrella.android.R;

import java.util.Locale;

/**
 * Voter Bar
 */
public class VoterBar extends HorizontalScrollView {

    public static class VoterBox {
        public final double left;
        public final double right;
        public final int height;
        public final double value;


        public VoterBox(double left, double right, double value, int height) {
            this.left = left;
            this.right = right;
            this.value = value;
            this.height = height;
        }
    }


    public interface VoterBarListener {

        void onVoteChanged(float vote, boolean fromUser);

        void onVoterBarReleased(float vote, boolean fromUser);
    }


    private LinearLayout mContainer;
    private float mInitialVote;
    private boolean mIsFromUser;
    private boolean mIsTouched;

    public VoterBar(Context context) {
        super(context);
    }

    public VoterBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VoterBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    private VoterBox[] mData;


    private VoterBarListener mVoterBarListener;

    public void setVoterBarListener(VoterBarListener listener) {
        mVoterBarListener = listener;
    }


    public void setVote(float vote) {
        if (mContainer != null && mContainer.getMeasuredWidth() != 0) {
            smoothScrollTo((int) ((mContainer.getWidth() - getMeasuredWidth()) * vote), 0);
        } else {
            mInitialVote = vote;
        }
    }


    public void init(VoterBox[] data, float initialVote) {

        mData = data;
        mInitialVote = initialVote;
        if (getMeasuredWidth() == 0 || getMeasuredHeight() == 0) {
            return;
        }

        if (mContainer == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            mContainer = new LinearLayout(getContext());
            mContainer.setGravity(Gravity.BOTTOM);
            mContainer.setOrientation(LinearLayout.HORIZONTAL);

            View leftView = new View(getContext());
            leftView.setBackgroundColor(Color.WHITE);
            mContainer.addView(leftView, new ViewGroup.LayoutParams(getMeasuredWidth() / 2, 1));

            int width = getMeasuredWidth();
            int votingWidth = 4 * width;

            if (data[0].left >= 0f) {
                View view = new View(getContext());
                leftView.setBackgroundColor(Color.WHITE);
                mContainer.addView(view, new ViewGroup.LayoutParams((int) (data[0].left * votingWidth), 1));
            }

            for (VoterBox box : data) {
                View view = inflater.inflate(R.layout.risk_bar, mContainer, false);
                ((TextView) view.findViewById(R.id.risk)).setText(String.format(Locale.US, "%.2f", box.value));
                int childWidth = (int) ((box.right - box.left) * votingWidth);
                mContainer.addView(view, new ViewGroup.LayoutParams(childWidth, ViewGroup.LayoutParams.MATCH_PARENT));
            }

            if (data[data.length - 1].right * votingWidth <= votingWidth) {
                View view = new View(getContext());
                leftView.setBackgroundColor(Color.WHITE);
                mContainer.addView(view, new ViewGroup.LayoutParams((int) (votingWidth - data[data.length - 1].right * votingWidth), 1));
            }

            View rightView = new View(getContext());
            leftView.setBackgroundColor(Color.WHITE);
            mContainer.addView(rightView, new ViewGroup.LayoutParams(getMeasuredWidth() / 2, ViewGroup.LayoutParams.MATCH_PARENT));
            addView(mContainer);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        post(() -> init(mData, mInitialVote));
    }


    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mIsFromUser = true;
                mIsTouched = true;
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mIsTouched = false;
                removeCallbacks(mIdleChecker);
                postDelayed(mIdleChecker, 200);
                break;
        }
        return super.onTouchEvent(ev);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (mContainer != null) {
            int j = 0;
            int max = 0;

            for (VoterBox box : mData) {
                if (box.height > max) {
                    max = box.height;
                }
            }
            for (int i = 0; i < mContainer.getChildCount(); i++) {
                View view = mContainer.getChildAt(i);
                if (view instanceof VoterBoxView) {
                    VoterBoxView boxView = (VoterBoxView) view;
                    if (boxView.getMinHeight() > 0) {
                        int height = getMeasuredHeight() - boxView.getMinHeight() - 20 - 50;
                        view.setTranslationY(50 + height - height * ((float) mData[j].height / (float) max));
                        j++;
                    }
                }
            }

            if (mInitialVote > 0 && getScrollX() == 0) {
                scrollTo((int) ((mContainer.getWidth() - getMeasuredWidth()) * mInitialVote), 0);
                mInitialVote = -1f;
            }
        }
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        int max = mContainer.getWidth() - getMeasuredWidth();
        if (mVoterBarListener != null) {
            mVoterBarListener.onVoteChanged(((float) l) / max, mIsFromUser);
        }
        int shift = getMeasuredWidth() / 2;
        for (int i = 0; i < mContainer.getChildCount(); i++) {
            View child = mContainer.getChildAt(i);
            child.setSelected(child.getLeft() - shift < l && child.getRight() - shift > l);
        }
        removeCallbacks(mIdleChecker);
        postDelayed(mIdleChecker, 200);
    }


    public boolean isUserActive() {
        return mIsTouched || mIsFromUser;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        Path clipPath = new Path();
        clipPath.addRoundRect(new RectF(canvas.getClipBounds()), 10, 10, Path.Direction.CW);
        canvas.clipPath(clipPath);
        super.onDraw(canvas);
    }


    private final Runnable mIdleChecker = () -> {
        if (mVoterBarListener != null && !mIsTouched) {
            int max = mContainer.getWidth() - getMeasuredWidth();
            mVoterBarListener.onVoterBarReleased(((float) getScrollX()) / max, mIsFromUser);
            mIsFromUser = false;
        }
    };
}
