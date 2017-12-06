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

    private static final float DEFAULT_PROPORTION = 0.5f;

    private static final String USD = "USD";
    private static final String PEN = "PEN";
    private static final String ARS = "ARS";
    private static final String EUR = "EUR";


    public static void setAmount(TextView textView, int amount, String currency) {
        final Context context = textView.getContext();
        final SpannableString text = new SpannableString(Integer.toString(amount) + " " + currency);
        int start = text.length() - currency.length() - 1;
        int end = text.length();
        text.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.darkSkyBlue)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        text.setSpan(new CurrencyRelativeSizeSpan("1234567890"/*Integer.toString(amount).substring(start - 1)*/), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(text);
    }

    public static void setAmount(TextView textView, float amount, String currency) {
        final Context context = textView.getContext();
        final SpannableString text = amount < 100f ? new SpannableString(String.format(Locale.US, "%.2f", amount) + " " + currency)
                : new SpannableString(String.format(Locale.US, "%d", Math.round(amount)) + " " + currency);
        int start = text.length() - currency.length() - 1;
        int end = text.length();
        text.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.darkSkyBlue)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        text.setSpan(new CurrencyRelativeSizeSpan("1234567890"), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(text);
    }

    public static void setAmount(TextView textView, String source, String currency) {
        final Context context = textView.getContext();
        final SpannableString text = new SpannableString(source);
        int index = 0;
        while (index != -1) {
            index = source.indexOf(currency, index);
            if (index != -1) {
                text.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.darkSkyBlue)), index, index + currency.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                text.setSpan(new CurrencyRelativeSizeSpan("1234567890"), index, index + currency.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                index += currency.length();
            }
        }
        textView.setText(text);
    }


    public static void setCryptoAmount(TextView textView, float amount) {
        final Context context = textView.getContext();
        int stringId = amount > 1 ? R.string.ethereum : R.string.milli_ethereum;
        String cryptoCurrency = context.getString(stringId);
        switch (stringId) {
            case R.string.ethereum:
                setAmount(textView, amount, cryptoCurrency);
                break;
            case R.string.milli_ethereum:
                setAmount(textView, Math.round(amount * 1000), cryptoCurrency);
                break;
        }
    }

    public static String getCurrencySign(String currency) {
        if (USD.equalsIgnoreCase(currency)) {
            return "$";
        } else if (PEN.equalsIgnoreCase(currency)) {
            return "S/. ";
        } else if (ARS.equalsIgnoreCase(currency)) {
            return "$";
        } else if (EUR.equalsIgnoreCase(currency)) {
            return "€";
        } else {
            return currency;
        }
    }


    private static class CurrencyRelativeSizeSpan extends MetricAffectingSpan {


        private final String mText;

        CurrencyRelativeSizeSpan(String text) {
            mText = text;
        }

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
            ds.getTextBounds(mText, 0, mText.length(), bounds);
            int shift = bounds.top - bounds.bottom;
            ds.setTextSize(ds.getTextSize() * DEFAULT_PROPORTION);
            ds.getTextBounds(mText, 0, mText.length(), bounds);
            shift += bounds.bottom - bounds.top;
            ds.baselineShift += shift;
        }
    }


}
