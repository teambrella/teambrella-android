package com.teambrella.android.ui.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

/**
 * Akkurat Text View
 */
public class AkkuratBoldTextView extends AppCompatTextView {

    public AkkuratBoldTextView(Context context) {
        super(context);
        init();
    }

    public AkkuratBoldTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AkkuratBoldTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/AkkuratPro-Bold.otf"));
    }
}
