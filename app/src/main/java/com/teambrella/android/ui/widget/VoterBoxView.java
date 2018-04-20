package com.teambrella.android.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.teambrella.android.R;

public final class VoterBoxView extends RelativeLayout {
    public VoterBoxView(Context context) {
        super(context);
    }

    public VoterBoxView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VoterBoxView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public int getMinHeight() {
        return findViewById(R.id.risk).getHeight();
    }
}
