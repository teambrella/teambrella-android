package com.teambrella.android.ui.teammate;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.teambrella.android.R;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.json.JsonWrapper;
import com.teambrella.android.image.glide.GlideApp;
import com.teambrella.android.ui.TeambrellaUser;
import com.teambrella.android.ui.base.ADataFragment;
import com.teambrella.android.ui.teammates.RiskRange;
import com.teambrella.android.ui.teammates.TeammatesByRiskActivity;
import com.teambrella.android.ui.votes.AllVotesActivity;
import com.teambrella.android.ui.widget.CountDownClock;
import com.teambrella.android.ui.widget.FadeInFadeOutViewController;
import com.teambrella.android.ui.widget.TeambrellaAvatarsWidgets;
import com.teambrella.android.ui.widget.TeammateVoteRisk;
import com.teambrella.android.ui.widget.VoterBar;
import com.teambrella.android.util.TeambrellaDateUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

import io.reactivex.Notification;
import io.reactivex.Observable;

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
    private TextView mAVGDifferenceTeamVote;
    private TextView mAVGDifferenceMyVote;
    private ArrayList<JsonWrapper> mRanges;
    private View mRestVoteButton;
    private TextView mProxyName;
    private ImageView mProxyAvatar;
    private TeambrellaAvatarsWidgets mAvatarWidgets;
    private float mAVGRisk;
    private View mAllVotesView;
    private View mOthersView;
    private TextView mYourVoteTitle;
    private TextView mWhen;
    private CountDownClock mClock;
    private FadeInFadeOutViewController mSwipeToVoteViewController;

    private int mCount;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_teammate_voting, container, false);

        mVoterBar = view.findViewById(R.id.voter_bar);
        mLeftTeammateIcon = view.findViewById(R.id.left_teammate_icon);
        mRightTeammateIcon = view.findViewById(R.id.right_teammate_icon);
        mNewTeammateIcon = view.findViewById(R.id.new_teammate_icon);
        mLeftTeammateRisk = view.findViewById(R.id.left_teammate_risk);
        mRightTeammateRisk = view.findViewById(R.id.right_teammate_risk);
        mNewTeammateRisk = view.findViewById(R.id.new_teammate_risk);
        TeammateVoteRisk teamVoteView = view.findViewById(R.id.team_vote_risk);
        mAVGDifferenceTeamVote = teamVoteView.avgDifference;
        mTeamVoteRisk = teamVoteView.risk;

        TeammateVoteRisk yourVoteView = view.findViewById(R.id.your_vote_risk);
        mAVGDifferenceMyVote = yourVoteView.avgDifference;
        mMyVoteRisk = yourVoteView.risk;
        mYourVoteTitle = yourVoteView.title;

        mRestVoteButton = view.findViewById(R.id.reset_vote_btn);
        mProxyName = view.findViewById(R.id.proxy_name);
        mProxyAvatar = view.findViewById(R.id.proxy_avatar);
        mAvatarWidgets = view.findViewById(R.id.team_avatars);
        mAllVotesView = view.findViewById(R.id.all_votes);
        mOthersView = view.findViewById(R.id.others);
        mWhen = view.findViewById(R.id.when);
        mClock = view.findViewById(R.id.clock);
        final View swipeToVoteView = view.findViewById(R.id.swipe_to_vote_foreground);
        swipeToVoteView.setVisibility(TeambrellaUser.get(getContext()).isSwipeToVoteShown() ? View.GONE : View.VISIBLE);
        mSwipeToVoteViewController = new FadeInFadeOutViewController(swipeToVoteView, 250);
        mVoterBar.setVoterBarListener(this);

        mYourVoteTitle.setOnClickListener(mShowSwipeToVoteForeground);
        mMyVoteRisk.setOnClickListener(mShowSwipeToVoteForeground);
        mAVGDifferenceMyVote.setOnClickListener(mShowSwipeToVoteForeground);
        mProxyAvatar.setOnClickListener(mShowSwipeToVoteForeground);
        mProxyName.setOnClickListener(mShowSwipeToVoteForeground);
        mNewTeammateRisk.setOnClickListener(mShowSwipeToVoteForeground);
        mNewTeammateIcon.setOnClickListener(mShowSwipeToVoteForeground);


        mRestVoteButton.setOnClickListener(v -> {
            getDataHost().postVote(-1f);
            mCount++;
            setVoting(true);
            mSwipeToVoteViewController.hide();
        });

        return view;
    }

    @Override
    protected void onDataUpdated(@NonNull Notification<JsonObject> notification) {

        if (mCount > 0) {
            mCount--;
        }


        try {
            if (notification.isOnNext()) {
                JsonWrapper response = new JsonWrapper(notification.getValue());
                JsonWrapper data = response.getObject(TeambrellaModel.ATTR_DATA);
                JsonWrapper voting = data.getObject(TeambrellaModel.ATTR_DATA_ONE_VOTING);
                JsonWrapper riskScale = data.getObject(TeambrellaModel.ATTR_DATA_ONE_RISK_SCALE);


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
                    mAVGRisk = riskScale.getFloat(TeambrellaModel.ATTR_DATA_AVG_RISK);
                    mVoterBar.init(boxes, (float) riskFloatProgress(voting != null ? voting.getFloat(TeambrellaModel.ATTR_DATA_MY_VOTE) : -1), riskFloatProgress(mAVGRisk));
                }

                if (voting != null && mCount == 0 && !mVoterBar.isUserActive()) {
                    double teamVote = voting.getFloat(TeambrellaModel.ATTR_DATA_RISK_VOTED, -1f);
                    double myVote = voting.getFloat(TeambrellaModel.ATTR_DATA_MY_VOTE, -1f);
                    String proxyName = voting.getString(TeambrellaModel.ATTR_DATA_PROXY_NAME);
                    String proxyAvatar = voting.getString(TeambrellaModel.ATTR_DATA_PROXY_AVATAR);


                    if (teamVote > 0) {
                        mTeamVoteRisk.setText(String.format(Locale.US, "%.2f", teamVote));
                        mAVGDifferenceTeamVote.setVisibility(View.VISIBLE);
                        setAVGDifference(teamVote, mAVGRisk, mAVGDifferenceTeamVote);
                    } else {
                        mTeamVoteRisk.setText(R.string.no_teammate_vote_value);
                        mAVGDifferenceTeamVote.setVisibility(View.INVISIBLE);
                    }

                    if (myVote > 0) {
                        mMyVoteRisk.setText(String.format(Locale.US, "%.2f", myVote));
                        setAVGDifference(myVote, mAVGRisk, mAVGDifferenceMyVote);
                        mAVGDifferenceMyVote.setVisibility(View.VISIBLE);
                        mVoterBar.setVote((float) riskFloatProgress(myVote));
                        mNewTeammateRisk.setText(String.format(Locale.US, "%.2f", myVote));
                    } else {
                        mAVGDifferenceMyVote.setVisibility(View.INVISIBLE);
                        mMyVoteRisk.setText(R.string.no_teammate_vote_value);
                        mVoterBar.setVote((float) riskFloatProgress(mAVGRisk));
                        mNewTeammateRisk.setText(String.format(Locale.US, "%.2f", mAVGRisk));
                    }

                    if (proxyName != null && proxyAvatar != null) {
                        mProxyName.setText(proxyName);
                        GlideApp.with(this).load(getImageLoader().getImageUrl(voting.getString(TeambrellaModel.ATTR_DATA_PROXY_AVATAR)))
                                .into(mProxyAvatar);
                        mProxyName.setVisibility(View.VISIBLE);
                        mProxyAvatar.setVisibility(View.VISIBLE);
                        mRestVoteButton.setVisibility(View.INVISIBLE);
                        mYourVoteTitle.setText(R.string.proxy_vote_title);
                    } else {
                        mProxyName.setVisibility(View.INVISIBLE);
                        mProxyAvatar.setVisibility(View.INVISIBLE);
                        mRestVoteButton.setVisibility(myVote > 0 ? View.VISIBLE : View.INVISIBLE);
                        mYourVoteTitle.setText(R.string.your_vote);
                    }


                    mWhen.setText(getContext().getString(R.string.ends_in, TeambrellaDateUtils.getRelativeTimeLocalized(getContext()
                            , voting.getInt(TeambrellaModel.ATTR_DATA_REMAINED_MINUTES))));

                    mClock.setRemainedMinutes(voting.getInt(TeambrellaModel.ATTR_DATA_REMAINED_MINUTES));


                    int otherCount = voting.getInt(TeambrellaModel.ATTR_DATA_OTHER_COUNT);

                    Observable.
                            fromIterable(voting.getJsonArray(TeambrellaModel.ATTR_DATA_OTHER_AVATARS))
                            .map(JsonElement::getAsString)
                            .toList()
                            .subscribe(uris -> mAvatarWidgets.setAvatars(getImageLoader(), uris, otherCount));

                    setVoting(false);
                }


                JsonWrapper basic = data.getObject(TeambrellaModel.ATTR_DATA_ONE_BASIC);

                if (basic != null) {
                    GlideApp.with(this).load(getImageLoader().getImageUrl(basic.getString(TeambrellaModel.ATTR_DATA_AVATAR)))
                            .into(mNewTeammateIcon);
                }


                mAllVotesView.setOnClickListener(view -> AllVotesActivity.startTeammateAllVotes(getContext(), getDataHost().getTeamId(), getDataHost().getTeammateId()));
            }
        } catch (Exception e) {

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


    private static void setAVGDifference(double vote, double average, TextView view) {
        long percent = Math.round(((vote - average) / average) * 100);
        if (percent > 0) {
            view.setText(view.getContext().getResources().getString(R.string.vote_avg_difference_bigger_format_string, percent));
        } else if (percent < 0) {
            view.setText(view.getContext().getResources().getString(R.string.vote_avg_difference_smaller_format_string, percent));
        } else {
            view.setText(R.string.vote_avg_difference_same);
        }
    }


    private void setVoting(boolean isVoting) {
        mMyVoteRisk.setAlpha(isVoting ? 0.3f : 1f);
        mRestVoteButton.setAlpha(isVoting ? 0.3f : 1f);
        mRestVoteButton.setEnabled(!isVoting);
    }


    @Override
    public void onVoteChanged(float vote, boolean fromUser) {
        double value = Math.pow(25, vote) / 5;

        if (fromUser) {
            setVoting(true);
            mMyVoteRisk.setText(String.format(Locale.US, "%.2f", value));
            setAVGDifference((float) value, mAVGRisk, mAVGDifferenceMyVote);
            mAVGDifferenceMyVote.setVisibility(View.VISIBLE);
            mProxyName.setVisibility(View.INVISIBLE);
            mProxyAvatar.setVisibility(View.INVISIBLE);
            mRestVoteButton.setVisibility(View.VISIBLE);
            mYourVoteTitle.setText(R.string.your_vote);
            mSwipeToVoteViewController.hide();
            TeambrellaUser.get(getContext()).setSwipeToVoteShown();
        }

        mNewTeammateRisk.setText(String.format(Locale.US, "%.2f", value));

        for (JsonWrapper interval : mRanges) {
            float left = interval.getFloat(TeambrellaModel.ATTR_DATA_LEFT_RANGE);
            float right = interval.getFloat(TeambrellaModel.ATTR_DATA_RIGHT_RANGE);
            if (value >= left && value < right) {
                ArrayList<JsonWrapper> teammates = interval.getArray(TeambrellaModel.ATTR_DATA_TEAMMTES_IN_RANGE);
                Iterator<JsonWrapper> it = teammates.iterator();

                if (it.hasNext()) {
                    JsonWrapper item = it.next();
                    mLeftTeammateIcon.setVisibility(View.VISIBLE);
                    GlideApp.with(this).load(getImageLoader().getImageUrl(item.getString(TeambrellaModel.ATTR_DATA_AVATAR)))
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
                    GlideApp.with(this).load(getImageLoader().getImageUrl(item.getString(TeambrellaModel.ATTR_DATA_AVATAR)))
                            .into(mRightTeammateIcon);
                    mRightTeammateRisk.setVisibility(View.VISIBLE);
                    mRightTeammateRisk.setText(String.format(Locale.US, "%.2f", item.getFloat(TeambrellaModel.ATTR_DATA_RISK)));
                } else {
                    mRightTeammateIcon.setVisibility(View.INVISIBLE);
                    mRightTeammateRisk.setVisibility(View.INVISIBLE);
                }

                mOthersView.setVisibility(teammates.size() > 0 ? View.VISIBLE : View.GONE);

                mOthersView.setOnClickListener(v -> {
                    ArrayList<RiskRange> list = new ArrayList<>();
                    for (JsonWrapper range : mRanges) {
                        if (range.getInt(TeambrellaModel.ATTR_DATA_COUNT, 0) > 0) {
                            list.add(new RiskRange(range.getFloat(TeambrellaModel.ATTR_DATA_LEFT_RANGE),
                                    range.getFloat(TeambrellaModel.ATTR_DATA_RIGHT_RANGE)));
                        }
                    }
                    TeammatesByRiskActivity.start(getContext(), getDataHost().getTeamId(), list, (float) value);
                });

                break;
            }
        }

        Fragment fragment = getParentFragment();

        if (fragment instanceof VoterBar.VoterBarListener) {
            ((VoterBar.VoterBarListener) fragment).onVoteChanged(vote, fromUser);
        }
    }

    @Override
    public void onVoterBarReleased(float vote, boolean fromUser) {
        if (fromUser) {
            getDataHost().postVote(Math.pow(25, vote) / 5);
            mCount++;
        }

        Fragment fragment = getParentFragment();

        if (fragment instanceof VoterBar.VoterBarListener) {
            ((VoterBar.VoterBarListener) fragment).onVoterBarReleased(vote, fromUser);
        }
    }

    private View.OnClickListener mShowSwipeToVoteForeground = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (TeambrellaUser.get(getContext()).isSwipeToVoteShown()) {
                mSwipeToVoteViewController.toggle();
            }
        }
    };
}
