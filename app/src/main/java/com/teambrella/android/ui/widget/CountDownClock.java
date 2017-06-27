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


    private static final Paint SOLID_PAINT;
    private static final Paint ALPHA_PAINT;
    private static final Paint STROKE_PAINT;


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


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mRect.bottom = getMeasuredHeight();
        mRect.right = getMeasuredWidth();
        canvas.drawCircle(mRect.right / 2, mRect.bottom / 2, mRect.bottom / 2, ALPHA_PAINT);
        canvas.drawArc(mRect, 150, 120, true, SOLID_PAINT);
        canvas.drawCircle(mRect.right / 2, mRect.bottom / 2, mRect.bottom / 2, STROKE_PAINT);
        canvas.drawArc(mRect, 150, 120, true, STROKE_PAINT);
    }
}
