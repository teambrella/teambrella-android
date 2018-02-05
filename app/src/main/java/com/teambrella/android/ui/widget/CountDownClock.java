package com.teambrella.android.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Clock Down Clock
 */
public class CountDownClock extends View {


    private static final long MINUTES_IN_7_DAYS = 7 * 24 * 60;
    private static final float MIN_RATIO = 0.08f;
    private static final float MAX_RATIO = 0.92f;


    private static final Paint SOLID_PAINT;
    private static final Paint ALPHA_PAINT;
    private static final Paint STROKE_PAINT;


    private float mSweepAngle = 0;


    static {
        SOLID_PAINT = new Paint();
        SOLID_PAINT.setStyle(Paint.Style.FILL);
        SOLID_PAINT.setColor(0xffffd152);
        SOLID_PAINT.setAntiAlias(true);

        ALPHA_PAINT = new Paint(SOLID_PAINT);
        ALPHA_PAINT.setAlpha(102);

        STROKE_PAINT = new Paint(SOLID_PAINT);
        STROKE_PAINT.setStyle(Paint.Style.STROKE);
        STROKE_PAINT.setColor(Color.WHITE);
        STROKE_PAINT.setStrokeWidth(3);


    }

    private RectF mRect = new RectF();

    public CountDownClock(Context context) {
        super(context);
    }

    public CountDownClock(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CountDownClock(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setRemainedMinutes(long minutes) {
        float ratio = (float) (minutes) / MINUTES_IN_7_DAYS;
        ratio = Math.max(Math.min(ratio, MAX_RATIO), MIN_RATIO);
        mSweepAngle = -360 * ratio;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mRect.bottom = getMeasuredHeight();
        mRect.right = getMeasuredWidth();
        canvas.drawCircle(mRect.right / 2, mRect.bottom / 2, mRect.bottom / 2, ALPHA_PAINT);
        float mStartAngle = 270f;
        canvas.drawArc(mRect, mStartAngle, mSweepAngle, true, SOLID_PAINT);
        canvas.drawCircle(mRect.right / 2, mRect.bottom / 2, mRect.bottom / 2, STROKE_PAINT);
        canvas.drawArc(mRect, mStartAngle, mSweepAngle, true, STROKE_PAINT);
    }
}
