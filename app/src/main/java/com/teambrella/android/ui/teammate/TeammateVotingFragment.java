package com.teambrella.android.ui.teammate;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.squareup.picasso.Picasso;
import com.teambrella.android.R;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.json.JsonWrapper;
import com.teambrella.android.api.server.TeambrellaServer;
import com.teambrella.android.image.TeambrellaImageLoader;
import com.teambrella.android.ui.base.ADataFragment;
import com.teambrella.android.ui.widget.VoterBar;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

import io.reactivex.Notification;

/**
 * Teammate Voting Fragment
 */
public class TeammateVotingFragment extends ADataFragment<ITeammateActivity> implements VoterBar.VoterBarListener {


    private TextView mTeamVoteRisk;
    private TextView mMyVoteRisk;
    private VoterBar mVoterBar;
    private ImageView mLeftTeammateIcon;
    private ImageView mRightTeammateIcon;
    private TextView mLeftTeammateRisk;
    private TextView mRightTeammateRisk;
    private ImageView mNewTeammateIcon;
    private TextView mNewTeammateRisk;
    private ArrayList<JsonWrapper> mRanges;
    //private SeekBar mVotingControl;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_teammate_voting, container, false);

        mTeamVoteRisk = (TextView) view.findViewById(R.id.team_vote_risk);
        mMyVoteRisk = (TextView) view.findViewById(R.id.your_vote_risk);
        mVoterBar = (VoterBar) view.findViewById(R.id.voter_bar);
        mLeftTeammateIcon = (ImageView) view.findViewById(R.id.left_teammate_icon);
        mRightTeammateIcon = (ImageView) view.findViewById(R.id.right_teammate_icon);
        mNewTeammateIcon = (ImageView) view.findViewById(R.id.new_teammate_icon);
        mLeftTeammateRisk = (TextView) view.findViewById(R.id.left_teammate_risk);
        mRightTeammateRisk = (TextView) view.findViewById(R.id.right_teammate_risk);
        mNewTeammateRisk = (TextView) view.findViewById(R.id.new_teammate_risk);
        mVoterBar.setVoterBarListener(this);
        //mVotingControl = (SeekBar) view.findViewById(R.id.voting_control);
        //mVotingControl.setMax(1000);
        //mVotingControl.setOnSeekBarChangeListener(this);

        return view;
    }

    @Override
    protected void onDataUpdated(Notification<JsonObject> notification) {
        if (notification.isOnNext()) {
            JsonWrapper response = new JsonWrapper(notification.getValue());
            JsonWrapper data = response.getObject(TeambrellaModel.ATTR_DATA);
            JsonWrapper voting = data.getObject(TeambrellaModel.ATTR_DATA_ONE_VOTING);
            JsonWrapper riskScale = data.getObject(TeambrellaModel.ATTR_DATA_ONE_RISK_SCALE);
            if (voting != null) {
                mTeamVoteRisk.setText(voting.getString(TeambrellaModel.ATTR_DATA_RISK_VOTED));
                mMyVoteRisk.setText(String.format(Locale.US, "%.2f", voting.getFloat(TeambrellaModel.ATTR_DATA_MY_VOTE, 0f)));
                //mVotingControl.setProgress(riskToProgress(voting.getDouble(TeambrellaModel.ATTR_DATA_MY_VOTE, 0f)));
            }


            if (riskScale != null) {
                mRanges = riskScale.getArray(TeambrellaModel.ATTR_DATA_RANGES);

                VoterBar.VoterBox[] boxes = new VoterBar.VoterBox[mRanges.size()];

                for (int i = 0; i < boxes.length; i++) {
                    JsonWrapper range = mRanges.get(i);
                    float left = range.getFloat(TeambrellaModel.ATTR_DATA_LEFT_RANGE);
                    float right = range.getFloat(TeambrellaModel.ATTR_DATA_RIGHT_RANGE);
                    float value = left + (right - left) / 2;
                    int count = range.getInt(TeambrellaModel.ATTR_DATA_COUNT);
                    boxes[i] = new VoterBar.VoterBox(riskFloatProgress(left), riskFloatProgress(right), value, count);
                }

                mVoterBar.init(boxes);
            }

            JsonWrapper basic = data.getObject(TeambrellaModel.ATTR_DATA_ONE_BASIC);

            if (basic != null) {
                Picasso picasso = TeambrellaImageLoader.getInstance(getContext()).getPicasso();
                picasso.load(TeambrellaModel.getImage(TeambrellaServer.AUTHORITY, basic.getObject(), TeambrellaModel.ATTR_DATA_AVATAR))
                        .into(mNewTeammateIcon);
            }


        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mVoterBar.setVoterBarListener(null);
    }

    private static double progressToRisk(int progress) {
        return Math.pow(25, (double) progress / 1000) / 5;
    }

    private static int riskToProgress(double risk) {
        return (int) Math.round(Math.log(risk * 5) / Math.log(25) * 1000);
    }


    private static double riskFloatProgress(double risk) {
        return (Math.log(risk * 5) / Math.log(25));
    }


    @Override
    public void onVoteChanged(float vote, boolean fromUser) {
        double value = Math.pow(25, vote) / 5;
        mMyVoteRisk.setText(String.format(Locale.US, "%.2f", value));
        mNewTeammateRisk.setText(String.format(Locale.US, "%.2f", value));
        Picasso picasso = TeambrellaImageLoader.getInstance(getContext()).getPicasso();
        for (JsonWrapper interval : mRanges) {
            float left = interval.getFloat(TeambrellaModel.ATTR_DATA_LEFT_RANGE);
            float right = interval.getFloat(TeambrellaModel.ATTR_DATA_RIGHT_RANGE);
            if (value >= left && value < right) {
                ArrayList<JsonWrapper> teammates = interval.getArray(TeambrellaModel.ATTR_DATA_TEAMMTES_IN_RANGE);
                Iterator<JsonWrapper> it = teammates.iterator();

                if (it.hasNext()) {
                    JsonWrapper item = it.next();
                    mLeftTeammateIcon.setVisibility(View.VISIBLE);
                    picasso.load(TeambrellaModel.getImage(TeambrellaServer.AUTHORITY, item.getObject(), TeambrellaModel.ATTR_DATA_AVATAR))
                            .into(mLeftTeammateIcon);
                    mLeftTeammateRisk.setVisibility(View.VISIBLE);
                    mLeftTeammateRisk.setText(String.format(Locale.US, "%.2f", item.getFloat(TeambrellaModel.ATTR_DATA_RISK)));
                } else {
                    mLeftTeammateIcon.setVisibility(View.INVISIBLE);
                    mLeftTeammateRisk.setVisibility(View.INVISIBLE);
                }

                if (it.hasNext()) {
                    JsonWrapper item = it.next();
                    mRightTeammateIcon.setVisibility(View.VISIBLE);
                    picasso.load(TeambrellaModel.getImage(TeambrellaServer.AUTHORITY, item.getObject(), TeambrellaModel.ATTR_DATA_AVATAR))
                            .into(mRightTeammateIcon);
                    mRightTeammateRisk.setVisibility(View.VISIBLE);
                    mRightTeammateRisk.setText(String.format(Locale.US, "%.2f", item.getFloat(TeambrellaModel.ATTR_DATA_RISK)));
                } else {
                    mRightTeammateIcon.setVisibility(View.INVISIBLE);
                    mRightTeammateRisk.setVisibility(View.INVISIBLE);
                }
                break;
            }
        }


    }
}
