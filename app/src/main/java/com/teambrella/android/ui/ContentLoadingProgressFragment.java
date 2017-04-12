package com.teambrella.android.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.teambrella.android.R;

/**
 * Basic content loading fragment
 */
public class ContentLoadingProgressFragment extends Fragment {

    /**
     * Progress.
     */
    private View mProgress;

    /**
     * Content
     */
    private ViewGroup mContent;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_loading, container, false);
        mProgress = view.findViewById(R.id.fragment_content_progress);
        mContent = (ViewGroup) view.findViewById(R.id.fragment_content);
        return view;
    }

    /**
     * Set content.
     *
     * @param resource layoutId
     */
    protected View setContent(int resource) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View view = inflater.inflate(resource, mContent, false);
        mContent.addView(view);
        return view;
    }


    /**
     * Set content shown.
     *
     * @param shown whether or not the content shall be shown
     */
    protected void setContentShown(boolean shown) {
        mContent.setVisibility(shown ? View.VISIBLE : View.GONE);
        mProgress.setVisibility(shown ? View.GONE : View.VISIBLE);
    }
}
