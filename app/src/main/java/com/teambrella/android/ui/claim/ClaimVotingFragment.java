package com.teambrella.android.ui.claim;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.squareup.picasso.Picasso;
import com.teambrella.android.R;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.json.JsonWrapper;
import com.teambrella.android.api.server.TeambrellaServer;
import com.teambrella.android.image.TeambrellaImageLoader;
import com.teambrella.android.ui.base.ADataFragment;
import com.teambrella.android.ui.votes.AllVotesActivity;
import com.teambrella.android.ui.widget.TeambrellaAvatarsWidgets;
import com.teambrella.android.util.AmountCurrencyUtil;
import com.teambrella.android.util.TeambrellaDateUtils;

import io.reactivex.Notification;
import io.reactivex.Observable;

/**
 * Claim Voting fragment
 */

public class ClaimVotingFragment extends ADataFragment<IClaimActivity> implements SeekBar.OnSeekBarChangeListener {


    private TextView mTeamVotePercents;
    private TextView mYourVotePercents;
    private TextView mTeamVoteCurrency;
    private TextView mYourVoteCurrency;
    private SeekBar mVotingControl;
    private float mClaimAmount;
    private TextView mWhen;
    private TextView mProxyName;
    private ImageView mProxyAvatar;
    private TeambrellaAvatarsWidgets mAvatarWidgets;
    private View mRestVoteButton;
    private View mAllVotesView;


    private String mCurrency;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_claim_voting, container, false);

        mTeamVotePercents = view.findViewById(R.id.team_vote_percent);
        mYourVotePercents = view.findViewById(R.id.your_vote_percent);
        mVotingControl = view.findViewById(R.id.voting_control);
        mTeamVoteCurrency = view.findViewById(R.id.team_vote_currency);
        mYourVoteCurrency = view.findViewById(R.id.your_vote_currency);
        mProxyName = view.findViewById(R.id.proxy_name);
        mProxyAvatar = view.findViewById(R.id.proxy_avatar);
        mAvatarWidgets = view.findViewById(R.id.team_avatars);
        mWhen = view.findViewById(R.id.when);
        mRestVoteButton = view.findViewById(R.id.reset_vote_btn);
        mAllVotesView = view.findViewById(R.id.all_votes);

        mVotingControl.setOnSeekBarChangeListener(this);
        mVotingControl.setMax(100);


        mRestVoteButton.setOnClickListener(v -> {
            mDataHost.postVote(-1);
            mYourVotePercents.setAlpha(0.3f);
            mYourVoteCurrency.setAlpha(0.3f);
            mRestVoteButton.setAlpha(0.3f);
        });

        return view;
    }

    @Override
    protected void onDataUpdated(Notification<JsonObject> notification) {
        if (notification.isOnNext()) {
            Picasso picasso = TeambrellaImageLoader.getInstance(getContext()).getPicasso();
            JsonWrapper response = new JsonWrapper(notification.getValue());
            JsonWrapper data = response.getObject(TeambrellaModel.ATTR_DATA);
            JsonWrapper basic = data.getObject(TeambrellaModel.ATTR_DATA_ONE_BASIC);
            JsonWrapper voting = data.getObject(TeambrellaModel.ATTR_DATA_ONE_VOTING);
            JsonWrapper team = data.getObject(TeambrellaModel.ATTR_DATA_ONE_TEAM);

            if (basic != null) {
                mClaimAmount = basic.getFloat(TeambrellaModel.ATTR_DATA_CLAIM_AMOUNT, mClaimAmount);
            }


            mCurrency = team != null ? team.getString(TeambrellaModel.ATTR_DATA_CURRENCY, mCurrency) : mCurrency;

            if (voting != null) {

                float teamVote = voting.getFloat(TeambrellaModel.ATTR_DATA_RATIO_VOTED, -1);
                float yourVote = voting.getFloat(TeambrellaModel.ATTR_DATA_MY_VOTE, -1);
                String proxyName = voting.getString(TeambrellaModel.ATTR_DATA_PROXY_NAME);
                String proxyAvatar = voting.getString(TeambrellaModel.ATTR_DATA_PROXY_AVATAR);


                if (teamVote >= 0) {
                    mTeamVotePercents.setText(Html.fromHtml(getString(R.string.vote_in_percent_format_string, (int) (teamVote * 100))));
                    AmountCurrencyUtil.setAmount(mTeamVoteCurrency, mClaimAmount * teamVote, mCurrency);
                    mTeamVoteCurrency.setVisibility(View.VISIBLE);

                } else {
                    mTeamVotePercents.setText(R.string.no_teammate_vote_value);
                    mTeamVoteCurrency.setVisibility(View.INVISIBLE);
                }


                if (yourVote >= 0) {
                    mYourVotePercents.setText(Html.fromHtml(getString(R.string.vote_in_percent_format_string, (int) (yourVote * 100))));
                    mVotingControl.setProgress((int) (yourVote * 100));
                    AmountCurrencyUtil.setAmount(mYourVoteCurrency, mClaimAmount * yourVote, mCurrency);
                    mYourVoteCurrency.setVisibility(View.VISIBLE);
                } else {
                    mYourVotePercents.setText(R.string.no_teammate_vote_value);
                    mYourVoteCurrency.setVisibility(View.INVISIBLE);
                    mVotingControl.setProgress(0);
                }


                mYourVotePercents.setAlpha(1f);
                mYourVoteCurrency.setAlpha(1f);
                mRestVoteButton.setAlpha(1f);

                mWhen.setText(getContext().getString(R.string.ends_in, TeambrellaDateUtils.getRelativeTimeLocalized(getContext(), voting.getInt(TeambrellaModel.ATTR_DATA_REMAINED_MINUTES))));

                Observable.
                        fromIterable(voting.getJsonArray(TeambrellaModel.ATTR_DATA_OTHER_AVATARS))
                        .map(jsonElement -> TeambrellaServer.BASE_URL + jsonElement.getAsString())
                        .toList()
                        .subscribe(mAvatarWidgets::setAvatars);

                if (proxyName != null && proxyAvatar != null) {
                    mProxyName.setText(proxyName);
                    picasso.load(TeambrellaModel.getImage(TeambrellaServer.BASE_URL, voting.getObject(), TeambrellaModel.ATTR_DATA_PROXY_AVATAR))
                            .into(mProxyAvatar);
                    mProxyName.setVisibility(View.VISIBLE);
                    mProxyAvatar.setVisibility(View.VISIBLE);
                    mRestVoteButton.setVisibility(View.INVISIBLE);
                } else {
                    mProxyName.setVisibility(View.INVISIBLE);
                    mProxyAvatar.setVisibility(View.INVISIBLE);
                    mRestVoteButton.setVisibility(yourVote > 0 ? View.VISIBLE : View.INVISIBLE);
                }


                mAvatarWidgets.setOnClickListener(view -> AllVotesActivity.startClaimAllVotes(view.getContext(), mDataHost.getTeamId(), mDataHost.getClaimId()));
                mAllVotesView.setOnClickListener(v -> AllVotesActivity.startClaimAllVotes(getContext(), mDataHost.getTeamId(), data.getInt(TeambrellaModel.ATTR_DATA_ID)));
            }

        }
    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            mYourVotePercents.setText(Html.fromHtml(getString(R.string.vote_in_percent_format_string, progress)));
            AmountCurrencyUtil.setAmount(mYourVoteCurrency, (mClaimAmount * progress) / 100, mCurrency);
            mYourVotePercents.setAlpha(0.3f);
            mYourVoteCurrency.setAlpha(0.3f);
            mRestVoteButton.setAlpha(0.3f);
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
