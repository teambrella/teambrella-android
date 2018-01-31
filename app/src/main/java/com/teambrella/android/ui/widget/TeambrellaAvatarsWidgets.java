package com.teambrella.android.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.teambrella.android.R;
import com.teambrella.android.image.TeambrellaImageLoader;
import com.teambrella.android.image.glide.GlideApp;

import java.util.Iterator;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Teambrella Avatar Widgets
 */
public class TeambrellaAvatarsWidgets extends FrameLayout {

    private int mAvatarSize;
    private int mAvatarBackgroundColor;
    private int mAvatarBorderWidth;
    private int mAvatarBorderColor;
    private int mAvatarCount;
    private int mAvatarShift;

    public TeambrellaAvatarsWidgets(@NonNull Context context) {
        super(context);
    }

    public TeambrellaAvatarsWidgets(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TeambrellaAvatarsWidgets(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TeambrellaAvatarsWidgets, defStyleAttr, 0);
        mAvatarSize = a.getDimensionPixelSize(R.styleable.TeambrellaAvatarsWidgets_avatar_size, 0);
        mAvatarBackgroundColor = a.getColor(R.styleable.TeambrellaAvatarsWidgets_avatar_background_color, 0);
        mAvatarBorderWidth = a.getDimensionPixelSize(R.styleable.TeambrellaAvatarsWidgets_avatar_border_width, 0);
        mAvatarBorderColor = a.getColor(R.styleable.TeambrellaAvatarsWidgets_avatar_border_color, 0);
        mAvatarCount = a.getInt(R.styleable.TeambrellaAvatarsWidgets_avatar_count, 0);
        mAvatarShift = a.getDimensionPixelSize(R.styleable.TeambrellaAvatarsWidgets_avatar_shift, 0);
        a.recycle();
        init();
    }

    private void init() {
        for (int i = 0; i < mAvatarCount; i++) {
            CircleImageView circleImageView = new CircleImageView(getContext());
            circleImageView.setBorderColor(mAvatarBorderColor);
            circleImageView.setBorderWidth(mAvatarBorderWidth);
            circleImageView.setFillColor(mAvatarBackgroundColor);
            int size = mAvatarSize;
            FrameLayout.LayoutParams params = new LayoutParams(size, size);
            params.gravity = Gravity.CENTER_VERTICAL;
            params.leftMargin = i * (mAvatarSize - mAvatarShift);
            addView(circleImageView, params);
        }

        AkkuratBoldTextView countView = (AkkuratBoldTextView) LayoutInflater.from(getContext())
                .inflate(R.layout.avatar_more_count_view, this, false);
        FrameLayout.LayoutParams params = new LayoutParams(mAvatarSize, mAvatarSize);
        params.gravity = Gravity.CENTER_VERTICAL;
        addView(countView, params);
    }


    public void setAvatars(TeambrellaImageLoader loader, List<String> uris, int posterCount) {
        Iterator<String> it = uris.iterator();
        for (int i = 0; i < mAvatarCount; i++) {
            ImageView imageview = (ImageView) getChildAt(i);
            String uri = it.hasNext() ? it.next() : null;
            if (uri != null) {
                GlideApp.with(this).load(loader.getImageUrl(uri))
                        .apply(RequestOptions.downsampleOf(DownsampleStrategy.CENTER_OUTSIDE))
                        .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                        .into(imageview);
            }
            imageview.setVisibility(uri != null ? VISIBLE : GONE);
        }

        AkkuratBoldTextView countView = findViewById(R.id.more_count);
        int count = posterCount - uris.size();
        if (count > 0) {
            FrameLayout.LayoutParams params = (LayoutParams) countView.getLayoutParams();
            params.leftMargin = uris.size() * (mAvatarSize - mAvatarShift);
            countView.setLayoutParams(params);
            countView.setText(getContext().getString(R.string.plus_count, count));
            countView.setVisibility(VISIBLE);
        } else {
            countView.setVisibility(GONE);
        }
    }


}
