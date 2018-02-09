package com.teambrella.android.ui.chat;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.support.design.widget.CoordinatorLayout;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

/**
 * Voting View Behavior
 */
public class AVotingViewBehaviour extends CoordinatorLayout.Behavior<View> {


    private static final long DURATION = 400;

    private final OnHideShowListener mListener;

    AVotingViewBehaviour() {
        mListener = null;
    }

    AVotingViewBehaviour(OnHideShowListener mListener) {
        this.mListener = mListener;
    }

    public interface OnHideShowListener {
        void onHide();

        void onShow();
    }

    boolean isAnimated = false;


    public void show(View view) {
        if (!isAnimated) {
            isAnimated = true;
            ObjectAnimator translation = ObjectAnimator.ofFloat(view, "translationY", -view.getHeight(), 0f);
            AnimatorSet set = new AnimatorSet();
            set.playTogether(translation);
            set.setDuration(DURATION);
//            set.setInterpolator((Interpolator) input -> {
//                        float value = (float) (1 / (1 + Math.pow(Math.E, (double) (-input * 2 + 1) * 10)));
//                        Log.e("TEST", "" + input + " " + value);
//                        return value;
//                    }
//            );
            set.setInterpolator(new DecelerateInterpolator());
            set.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    if (mListener != null) {
                        mListener.onShow();
                    }
                    view.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    isAnimated = false;
                }
            });
            set.start();
        }
    }


    void hide(View view) {
        if (!isAnimated) {
            if (view.getVisibility() == View.VISIBLE) {
                isAnimated = true;
                if (mListener != null) {
                    mListener.onHide();
                }
                ObjectAnimator translation = ObjectAnimator.ofFloat(view, "translationY", 0, -(float) view.getHeight());
                AnimatorSet set = new AnimatorSet();
                set.playTogether(translation);
                set.setDuration(DURATION);
                set.setInterpolator(new AccelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        view.setVisibility(View.INVISIBLE);
                        isAnimated = false;
                    }
                });
                set.start();
            }
        }
    }
}
