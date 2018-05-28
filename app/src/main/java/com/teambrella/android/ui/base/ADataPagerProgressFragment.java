package com.teambrella.android.ui.base;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.teambrella.android.BuildConfig;
import com.teambrella.android.R;
import com.teambrella.android.data.base.IDataHost;
import com.teambrella.android.data.base.IDataPager;
import com.teambrella.android.util.log.Log;

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
    protected ATeambrellaDataPagerAdapter mAdapter;
    protected String mTag;
    protected ItemTouchHelper mItemTouchHelper;

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
        mList = view.findViewById(R.id.list);
        (mItemTouchHelper = new ItemTouchHelper(new ItemTouchCallback())).attachToRecyclerView(mList);
        mList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false) {
            @Override
            public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State
                    state) {
                try {
                    super.onLayoutChildren(recycler, state);
                } catch (Throwable e) {
                    if (BuildConfig.DEBUG) {
                        Log.e("TEST", e.toString());
                    }
                }
            }
        });
        mAdapter = getAdapter();
        ((ITeambrellaDaggerActivity) (getContext())).getComponent().inject(mAdapter);
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
        if (pager.getLoadedData().size() == 0 && pager.getHasNext()) {
            pager.reload();
            setContentShown(false);
        } else {
            setContentShown(true);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        IDataPager<JsonArray> pager = mDataHost.getPager(mTag);
        mDisposable = pager.getDataObservable()
                .subscribe(this::onDataUpdated);
    }

    @Override
    protected void onReload() {
        mDataHost.getPager(mTag).reload();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mDisposable != null && !mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
    }

    protected void onDataUpdated(Notification<JsonObject> notification) {
        setContentShown(true);
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


    protected abstract ATeambrellaDataPagerAdapter getAdapter();


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
                    //viewHolder.itemView.setAlpha(0.9f);
                    setRefreshable(false);
                    break;
            }
        }

        @Override
        public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            super.clearView(recyclerView, viewHolder);
            //viewHolder.itemView.setAlpha(1f);
            ADataPagerProgressFragment.this.onDraggingFinished(viewHolder);
            setRefreshable(true);
        }
    }

}
