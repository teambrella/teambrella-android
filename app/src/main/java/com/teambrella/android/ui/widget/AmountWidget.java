package com.teambrella.android.ui.widget;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.widget.TextView;

import com.teambrella.android.R;

import java.util.Locale;

/**
 * Amount Widget
 */
public class AmountWidget extends ConstraintLayout {


    public AmountWidget(Context context) {
        super(context);
        init();
    }

    public AmountWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AmountWidget(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private void init() {
        inflate(getContext(), R.layout.widget_amount, this);
    }


    /**
     * Set Amount
     *
     * @param amount amount
     */
    public void setAmount(float amount) {
        ((TextView) findViewById(R.id.amount)).setText(getContext().getString(R.string.currency_format_string, amount));
    }

    /**
     * Set Amount
     *
     * @param amount amount
     */
    public void setAmount(int amount) {
        ((TextView) findViewById(R.id.amount)).setText(String.format(Locale.US, "%d", amount));
    }


}
