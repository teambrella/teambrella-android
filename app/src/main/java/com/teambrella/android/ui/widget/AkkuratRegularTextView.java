package com.teambrella.android.ui.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

/**
 * Akkurat Regular Text View
 */
public class AkkuratRegularTextView extends android.support.v7.widget.AppCompatTextView {

    public AkkuratRegularTextView(Context context) {
        super(context);
        init();
    }

    public AkkuratRegularTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AkkuratRegularTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/AkkuratPro-Regular.otf"));
    }

}
