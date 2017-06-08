package com.teambrella.android.ui.widget;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.teambrella.android.R;
import com.teambrella.android.image.TeambrellaImageLoader;
import com.teambrella.android.ui.image.ImageViewerActivity;

import java.util.ArrayList;

/**
 * Image Pager
 */
public class ImagePager extends FrameLayout {

    private ViewPager mPager;

    public ImagePager(@NonNull Context context) {
        super(context);
        init();
    }

    public ImagePager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ImagePager(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private void init() {
        inflate(getContext(), R.layout.widget_image_pager, this);
        mPager = (ViewPager) findViewById(R.id.pager);
    }


    public void init(FragmentManager fragmentManager, final ArrayList<String> uris) {
        mPager.setAdapter(new FragmentPagerAdapter(fragmentManager) {
            @Override
            public Fragment getItem(int position) {
                return ImageFragment.getInstance(uris.get(position), uris);
            }

            @Override
            public int getCount() {
                return uris.size();
            }
        });
    }


    public static final class ImageFragment extends Fragment {

        private static final String EXTRA_URI = "uri";
        private static final String EXTRA_URIS = "uris";

        public static ImageFragment getInstance(String uri, ArrayList<String> uris) {
            ImageFragment fragment = new ImageFragment();
            Bundle args = new Bundle();
            args.putString(EXTRA_URI, uri);
            args.putStringArrayList(EXTRA_URIS, uris);
            fragment.setArguments(args);
            return fragment;
        }


        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            ImageView imageView = (ImageView) inflater.inflate(R.layout.fragment_image, container, false);
            TeambrellaImageLoader.getInstance(getActivity()).getPicasso().load(getArguments().getString(EXTRA_URI)).into(imageView);
            imageView.setOnClickListener(v-> v.getContext().startActivity(ImageViewerActivity.getLaunchIntent(v.getContext(), getArguments().getStringArrayList(EXTRA_URIS))));
            return imageView;
        }
    }
}
