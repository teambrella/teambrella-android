package com.teambrella.android.util;

import android.content.Context;
import android.graphics.Rect;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.style.ForegroundColorSpan;
import android.text.style.MetricAffectingSpan;
import android.widget.TextView;

import com.teambrella.android.R;

import java.util.Locale;

/**
 * Amount currency util
 */
public class AmountCurrencyUtil {

    private static final String DEFAULT_TEXT = "1A";
    private static final float DEFAULT_PROPORTION = 0.5f;

    private static final String USD = "USD";
    private static final String PEN = "PEN";


    public static void setAmount(TextView textView, int amount, String currency) {
        final Context context = textView.getContext();
        final SpannableString text = new SpannableString(Integer.toString(amount) + " " + currency);
        int start = text.length() - currency.length() - 1;
        int end = text.length();
        text.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.darkSkyBlue)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        text.setSpan(new CurrencyRelativeSizeSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(text);
    }

    public static void setAmount(TextView textView, float amount, String currency) {
        final Context context = textView.getContext();
        final SpannableString text = amount < 100f ? new SpannableString(String.format(Locale.US, "%.2f", amount) + " " + currency)
                : new SpannableString(String.format(Locale.US, "%d", Math.round(amount)) + " " + currency);
        int start = text.length() - currency.length() - 1;
        int end = text.length();
        text.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.darkSkyBlue)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        text.setSpan(new CurrencyRelativeSizeSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(text);
    }

    public static String getCurrencySign(String currency) {
        if (USD.equalsIgnoreCase(currency)) {
            return "$";
        } else if (PEN.equalsIgnoreCase(currency)) {
            return "S/. ";
        } else {
            return currency;
        }
    }


    private static class CurrencyRelativeSizeSpan extends MetricAffectingSpan {

        @Override
        public void updateDrawState(TextPaint ds) {
            updateAnyState(ds);
        }

        @Override
        public void updateMeasureState(TextPaint ds) {
            updateAnyState(ds);
        }

        private void updateAnyState(TextPaint ds) {
            Rect bounds = new Rect();
            ds.getTextBounds(DEFAULT_TEXT, 0, DEFAULT_TEXT.length(), bounds);
            int shift = bounds.top - bounds.bottom;
            ds.setTextSize(ds.getTextSize() * DEFAULT_PROPORTION);
            ds.getTextBounds(DEFAULT_TEXT, 0, DEFAULT_TEXT.length(), bounds);
            shift += bounds.bottom - bounds.top;
            ds.baselineShift += shift;
        }
    }


}
