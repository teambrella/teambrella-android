package com.teambrella.android.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.teambrella.android.R;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.json.JsonWrapper;
import com.teambrella.android.data.base.IDataPager;
import com.teambrella.android.image.TeambrellaImageLoader;
import com.teambrella.android.ui.base.TeambrellaDataPagerAdapter;

import io.reactivex.Notification;
import io.reactivex.disposables.Disposable;

/**
 * Team selection dialog fragment
 */
public class TeamSelectionFragment extends DialogFragment {


    private ProgressBar mProggres;
    private RecyclerView mList;
    private IMainDataHost mDataHost;
    private Disposable mDisposable;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mDataHost = (IMainDataHost) context;
    }


    @Override
    public void onStart() {
        super.onStart();
        mDisposable = mDataHost.getPager(MainActivity.TEAMS_DATA).getObservable().subscribe(this::onDataUpdated);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mDisposable != null && !mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        if (savedInstanceState == null) {
            mDataHost.getPager(MainActivity.TEAMS_DATA).reload();
        }

        View view = View.inflate(getContext(), R.layout.dialog_team_selection, null);
        mProggres = view.findViewById(R.id.progress);
        mList = view.findViewById(R.id.list);
        mList.setAdapter(new TeamsAdapter(mDataHost.getPager(MainActivity.TEAMS_DATA)));
        mList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mProggres.setVisibility(View.VISIBLE);
        mList.setVisibility(View.GONE);
        return new AlertDialog.Builder(getContext()).setTitle("Choose your team")
                .setView(view).create();
    }


    private void onDataUpdated(Notification<JsonObject> notification) {
        if (notification.isOnNext()) {
            mProggres.setVisibility(View.GONE);
            mList.setVisibility(View.VISIBLE);
        } else {
            dismiss();
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mDataHost = null;
    }


    private class TeamsAdapter extends TeambrellaDataPagerAdapter {
        TeamsAdapter(IDataPager<JsonArray> pager) {
            super(pager);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            RecyclerView.ViewHolder viewHolder = super.onCreateViewHolder(parent, viewType);
            if (viewHolder == null) {
                viewHolder = new TeamViewHolder(LayoutInflater.from(getContext()).inflate(R.layout.list_item_team, parent, false));
            }

            return viewHolder;
        }


        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            super.onBindViewHolder(holder, position);
            if (holder instanceof TeamViewHolder) {
                ((TeamViewHolder) holder).onBind(new JsonWrapper(mPager.getLoadedData().get(position).getAsJsonObject()));
            }
        }
    }


    private class TeamViewHolder extends RecyclerView.ViewHolder {
        private ImageView mIcon;
        private TextView mTitle;

        TeamViewHolder(View itemView) {
            super(itemView);
            mIcon = itemView.findViewById(R.id.team_icon);
            mTitle = itemView.findViewById(R.id.team_title);
        }

        void onBind(JsonWrapper item) {
            mTitle.setText(item.getString(TeambrellaModel.ATTR_DATA_TEAM_NAME));
            TeambrellaImageLoader.getInstance(getContext()).getPicasso()
                    .load(TeambrellaImageLoader.getImageUri(item.getString(TeambrellaModel.ATTR_DATA_TEAM_LOGO)))
                    .into(mIcon);

            itemView.setOnClickListener(v -> {
                getActivity().finish();
                startActivity(MainActivity.getLaunchIntent(getContext()
                        , mDataHost.getUserId()
                        , item.getObject().toString()));
                TeambrellaUser.get(getContext()).setTeamId(item.getInt(TeambrellaModel.ATTR_DATA_TEAM_ID));
            });


        }

    }
}