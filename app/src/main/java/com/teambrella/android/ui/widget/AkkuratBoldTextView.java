package com.teambrella.android.ui.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.StringRes;
import android.support.text.emoji.widget.EmojiTextViewHelper;
import android.support.v7.widget.AppCompatTextView;
import android.text.InputFilter;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.AttributeSet;

/**
 * Akkurat Text View
 */
public class AkkuratBoldTextView extends AppCompatTextView {

  private EmojiTextViewHelper mEmojiHelper;

  public static CharSequence getAkkuratBoldText(Context context, @StringRes int sourceId) {
    SpannableString s = new SpannableString(context.getString(sourceId));
    s.setSpan(new AkkuratBoldTypefaceSpan(context), 0, s.length(),
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    return s;
  }

  public static CharSequence getAkkuratBoldText(Context context, CharSequence source) {
    SpannableString s = new SpannableString(source);
    s.setSpan(new AkkuratBoldTypefaceSpan(context), 0, s.length(),
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    return s;
  }


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
