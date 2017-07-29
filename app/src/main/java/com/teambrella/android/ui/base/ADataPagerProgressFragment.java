package com.teambrella.android.ui.base;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
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
    private SwipeRefreshLayout mRefreshable;

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
        View view = inflater.inflate(getContentLayout(), container, false);
        mList = (RecyclerView) view.findViewById(R.id.list);
        new ItemTouchHelper(new ItemTouchCallback()).attachToRecyclerView(mList);
        mRefreshable = (SwipeRefreshLayout) view.findViewById(R.id.refreshable);
        mList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false)

        {
            @Override
            public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State
                    state) {
                try {
                    super.onLayoutChildren(recycler, state);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        mAdapter = getAdapter();
        mList.setAdapter(mAdapter);
        return view;
    }

    protected @LayoutRes
    int getContentLayout() {
        return R.layout.fragment_list;
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

        mRefreshable.setOnRefreshListener(pager::reload);
        mRefreshable.setColorSchemeColors(getResources().getColor(R.color.colorPrimary));

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

    protected void onDataUpdated(Notification<JsonObject> notification) {
        if (notification.isOnNext()) {
        } else {
            Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
        }
        setContentShown(true);
        mRefreshable.setRefreshing(false);
    }

    protected boolean isLongPressDragEnabled() {
        return false;
    }

    protected void onDraggingFinished(RecyclerView.ViewHolder viewHolder) {

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


    protected void setRefreshable(@SuppressWarnings("SameParameterValue") boolean refreshable) {
        mRefreshable.setEnabled(refreshable);
    }

    protected void setRefreshing(@SuppressWarnings("SameParameterValue") boolean refreshing) {
        mRefreshable.setRefreshing(refreshing);
    }

    protected abstract TeambrellaDataPagerAdapter getAdapter();


    private class ItemTouchCallback extends ItemTouchHelper.SimpleCallback {

        ItemTouchCallback() {
            super(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0);
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return ADataPagerProgressFragment.this.isLongPressDragEnabled();
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            mAdapter.exchangeItems(viewHolder, target);
            return true;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            // nothing to do
        }

        @Override
        public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
            super.onSelectedChanged(viewHolder, actionState);
            switch (actionState) {
                case ItemTouchHelper.ACTION_STATE_DRAG:
                    viewHolder.itemView.setAlpha(0.5f);
                    break;
            }
        }

        @Override
        public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            super.clearView(recyclerView, viewHolder);
            viewHolder.itemView.setAlpha(1f);
            ADataPagerProgressFragment.this.onDraggingFinished(viewHolder);
        }
    }

}
