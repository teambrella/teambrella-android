package com.teambrella.android.ui.teammate;

import android.os.Bundle;
import android.support.annotation.Nullable;
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

import java.util.Locale;

import io.reactivex.Notification;

/**
 * Teammate Voting Fragment
 */
public class TeammateVotingFragment extends ADataFragment<ITeammateActivity> implements SeekBar.OnSeekBarChangeListener {


    private TextView mTeamVoteRisk;
    private TextView mMyVoteRisk;
    private SeekBar mVotingControl;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_teammate_voting, container, false);

        mTeamVoteRisk = (TextView) view.findViewById(R.id.team_vote_risk);
        mMyVoteRisk = (TextView) view.findViewById(R.id.your_vote_risk);
        mVotingControl = (SeekBar) view.findViewById(R.id.voting_control);
        mVotingControl.setMax(1000);
        mVotingControl.setOnSeekBarChangeListener(this);
        return view;
    }

    @Override
    protected void onDataUpdated(Notification<JsonObject> notification) {
        if (notification.isOnNext()) {
            JsonWrapper response = new JsonWrapper(notification.getValue());
            JsonWrapper data = response.getObject(TeambrellaModel.ATTR_DATA);
            JsonWrapper voting = data.getObject(TeambrellaModel.ATTR_DATA_ONE_VOTING);
            if (voting != null) {
                mTeamVoteRisk.setText(voting.getString(TeambrellaModel.ATTR_DATA_RISK_VOTED));
                mMyVoteRisk.setText(String.format(Locale.US, "%.2f", voting.getFloat(TeambrellaModel.ATTR_DATA_MY_VOTE, 0f)));
                mVotingControl.setProgress(riskToProgress(voting.getDouble(TeambrellaModel.ATTR_DATA_MY_VOTE, 0f)));
            }
        }
    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            mMyVoteRisk.setText(String.format(Locale.US, "%.2f", progressToRisk(progress)));
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mDataHost.postVote(progressToRisk(seekBar.getProgress()));
    }


    private static double progressToRisk(int progress) {
        return Math.pow(25, (double) progress / 1000) / 5;
    }

    private static int riskToProgress(double risk) {
        return (int) Math.round(Math.log(risk * 5) / Math.log(25) * 1000);
    }
}
