package com.teambrella.android.data.base;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.google.gson.JsonObject;

import io.reactivex.Notification;
import io.reactivex.Observable;

/**
 * Teambrella Data Fragment
 */
public class TeambrellaDataFragment extends Fragment {

    private static final String EXTRA_URI = "uri";


    private Uri mUri;
    private TeambrellaDataLoader mLoader;

    public static TeambrellaDataFragment getInstance(Uri uri) {
        TeambrellaDataFragment fragment = new TeambrellaDataFragment();
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_URI, uri);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mLoader = new TeambrellaDataLoader(getContext());
        mUri = getArguments().getParcelable(EXTRA_URI);
    }


    public void load() {
        mLoader.load(mUri, null);
    }


    public Observable<Notification<JsonObject>> getObservable() {
        return mLoader.getObservable();
    }
}
