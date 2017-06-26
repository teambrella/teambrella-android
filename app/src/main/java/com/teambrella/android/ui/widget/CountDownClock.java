package com.teambrella.android.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.teambrella.android.R;

/**
 * Created by dvasilin on 26/06/2017.
 */

public class CountDownClock extends View {

    private Paint mPaint = new Paint();
    private RectF mRect = new RectF();


    public CountDownClock(Context context) {
        super(context);
        init();
    }

    public CountDownClock(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CountDownClock(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(getContext().getResources().getColor(R.color.lightGold));
        mPaint.setAlpha(100);
        canvas.drawCircle((float) getMeasuredWidth() / 2, (float) getMeasuredHeight() / 2, (float) getMeasuredHeight() / 2, mPaint);
        mPaint.setAlpha(255);
        mRect.bottom = getMeasuredHeight();
        mRect.right = getMeasuredWidth();

        canvas.drawArc(mRect, 150, 120, true, mPaint);

        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.WHITE);
        mPaint.setStrokeWidth(2);
        canvas.drawCircle((float) getMeasuredWidth() / 2, (float) getMeasuredHeight() / 2, (float) getMeasuredHeight() / 2, mPaint);
        mRect.bottom = getMeasuredHeight();
        mRect.right = getMeasuredWidth();
        canvas.drawArc(mRect, 150, 120, true, mPaint);
    }
}
