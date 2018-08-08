package com.teambrella.android.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.util.AttributeSet;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.teambrella.android.R;

/**
 * Percentage statistics widget
 */
public class PercentageWidget extends ConstraintLayout {

    private ProgressBar mProgress;
    private TextView mPercentage;
    private TextView mDescription;

    public PercentageWidget(Context context) {
        super(context);
        init(context, null);
    }

    public PercentageWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public PercentageWidget(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public void setPercentage(float percentage) {
        int percent = Math.round(percentage * 100);
        mProgress.setMax(100);
        mProgress.setProgress(percent);
        mPercentage.setText(getContext().getString(R.string.percentage_format_string, percent));
    }

    public void setDescription(String description) {
        mDescription.setText(description);
    }

    private void init(Context context, AttributeSet attrs) {
        inflate(getContext(), R.layout.widget_percentage, this);
        mPercentage = findViewById(R.id.percentage);
        mProgress = findViewById(R.id.progress);
        mDescription = findViewById(R.id.description);
        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.PercentageWidget,
                    0, 0);

            try {
                Drawable drawableResource = a.getDrawable(R.styleable.PercentageWidget_percentageDrawable);
                if (drawableResource != null) {
                    mProgress.setProgressDrawable(drawableResource);
                }
            } finally {
                a.recycle();
            }
        }
    }
}
