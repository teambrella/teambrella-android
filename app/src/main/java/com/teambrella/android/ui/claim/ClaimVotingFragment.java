package com.teambrella.android.ui.claim;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.format.DateUtils;
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
import com.teambrella.android.ui.widget.TeambrellaAvatarsWidgets;
import com.teambrella.android.util.AmountCurrencyUtil;

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

            if (basic != null) {
                mClaimAmount = basic.getFloat(TeambrellaModel.ATTR_DATA_CLAIM_AMOUNT, mClaimAmount);
            }


            if (voting != null) {

                float teamVote = voting.getFloat(TeambrellaModel.ATTR_DATA_RATIO_VOTED, 0);
                float yourVote = voting.getFloat(TeambrellaModel.ATTR_DATA_MY_VOTE, 0);
                String proxyName = voting.getString(TeambrellaModel.ATTR_DATA_PROXY_NAME);
                String proxyAvatar = voting.getString(TeambrellaModel.ATTR_DATA_PROXY_AVATAR);

                mTeamVotePercents.setText(Html.fromHtml(getString(R.string.vote_in_percent_format_string, (int) (teamVote * 100))));
                mYourVotePercents.setText(Html.fromHtml(getString(R.string.vote_in_percent_format_string, (int) (yourVote * 100))));
                mVotingControl.setProgress((int) (yourVote * 100));
                AmountCurrencyUtil.setAmount(mTeamVoteCurrency, mClaimAmount * teamVote, "USD");
                AmountCurrencyUtil.setAmount(mYourVoteCurrency, mClaimAmount * yourVote, "USD");
                mYourVotePercents.setAlpha(1f);
                mYourVoteCurrency.setAlpha(1f);
                mRestVoteButton.setAlpha(1f);

                long now = System.currentTimeMillis();
                long when = now + 60000 * voting.getInt(TeambrellaModel.ATTE_DATA_REMAINED_MINUTES);
                mWhen.setText(DateUtils.getRelativeTimeSpanString(when, now, DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE));

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
            }

        }
    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        mYourVotePercents.setText(Html.fromHtml(getString(R.string.vote_in_percent_format_string, progress)));
        AmountCurrencyUtil.setAmount(mYourVoteCurrency, (mClaimAmount * progress) / 100, "USD");
        if (fromUser) {
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
