package com.teambrella.android.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.teambrella.android.R;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.json.JsonWrapper;
import com.teambrella.android.data.base.IDataPager;
import com.teambrella.android.image.glide.GlideApp;
import com.teambrella.android.ui.base.ATeambrellaDialogFragment;
import com.teambrella.android.ui.base.TeambrellaDataPagerAdapter;
import com.teambrella.android.util.StatisticHelper;

import java.util.Locale;

import io.reactivex.Notification;
import io.reactivex.disposables.Disposable;

/**
 * Team selection dialog fragment
 */
public class TeamSelectionFragment extends ATeambrellaDialogFragment {


    private ProgressBar mProggres;
    private RecyclerView mList;
    private IMainDataHost mDataHost;
    private Disposable mDisposable;
    private View mContent;
    private TeambrellaUser mUser;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mDataHost = (IMainDataHost) context;
        mUser = TeambrellaUser.get(context);
    }


    @Override
    public void onStart() {
        super.onStart();
        mDataHost.getPager(MainActivity.TEAMS_DATA).getDataObservable().observeForever(observer);
    }

    @Override
    public void onStop() {
        super.onStop();
        mDataHost.getPager(MainActivity.TEAMS_DATA).getDataObservable().removeObserver(observer);
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
        mContent = view.findViewById(R.id.content);
        mList.setAdapter(new TeamsAdapter(mDataHost.getPager(MainActivity.TEAMS_DATA)));
        mList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mProggres.setVisibility(View.VISIBLE);

        View exitDemoView = view.findViewById(R.id.exit_demo);

        if (!mUser.isDemoUser()) {
            exitDemoView.setVisibility(View.GONE);
        } else {
            exitDemoView.setOnClickListener(v -> {
                mUser.resetDemoUser();
                StatisticHelper.setUserId(null);
                startActivity(new Intent(getContext(), WelcomeActivity.class));
                getActivity().finish();
            });
        }

        Dialog dialog = new AlertDialog.Builder(getContext()).setView(view).create();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }


    private void onDataUpdated(Notification<JsonObject> notification) {
        if (notification.isOnNext()) {
            mProggres.setVisibility(View.GONE);
            mContent.setVisibility(View.VISIBLE);
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

        @Override
        protected RecyclerView.ViewHolder createBottomViewHolder(ViewGroup parent) {
            return new RecyclerView.ViewHolder(new View(parent.getContext())) {
            };
        }
    }


    private class TeamViewHolder extends RecyclerView.ViewHolder {
        private ImageView mIcon;
        private TextView mTitle;
        private TextView mObject;
        private TextView mCoverage;
        private View mCurrentTeamMark;

        TeamViewHolder(View itemView) {
            super(itemView);
            mIcon = itemView.findViewById(R.id.team_icon);
            mTitle = itemView.findViewById(R.id.team_title);
            mObject = itemView.findViewById(R.id.object);
            mCoverage = itemView.findViewById(R.id.coverage);
            mCurrentTeamMark = itemView.findViewById(R.id.current_team_mark);
        }

        void onBind(JsonWrapper item) {
            mTitle.setText(item.getString(TeambrellaModel.ATTR_DATA_TEAM_NAME));
            mObject.setText(item.getString(TeambrellaModel.ATTR_DATA_OBJECT_NAME));
            mCoverage.setText(String.format(Locale.US, "%d%%", Math.round(100 * item.getFloat(TeambrellaModel.ATTR_DATA_OBJECT_COVERAGE))));
            GlideApp.with(itemView).load(getTeambrellaImageLoader().getImageUrl((item.getString(TeambrellaModel.ATTR_DATA_TEAM_LOGO))))
                    .apply(new RequestOptions().transforms(new CenterCrop(), new RoundedCorners(getResources().getDimensionPixelOffset(R.dimen.rounded_corners_4dp)))
                            .placeholder(R.drawable.picture_background_round_4dp)).into(mIcon);

            mCurrentTeamMark.setVisibility(mDataHost.getTeamId() != item.getInt(TeambrellaModel.ATTR_DATA_TEAM_ID) ? View.INVISIBLE : View.VISIBLE);

            itemView.setOnClickListener(v -> {
                if (mDataHost.getTeamId() != item.getInt(TeambrellaModel.ATTR_DATA_TEAM_ID)) {
                    getActivity().finish();
                    startActivity(MainActivity.getLaunchIntent(getContext()
                            , mDataHost.getUserId()
                            , item.getObject().toString()));
                    TeambrellaUser.get(getContext()).setTeamId(item.getInt(TeambrellaModel.ATTR_DATA_TEAM_ID));
                } else {
                    dismiss();
                }
            });


        }

    }

    private Observer<Notification<JsonObject>> observer = this::onDataUpdated;
}
