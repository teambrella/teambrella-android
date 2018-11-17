package com.teambrella.android.ui.chat;

import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.view.MotionEvent;
import android.view.View;

import com.teambrella.android.R;
import com.teambrella.android.util.log.Log;

/**
 * Voting Container Behaviour
 */
public class VotingContainerBehaviour extends AVotingViewBehaviour {

  private static final String LOG_TAG = VotingContainerBehaviour.class.getSimpleName() + " [scroll]";

  VotingContainerBehaviour(OnHideShowListener mListener) {
    super(mListener);
  }

  @Override
  public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
    return dependency.getId() == R.id.list;
  }

  @Override
  public boolean onInterceptTouchEvent(CoordinatorLayout parent, View child, MotionEvent ev) {
    if (ev.getAction() == MotionEvent.ACTION_DOWN && child.getVisibility() == View.VISIBLE
            && !isAnimated) {
      // Log.v(LOG_TAG, "onInterceptTouchEvent - if");
      int location[] = new int[2];
      child.getLocationOnScreen(location);
      RectF rect = new RectF(location[0], location[1], location[0] + child.getWidth(), location[1] + child.getHeight());
      if (!rect.contains(ev.getRawX(), ev.getRawY()))
        // Log.v(LOG_TAG, "onInterceptTouchEvent - hide");
        hide(child);
    }
    else {
      // Log.v(LOG_TAG, "onInterceptTouchEvent - else");
    }
    return super.onInterceptTouchEvent(parent, child, ev);
  }

  @Override
  public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull View child, @NonNull View directTargetChild, @NonNull View target, int axes, int type) {
    // Log.v(LOG_TAG, "onStartNestedScroll");
    return true;
  }

  @Override
  public void onNestedPreScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull View child, @NonNull View target, int dx, int dy, @NonNull int[] consumed, int type) {
    if ((dy != 0 || dx != 0) && type == ViewCompat.TYPE_TOUCH) {
      hide(child);
      // Log.v(LOG_TAG, "onNestedPreScroll - if, dy=" + String.valueOf(dy));
    }
    else {
      // Log.v(LOG_TAG, "onNestedPreScroll - else");
    }
    super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type);
  }

}
