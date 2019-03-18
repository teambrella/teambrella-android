package com.teambrella.android.util;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.style.ForegroundColorSpan;
import android.text.style.MetricAffectingSpan;
import android.widget.TextView;

import com.teambrella.android.R;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
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
    private static final String RUB = "RUB";

    private static DecimalFormat sDecimalFormat = (DecimalFormat)DecimalFormat.getInstance();


    static {
        DecimalFormatSymbols symbols = sDecimalFormat.getDecimalFormatSymbols();
        symbols.setGroupingSeparator(' ');
        sDecimalFormat.setGroupingUsed(true);
        sDecimalFormat.setDecimalFormatSymbols(symbols);
    }


    public static void setAmount(TextView textView, int amount, String currency) {
        final Context context = textView.getContext();
        currency = getLocalizedCurrency(context, currency);
        final SpannableString text = new SpannableString(sDecimalFormat.format(amount) + " " + currency);
        int start = text.length() - currency.length() - 1;
        int end = text.length();
        text.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.darkSkyBlue)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        text.setSpan(new CurrencyRelativeSizeSpan("1234567890"/*Integer.toString(amount).substring(start - 1)*/), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(text);
    }

    public static void setAmount(TextView textView, float amount, String currency) {
        final Context context = textView.getContext();
        currency = getLocalizedCurrency(context, currency);
        final SpannableString text = amount < 100f ? new SpannableString(String.format(Locale.US, "%.2f", amount) + " " + currency)
                : new SpannableString(sDecimalFormat.format(Math.round(amount)) + " " + currency);
        int start = text.length() - currency.length() - 1;
        int end = text.length();
        text.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.darkSkyBlue)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        text.setSpan(new CurrencyRelativeSizeSpan("1234567890"), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(text);
    }

    public static void setAmount(TextView textView, String source, String currency) {
        final Context context = textView.getContext();
        currency = getLocalizedCurrency(context, currency);
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
        String cryptoCurrency = context.getString(R.string.milli_ethereum);
        setAmount(textView, Math.round(amount * 1000), cryptoCurrency);
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
        } else if (RUB.equalsIgnoreCase(currency) && Build.VERSION.SDK_INT
                >= Build.VERSION_CODES.M){
            return "\u20BD";
        } else {
            return currency;
        }
    }

    public static String getLocalizedCurrency(Context context, String code){
        switch (code) {
            case USD:
                return context.getString(R.string.currency_usd);
            case PEN:
                return context.getString(R.string.currency_pen);
            case ARS:
                return context.getString(R.string.currency_ars);
            case EUR:
                return context.getString(R.string.currency_eur);
            case RUB:
                return context.getString(R.string.currency_rub);
            default:
                return code;
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
        public void updateMeasureState(@NonNull TextPaint ds) {
            updateAnyState(ds);
        }

        private void updateAnyState(TextPaint ds) {
            Rect bounds = new Rect();
            ds.getTextBounds(mText, 0, mText.length(), bounds);
            int shift = bounds.top - bounds.bottom;
            ds.setTextSize(ds.getTextSize() * DEFAULT_PROPORTION);
            ds.getTextBounds(mText, 0, mText.length(), bounds);
            shift += bounds.bottom - bounds.top;
            ds.baselineShift += (shift + 2);
        }
    }


}
