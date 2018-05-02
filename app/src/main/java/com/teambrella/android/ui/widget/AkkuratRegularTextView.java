package com.teambrella.android.ui.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.support.text.emoji.widget.EmojiTextViewHelper;
import android.text.InputFilter;
import android.util.AttributeSet;

/**
 * Akkurat Regular Text View
 */
public class AkkuratRegularTextView extends android.support.v7.widget.AppCompatTextView {

    private EmojiTextViewHelper mEmojiHelper;

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
        getEmojiTextViewHelper().updateTransformationMethod();
    }

    @Override
    public void setFilters(InputFilter[] filters) {
        super.setFilters(getEmojiTextViewHelper().getFilters(filters));
    }

    @Override
    public void setAllCaps(boolean allCaps) {
        super.setAllCaps(allCaps);
        getEmojiTextViewHelper().setAllCaps(allCaps);
    }

    private EmojiTextViewHelper getEmojiTextViewHelper() {
        if (mEmojiHelper == null) {
            mEmojiHelper = new EmojiTextViewHelper(this);
        }
        return mEmojiHelper;
    }

}
