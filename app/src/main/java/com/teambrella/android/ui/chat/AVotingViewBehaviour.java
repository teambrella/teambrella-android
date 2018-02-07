package com.teambrella.android.ui.chat;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.support.design.widget.CoordinatorLayout;
import android.view.View;

/**
 * Voting View Behavior
 */
public class AVotingViewBehaviour extends CoordinatorLayout.Behavior<View> {

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
            ObjectAnimator fadeIn = ObjectAnimator.ofFloat(view, "alpha", 0.5f, 1f);
            AnimatorSet set = new AnimatorSet();
            set.playTogether(translation, fadeIn);
            set.setDuration(300);
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
                ObjectAnimator fadeOut = ObjectAnimator.ofFloat(view, "alpha", 1f, 0.5f);
                AnimatorSet set = new AnimatorSet();
                set.playTogether(translation, fadeOut);
                set.setDuration(300);
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
