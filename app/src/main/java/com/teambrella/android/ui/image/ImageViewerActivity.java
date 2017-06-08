package com.teambrella.android.ui.image;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.teambrella.android.R;
import com.teambrella.android.image.TeambrellaImageLoader;

import java.util.ArrayList;

/**
 *
 */
public class ImageViewerActivity extends AppCompatActivity {

    private static final String EXTRA_URIS = "uris";

    private ViewPager mViewPager;


    public static Intent getLaunchIntent(Context context, ArrayList<String> uris) {
        return new Intent(context, ImageViewerActivity.class).putExtra(EXTRA_URIS, uris);
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);
        supportPostponeEnterTransition();
        mViewPager = (ViewPager) findViewById(R.id.pager);
        final ArrayList<String> uris = getIntent().getStringArrayListExtra(EXTRA_URIS);
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
    }


    public static class ImageFragment extends Fragment {

        public static final String EXTRA_URI = "uri";

        public static ImageFragment getInstance(String uri) {
            ImageFragment fragment = new ImageFragment();
            Bundle args = new Bundle();
            args.putString(EXTRA_URI, uri);
            fragment.setArguments(args);
            return fragment;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            ImageView imageView = (ImageView) inflater.inflate(R.layout.fragment_image_fullscreen, container, false);
            imageView.setTransitionName(getArguments().getString(EXTRA_URI));
            TeambrellaImageLoader.getInstance(getActivity()).getPicasso().load(getArguments().getString(EXTRA_URI)).into(imageView, new Callback() {
                @Override
                public void onSuccess() {
                    getActivity().supportStartPostponedEnterTransition();
                }

                @Override
                public void onError() {
                    getActivity().supportStartPostponedEnterTransition();
                }
            });
            return imageView;
        }
    }


}
