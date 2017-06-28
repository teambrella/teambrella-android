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
import com.teambrella.android.ui.base.ADataFragment;
import com.teambrella.android.ui.widget.AmountWidget;

import io.reactivex.Notification;

/**
 * Claim Voting fragment
 */

public class ClaimVotingFragment extends ADataFragment<IClaimActivity> implements SeekBar.OnSeekBarChangeListener {


    private TextView mTeamVotePercents;
    private TextView mYourVotePercents;
    private AmountWidget mTeamVoteCurrency;
    private AmountWidget mYourVoteCurrency;
    private SeekBar mVotingControl;
    private float mClaimAmount;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_claim_voting, container, false);

        mTeamVotePercents = (TextView) view.findViewById(R.id.team_vote_percent);
        mYourVotePercents = (TextView) view.findViewById(R.id.your_vote_percent);
        mVotingControl = (SeekBar) view.findViewById(R.id.voting_control);
        mTeamVoteCurrency = (AmountWidget) view.findViewById(R.id.team_vote_currency);
        mYourVoteCurrency = (AmountWidget) view.findViewById(R.id.your_vote_currency);
        mVotingControl.setOnSeekBarChangeListener(this);
        mVotingControl.setMax(100);
        return view;
    }

    @Override
    protected void onDataUpdated(Notification<JsonObject> notification) {
        if (notification.isOnNext()) {
            JsonWrapper response = new JsonWrapper(notification.getValue());
            JsonWrapper data = response.getObject(TeambrellaModel.ATTR_DATA);
            JsonWrapper basic = data.getObject(TeambrellaModel.ATTR_DATA_ONE_BASIC);

            if (basic != null) {
                mClaimAmount = basic.getFloat(TeambrellaModel.ATTR_DATA_CLAIM_AMOUNT, mClaimAmount);
            }

            float teamVote = data.getObject(TeambrellaModel.ATTR_DATA_ONE_VOTING).getFloat(TeambrellaModel.ATTR_DATA_RATIO_VOTED, 0);
            float yourVote = data.getObject(TeambrellaModel.ATTR_DATA_ONE_VOTING).getFloat(TeambrellaModel.ATTR_DATA_MY_VOTE, 0);

            mTeamVotePercents.setText(Html.fromHtml(getString(R.string.vote_in_percent_format_string, (int) (teamVote * 100))));
            mYourVotePercents.setText(Html.fromHtml(getString(R.string.vote_in_percent_format_string, (int) (yourVote * 100))));
            mVotingControl.setProgress((int) (yourVote * 100));
            mTeamVoteCurrency.setAmount(mClaimAmount * teamVote);
            mYourVoteCurrency.setAmount(mClaimAmount * yourVote);
            mYourVotePercents.setAlpha(1f);
            mYourVoteCurrency.setAlpha(1f);
        }
    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        mYourVotePercents.setText(Html.fromHtml(getString(R.string.vote_in_percent_format_string, progress)));
        mYourVoteCurrency.setAmount((mClaimAmount * progress) / 100);
        if (fromUser) {
            mYourVotePercents.setAlpha(0.3f);
            mYourVoteCurrency.setAlpha(0.3f);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mDataHost.postVote(seekBar.getProgress());
    }
}
