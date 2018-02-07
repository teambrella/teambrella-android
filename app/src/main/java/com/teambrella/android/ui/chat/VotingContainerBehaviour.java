package com.teambrella.android.ui.chat;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.view.View;

import com.teambrella.android.R;
import com.teambrella.android.util.log.Log;

/**
 * Voting Container Behaviour
 */
public class VotingContainerBehaviour extends AVotingViewBehaviour {


    private int mScrollCount;

    VotingContainerBehaviour(OnHideShowListener mListener) {
        super(mListener);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        return dependency.getId() == R.id.list;
    }

    @Override
    public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull View child, @NonNull View directTargetChild, @NonNull View target, int axes, int type) {
        mScrollCount++;
        return true;
    }

    @Override
    public void onNestedPreScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull View child, @NonNull View target, int dx, int dy, @NonNull int[] consumed, int type) {
        Log.e("TEST", "On Nested Prescroll " + dy);
        if (!isAnimated && child.getVisibility() == View.VISIBLE) {
            float currentTranslation = child.getTranslationY() - dy;

            if (currentTranslation < -child.getHeight()) {
                currentTranslation = -child.getHeight();
            }
            if (currentTranslation > 0) {
                currentTranslation = 0;
            }
            child.setTranslationY(currentTranslation);
        }
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type);
    }

    @Override
    public boolean onNestedPreFling(@NonNull CoordinatorLayout coordinatorLayout, @NonNull View child, @NonNull View target, float velocityX, float velocityY) {
        return super.onNestedPreFling(coordinatorLayout, child, target, velocityX, velocityY);
    }

    @Override
    public void onStopNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull View child, @NonNull View target, int type) {
        super.onStopNestedScroll(coordinatorLayout, child, target, type);
        mScrollCount--;
        if (mScrollCount == 0) {
            float translationY = child.getTranslationY();
            if (translationY <= (-child.getHeight()) / 2) {
                ObjectAnimator translation = ObjectAnimator.ofFloat(child, "translationY", translationY, -child.getHeight());
                translation.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        isAnimated = false;
                    }
                });
                isAnimated = true;
                translation.start();
            } else {
                ObjectAnimator translation = ObjectAnimator.ofFloat(child, "translationY", child.getTranslationY(), 0);
                translation.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        isAnimated = false;
                    }
                });
                isAnimated = true;
                translation.start();
            }

            Log.e("TEST", "FIRE");
        }
    }


}
