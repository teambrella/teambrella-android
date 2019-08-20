package com.teambrella.android.ui.image;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.teambrella.android.R;
import com.teambrella.android.image.glide.GlideApp;
import com.teambrella.android.ui.base.TeambrellaDaggerActivity;
import com.teambrella.android.ui.base.TeambrellaFragment;

import java.util.ArrayList;

/**
 *
 */
public class ImageViewerActivity extends TeambrellaDaggerActivity {

    private static final String EXTRA_URIS = "uris";
    private static final String EXTRA_POSITION = "position";

    @SuppressWarnings("FieldCanBeLocal")
    private ViewPager mViewPager;
    private TextView mPagerIndicator;


    public static Intent getLaunchIntent(Context context, ArrayList<String> uris, int position) {
        return new Intent(context, ImageViewerActivity.class)
                .putExtra(EXTRA_URIS, uris)
                .putExtra(EXTRA_POSITION, position);
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);
        mViewPager = findViewById(R.id.pager);
        final ArrayList<String> uris = getIntent().getStringArrayListExtra(EXTRA_URIS);
        int position = getIntent().getIntExtra(EXTRA_POSITION, 0);
        mPagerIndicator = findViewById(R.id.pager_indicator);
        mViewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return ImageFragment.getInstance(uris.get(position));
            }

            @Override
            public int getCount() {
                return uris.size();
            }
        });


        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mPagerIndicator.setText(getString(R.string.image_full_screen_indicator_format_string,
                        position + 1
                        , uris.size()));
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        if (savedInstanceState == null) {
            mViewPager.setCurrentItem(position);
        }

        mPagerIndicator.setText(getString(R.string.image_full_screen_indicator_format_string
                , mViewPager.getCurrentItem() + 1
                , uris.size()));

        findViewById(R.id.back).setOnClickListener(v -> finish());
    }


    public static class ImageFragment extends TeambrellaFragment {

        public static final String EXTRA_URI = "uri";
        protected com.ortiz.touch.TouchImageView _imageView;

        public static ImageFragment getInstance(String uri) {
            ImageFragment fragment = new ImageFragment();
            Bundle args = new Bundle();
            args.putString(EXTRA_URI, uri);
            fragment.setArguments(args);
            return fragment;
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            _imageView = (com.ortiz.touch.TouchImageView) inflater.inflate(R.layout.fragment_image_fullscreen, container, false);
    
            GlideApp.with(this)
                    .asBitmap()
                    .load(getImageLoader().getImageUrl(getArguments().getString(EXTRA_URI)))
                    .apply(new RequestOptions().override(1600, 1600))
                    //.placeholder(R.drawable.picture_background_circle)
                    //.transition(DrawableTransitionOptions.withCrossFade())
                    .into(new BitmapImageViewTarget(_imageView) {
                        @Override
                        public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                            super.onResourceReady(resource, transition);
                            _imageView.setImageBitmap(resource);
                            _imageView.setZoom(1);
                        }
                    });
            
            return _imageView;
        }
    }


}
