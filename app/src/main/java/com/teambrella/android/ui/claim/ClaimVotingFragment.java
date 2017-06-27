package com.teambrella.android.ui.claim;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.teambrella.android.R;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.json.JsonWrapper;
import com.teambrella.android.api.server.TeambrellaServer;
import com.teambrella.android.api.server.TeambrellaUris;
import com.teambrella.android.ui.TeambrellaUser;
import com.teambrella.android.ui.base.ADataFragment;

import io.reactivex.Notification;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Claim Voting fragment
 */

public class ClaimVotingFragment extends ADataFragment<IClaimActivity> implements SeekBar.OnSeekBarChangeListener {


    private TextView mTeamVotePercents;
    private TextView mYourVotePercents;
    private SeekBar mVotingControl;
    private int mCurentProgress;
    private int mClaimId;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_claim_voting, container, false);

        mTeamVotePercents = (TextView) view.findViewById(R.id.team_vote_percent);
        mYourVotePercents = (TextView) view.findViewById(R.id.your_vote_percent);
        mVotingControl = (SeekBar) view.findViewById(R.id.voting_control);
        mVotingControl.setOnSeekBarChangeListener(this);
        mVotingControl.setMax(100);
        return view;
    }

    @Override
    protected void onDataUpdated(Notification<JsonObject> notification) {
        if (notification.isOnNext()) {
            JsonWrapper response = new JsonWrapper(notification.getValue());
            JsonWrapper data = response.getObject(TeambrellaModel.ATTR_DATA);
            mClaimId = data.getInt(TeambrellaModel.ATTR_DATA_ID, 0);

            mTeamVotePercents.setText(Html.fromHtml(getString(R.string.vote_in_percent_format_string,
                    (int) (data.getObject(TeambrellaModel.ATTR_DATA_ONE_VOTING).getFloat(TeambrellaModel.ATTR_DATA_RATIO_VOTED, 0) * 100))));


            mYourVotePercents.setText(Html.fromHtml(getString(R.string.vote_in_percent_format_string,
                    (int) (data.getObject(TeambrellaModel.ATTR_DATA_ONE_VOTING).getFloat(TeambrellaModel.ATTR_DATA_MY_VOTE, 0) * 100))));


            mVotingControl.setProgress((int) (data.getObject(TeambrellaModel.ATTR_DATA_ONE_VOTING).getFloat(TeambrellaModel.ATTR_DATA_MY_VOTE, 0) * 100));


        }
    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        mYourVotePercents.setText(Html.fromHtml(getString(R.string.vote_in_percent_format_string, progress)));
        if (fromUser) {
            mYourVotePercents.setAlpha(0.3f);
        }
        mCurentProgress = progress;
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        new TeambrellaServer(getContext(),
                TeambrellaUser.get(getContext()).getPrivateKey()).requestObservable(TeambrellaUris.getClaimVoteUri(mClaimId, seekBar.getProgress()), null)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(jsonObject -> {
                    //Toast.makeText(getContext(), "Yes", Toast.LENGTH_SHORT).show();
                    mYourVotePercents.setAlpha(1f);
                });
    }
}
