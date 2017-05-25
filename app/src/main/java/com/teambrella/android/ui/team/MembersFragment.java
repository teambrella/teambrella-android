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

import com.google.gson.JsonObject;
import com.teambrella.android.R;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.data.teammates.TeammatesRecyclerAdapter;
import com.teambrella.android.ui.IMainDataHost;
import com.teambrella.android.ui.base.ProgressFragment;

import io.reactivex.disposables.Disposable;

/**
 * Members fragment
 */
public class MembersFragment extends ProgressFragment {

    private static final String LOG_TAG = MembersFragment.class.getSimpleName();

    private IMainDataHost mDataHost;
    private Disposable mDisposable;
    private RecyclerView mList;

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

        if (savedInstanceState == null) {
            mDataHost.requestTeamList(2006);
        }

        mDisposable = mDataHost.getTeamListObservable()
                .subscribe(this::onDataUpdated, this::onError);

        return view;
    }

    private void onDataUpdated(JsonObject data) {
        mList.setAdapter(new TeammatesRecyclerAdapter(data.get(TeambrellaModel.ATTR_DATA)
                .getAsJsonObject().get(TeambrellaModel.ATTR_DATA_TEAMMATES).getAsJsonArray()));
        setContentShown(true);
    }

    private void onError(Throwable e) {
        Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
        setContentShown(true);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mDisposable != null && !mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
        mList = null;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mDataHost = null;
    }
}
