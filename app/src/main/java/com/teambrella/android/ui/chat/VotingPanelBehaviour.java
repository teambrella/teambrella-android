package com.teambrella.android.ui.chat;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.view.View;

import com.teambrella.android.R;

/**
 * Voting Panel Behaviour
 */
public class VotingPanelBehaviour extends AVotingViewBehaviour {


    private int mDirection;

    public VotingPanelBehaviour() {
    }

    public VotingPanelBehaviour(OnHideShowListener mListener) {
        super(mListener);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        return dependency.getId() == R.id.list;
    }


    @Override
    public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull View child, @NonNull View directTargetChild, @NonNull View target, int axes, int type) {
        return true;
    }


    @Override
    public void onNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull View child, @NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type) {
        if (!isAnimated && child.getVisibility() == View.VISIBLE) {
                mDirection += dyConsumed;
            float currentTranslation = child.getTranslationY() - dyConsumed;

            if (currentTranslation < -child.getHeight()) {
                currentTranslation = -child.getHeight();
            }
            if (currentTranslation > 0) {
                currentTranslation = 0;
            }
            child.setTranslationY(currentTranslation);
            super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type);
        }
    }

    @Override
    public void onStopNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull View child, @NonNull View target, int type) {
        super.onStopNestedScroll(coordinatorLayout, child, target, type);
        if (!isAnimated && child.getVisibility() == View.VISIBLE) {
            float currentTranslation = child.getTranslationY();
            if (currentTranslation != 0 && currentTranslation != -child.getHeight()) {
                if (mDirection < 0) {
                    ObjectAnimator animator = ObjectAnimator.ofFloat(child, "translationY", currentTranslation, 0);
                    animator.setDuration(DURATION);
                    animator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            isAnimated = false;
                        }
                    });
                    isAnimated = true;
                    animator.start();
                } else {
                    ObjectAnimator animator = ObjectAnimator.ofFloat(child, "translationY", currentTranslation, -child.getHeight());
                    animator.setDuration(DURATION);
                    animator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            isAnimated = false;
                        }
                    });
                    isAnimated = true;
                    animator.start();
                }
            }
            mDirection = 0;
        }
    }
}

