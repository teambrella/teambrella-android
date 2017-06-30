package com.teambrella.android.ui.widget;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import java.util.Random;

/**
 * Voter Bar
 */
public class VoterBar extends HorizontalScrollView {


    public interface VoterBarListener {
        void onVoteChanged(float vote, boolean fromUser);
    }


    private LinearLayout mContainer;

    public VoterBar(Context context) {
        super(context);
    }

    public VoterBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VoterBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    private VoterBarListener mVoterBarListener;

    public void setVoterBarListener(VoterBarListener listener) {
        mVoterBarListener = listener;
    }


    private void init() {
        if (mContainer == null) {
            mContainer = new LinearLayout(getContext());
            mContainer.setGravity(Gravity.BOTTOM);
            mContainer.setOrientation(LinearLayout.HORIZONTAL);

            View leftView = new View(getContext());
            leftView.setBackgroundColor(Color.WHITE);
            mContainer.addView(leftView, new ViewGroup.LayoutParams(getMeasuredWidth() / 2, ViewGroup.LayoutParams.MATCH_PARENT));

            for (int i = 0; i < 24; i++) {
                View box = new View(getContext());
                box.setBackgroundColor(new Random().nextInt());
                mContainer.addView(box, new ViewGroup.LayoutParams(getMeasuredWidth() / 16,
                        (int) (new Random().nextFloat() * getMeasuredHeight())));
            }
            View rightView = new View(getContext());
            leftView.setBackgroundColor(Color.WHITE);
            mContainer.addView(rightView, new ViewGroup.LayoutParams(getMeasuredWidth() / 2, ViewGroup.LayoutParams.MATCH_PARENT));

            addView(mContainer);
            setSmoothScrollingEnabled(false);
        }
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        post(this::init);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        int max = mContainer.getWidth() - getMeasuredWidth();
        if (mVoterBarListener != null) {
            mVoterBarListener.onVoteChanged(((float) l) / max, true);
        }
    }
}
