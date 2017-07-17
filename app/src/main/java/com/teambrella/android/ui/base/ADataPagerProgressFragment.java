package com.teambrella.android.ui.base;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.teambrella.android.R;
import com.teambrella.android.data.base.IDataHost;
import com.teambrella.android.data.base.IDataPager;

import io.reactivex.Notification;
import io.reactivex.disposables.Disposable;

/**
 * Base data pager progress fragment
 */
public abstract class ADataPagerProgressFragment<T extends IDataHost> extends ProgressFragment {

    public static final String EXTRA_PAGER_FRAGMENT_TAG = "pager_fragment_tag";

    protected T mDataHost;
    private Disposable mDisposable;
    protected RecyclerView mList;
    protected TeambrellaDataPagerAdapter mAdapter;
    protected String mTag;

    public static <T extends ADataPagerProgressFragment> T getInstance(String tag, Class<T> clazz) {
        T fragment;
        try {
            fragment = clazz.newInstance();
        } catch (java.lang.InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("unable to create fragment");
        }

        Bundle args = new Bundle();
        args.putString(EXTRA_PAGER_FRAGMENT_TAG, tag);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTag = getArguments().getString(EXTRA_PAGER_FRAGMENT_TAG);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mDataHost = (T) context;
    }

    @Override
    protected View onCreateContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);
        mList = (RecyclerView) view.findViewById(R.id.list);
        mList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mAdapter = getAdapter();
        mList.setAdapter(mAdapter);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        IDataPager<JsonArray> pager = mDataHost.getPager(mTag);
        if (pager.getLoadedData().size() == 0 && pager.hasNext()) {
            pager.loadNext(false);
            setContentShown(false);
        } else {
            setContentShown(true);
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        IDataPager<JsonArray> pager = mDataHost.getPager(mTag);
        mDisposable = pager.getObservable()
                .subscribe(this::onDataUpdated);
    }


    @Override
    public void onStop() {
        super.onStop();
        if (mDisposable != null && !mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
    }

    protected void onDataUpdated(Notification<Pair<Integer, JsonArray>> notification) {
        if (notification.isOnNext()) {

        } else {
            Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
        }
        setContentShown(true);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mAdapter.destroy();
        mList = null;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mDataHost = null;
    }

    protected abstract TeambrellaDataPagerAdapter getAdapter();

}
