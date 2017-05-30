package com.teambrella.android.ui.team;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.teambrella.android.R;
import com.teambrella.android.data.base.IDataPager;
import com.teambrella.android.ui.IMainDataHost;
import com.teambrella.android.ui.base.ProgressFragment;

import io.reactivex.Notification;
import io.reactivex.disposables.Disposable;

/**
 * Members fragment
 */
public class MembersFragment extends ProgressFragment {

    private static final String LOG_TAG = MembersFragment.class.getSimpleName();

    private IMainDataHost mDataHost;
    private Disposable mDisposable;
    private RecyclerView mList;
    private TeammatesRecyclerAdapter mAdapter;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mDataHost = (IMainDataHost) context;
    }

    @Override
    protected View onCreateContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_team_members, container, false);
        mList = (RecyclerView) view.findViewById(R.id.list);
        mList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mAdapter = new TeammatesRecyclerAdapter(mDataHost.getTeamListPager());
        mList.setAdapter(mAdapter);

        IDataPager<JsonArray> pager = mDataHost.getTeamListPager();
        mDisposable = pager.getObservable()
                .subscribe(this::onDataUpdated);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        IDataPager<JsonArray> pager = mDataHost.getTeamListPager();
        if (pager.getLoadedData().size() == 0 && pager.hasNext()) {
            pager.loadNext();
            setContentShown(false);
        } else {
            setContentShown(true);
        }

    }

    private void onDataUpdated(Notification<JsonArray> notification) {
        if (notification.isOnNext()) {

        } else {
            Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
        }
        setContentShown(true);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mDisposable != null && !mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
        mAdapter.destroy();
        mList = null;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mDataHost = null;
    }
}
