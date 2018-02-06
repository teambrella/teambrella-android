package com.teambrella.android.ui.chat;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;

import com.teambrella.android.R;

/**
 * Voting Panel Behaviour
 */
public class VotingPanelBehaviour extends CoordinatorLayout.Behavior<View> {

    public VotingPanelBehaviour() {
        super();
    }

    public VotingPanelBehaviour(Context context, AttributeSet attrs) {
        super(context, attrs);
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

