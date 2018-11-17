package com.teambrella.android.ui.chat;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.teambrella.android.R;
import com.teambrella.android.util.log.Log;

/**
 * Voting Panel Behaviour
 */
public class VotingPanelBehaviour extends AVotingViewBehaviour {

  private static final String LOG_TAG = VotingPanelBehaviour.class.getSimpleName() + " [scroll]";

  private int mDirection;

  public VotingPanelBehaviour() {
  }

  public VotingPanelBehaviour(OnHideShowListener mListener) {
    super(mListener);
  }

  @Override
  public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
    // Log.v(LOG_TAG, "layoutDependsOn");
    return dependency.getId() == R.id.list;
  }


  @Override
  public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull View child, @NonNull View directTargetChild, @NonNull View target, int axes, int type) {
    // Log.v(LOG_TAG, "onStartNestedScroll");
    return true;
  }

  @Override
  public boolean blocksInteractionBelow(CoordinatorLayout parent, View child) {
    return false;
  }


  @Override
  public void onNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull View child, @NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type) {
    if (!isAnimated && child.getVisibility() == View.VISIBLE) {
      if (dyConsumed == 0) {
        //((RecyclerView) target).smoothScrollBy(0,0);
        ((RecyclerView) target).stopNestedScroll(type);
      }
      // Log.v(LOG_TAG, "onNestedScroll - if, dyConsumed=" + String.valueOf(dyConsumed) + ", " + " dyUnconsumed=" + String.valueOf(dyUnconsumed) + " type=" + String.valueOf(type));
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
    else {
      // Log.v(LOG_TAG, "onNestedScroll - else, dyConsumed=" + String.valueOf(dyConsumed) + ", " + " dyUnconsumed=" + String.valueOf(dyUnconsumed) + " type=" + String.valueOf(type));
    }
  }

  @Override
  public void onStopNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull View child, @NonNull View target, int type) {
    super.onStopNestedScroll(coordinatorLayout, child, target, type);
    if (!isAnimated && child.getVisibility() == View.VISIBLE) {
      float currentTranslation = child.getTranslationY();
      // Log.v(LOG_TAG, "onStopNestedScroll - if, currentTranslation=" + String.valueOf(currentTranslation));
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
      else {
        // Log.v(LOG_TAG, "onStopNestedScroll - else, currentTranslation=" + String.valueOf(currentTranslation));
      }
      mDirection = 0;
    }
  }
}

