package com.teambrella.android.ui.chat;

import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.view.View;

import com.teambrella.android.R;

/**
 * Voting Panel Behaviour
 */
public class VotingPanelBehaviour extends AVotingViewBehaviour {


    private boolean mIgnoreScroll;

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
        if (!isAnimated && child.getVisibility() == View.VISIBLE && type == ViewCompat.TYPE_TOUCH) {
            float currentTranslation = child.getTranslationY() - dyConsumed - dyUnconsumed;

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
    }
}

