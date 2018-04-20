package com.teambrella.android.data.base;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.google.gson.JsonObject;
import com.teambrella.android.ui.base.TeambrellaDataHostActivity;

import io.reactivex.Notification;
import io.reactivex.Observable;

/**
 * Teambrella Data Fragment
 */
public class TeambrellaDataFragment extends Fragment {

    private static final String EXTRA_URI = "uri";
    private static final String EXTRA_LOAD_ON_CREATE = "load_on_create";


    private Uri mUri;
    private TeambrellaDataLoader mLoader;

    public static TeambrellaDataFragment getInstance(Uri uri) {
        TeambrellaDataFragment fragment = new TeambrellaDataFragment();
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_URI, uri);
        fragment.setArguments(args);
        return fragment;
    }

    public static TeambrellaDataFragment getInstance(Uri uri, boolean loadOnCreate) {
        TeambrellaDataFragment fragment = new TeambrellaDataFragment();
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_URI, uri);
        args.putBoolean(EXTRA_LOAD_ON_CREATE, loadOnCreate);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        Context context = getContext();
        mLoader = new TeambrellaDataLoader();
        ((TeambrellaDataHostActivity) context).getComponent().inject(mLoader);
        Bundle args = getArguments();
        mUri = args.getParcelable(EXTRA_URI);
        if (args.getBoolean(EXTRA_LOAD_ON_CREATE, false)) {
            load();
        }

    }


    public void load() {
        mLoader.load(mUri, null);
    }

    public void load(Uri uri) {
        mLoader.load(uri, null);
    }


    public void setUri(Uri uri) {
        mUri = uri;
    }


    public Observable<Notification<JsonObject>> getObservable() {
        return mLoader.getObservable();
    }
}
