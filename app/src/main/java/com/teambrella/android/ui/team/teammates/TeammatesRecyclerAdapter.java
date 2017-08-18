package com.teambrella.android.ui.team.teammates;

import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.teambrella.android.R;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.json.JsonWrapper;
import com.teambrella.android.data.base.IDataPager;
import com.teambrella.android.image.TeambrellaImageLoader;
import com.teambrella.android.ui.base.TeambrellaDataPagerAdapter;
import com.teambrella.android.ui.teammate.TeammateActivity;
import com.teambrella.android.util.AmountCurrencyUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Notification;
import io.reactivex.Observable;

/**
 * Teammates Recycler Adapter
 */
public class TeammatesRecyclerAdapter extends TeambrellaDataPagerAdapter {

    public static final int VIEW_TYPE_TEAMMATE = VIEW_TYPE_REGULAR;
    public static final int VIEW_TYPE_NEW_MEMBER = VIEW_TYPE_REGULAR + 1;
    public static final int VIEW_TYPE_HEADER_TEAMMATES = VIEW_TYPE_REGULAR + 2;
    public static final int VIEW_TYPE_HEADER_NEW_MEMBERS = VIEW_TYPE_REGULAR + 3;

    private final int mTeamId;
    private final String mCurrency;

    /**
     * Constructor.
     */
    TeammatesRecyclerAdapter(IDataPager<JsonArray> pager, int teamId, String currency) {
        super(pager);
        mTeamId = teamId;
        mCurrency = currency;

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = super.onCreateViewHolder(parent, viewType);
        if (viewHolder == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            switch (viewType) {
                case VIEW_TYPE_TEAMMATE:
                    viewHolder = new TeammatesViewHolder(inflater.inflate(R.layout.list_item_teammate, parent, false));
                    break;
                case VIEW_TYPE_NEW_MEMBER:
                    viewHolder = new NewMemberViewHolder(inflater.inflate(R.layout.list_item_new_teamate, parent, false));
                    break;
                case VIEW_TYPE_HEADER_TEAMMATES:
                    viewHolder = new Header(parent, R.string.teammates, R.string.net);
                    break;
                case VIEW_TYPE_HEADER_NEW_MEMBERS:
                    viewHolder = new Header(parent, R.string.new_teammates, R.string.voting_ends_title);
                    break;
            }
        }
        return viewHolder;
    }

    @Override
    public int getItemViewType(int position) {
        int type = super.getItemViewType(position);
        if (type == VIEW_TYPE_REGULAR) {
            JsonObject item = mPager.getLoadedData().get(position).getAsJsonObject();
            switch (item.get(TeambrellaModel.ATTR_DATA_ITEM_TYPE).getAsInt()) {
                case TeambrellaModel.ATTR_DATA_ITEM_TYPE_SECTION_NEW_MEMBERS:
                    return VIEW_TYPE_HEADER_NEW_MEMBERS;
                case TeambrellaModel.ATTR_DATA_ITEM_TYPE_SECTION_TEAMMATES:
                    return VIEW_TYPE_HEADER_TEAMMATES;
                case TeambrellaModel.ATTR_DATA_ITEM_TYPE_TEAMMATE:
                    if (item.get(TeambrellaModel.ATTR_DATA_IS_VOTING).getAsBoolean()) {
                        return VIEW_TYPE_NEW_MEMBER;
                    }
                    return VIEW_TYPE_TEAMMATE;
            }
        }
        return type;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        super.onBindViewHolder(holder, position);
        if (holder instanceof ATeammateViewHolder) {
            ATeammateViewHolder aHolder = (ATeammateViewHolder) holder;
            aHolder.onBind(new JsonWrapper(mPager.getLoadedData().get(position).getAsJsonObject()));
        }
    }


    @Override
    protected boolean hasHeader() {
        return false;
    }


    abstract class ATeammateViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.icon)
        ImageView mIcon;
        @BindView(R.id.teammate)
        TextView mTitle;
        @BindView(R.id.object)
        TextView mObject;

        ATeammateViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void onBind(JsonWrapper item) {

            Observable.fromArray(item).map(json -> TeambrellaImageLoader.getImageUri(json.getString(TeambrellaModel.ATTR_DATA_AVATAR)))
                    .map(uri -> TeambrellaImageLoader.getInstance(itemView.getContext()).getPicasso().load(uri))
                    .subscribe(requestCreator -> requestCreator.into(mIcon), throwable -> {
                        // 8)
                    });

            String userPictureUri = Observable.fromArray(item).map(json -> Notification.createOnNext(json.getString(TeambrellaModel.ATTR_DATA_AVATAR)))
                    .blockingFirst().getValue();


            mTitle.setText(item.getString(TeambrellaModel.ATTR_DATA_NAME));
            mObject.setText(item.getString(TeambrellaModel.ATTR_DATA_MODEL));
            itemView.setOnClickListener(v -> TeammateActivity.start(itemView.getContext(), mTeamId,
                    item.getString(TeambrellaModel.ATTR_DATA_USER_ID), item.getString(TeambrellaModel.ATTR_DATA_NAME), userPictureUri, mCurrency));
        }
    }

    class TeammatesViewHolder extends ATeammateViewHolder {

        @BindView(R.id.net)
        TextView mNet;
        @BindView(R.id.indicator)
        TextView mRisk;

        TeammatesViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        void onBind(JsonWrapper item) {
            super.onBind(item);
            Long net = Math.round(item.getDouble(TeambrellaModel.ATTR_DATA_TOTALLY_PAID));
            if (net > 0) {
                mNet.setText(Html.fromHtml(itemView.getContext().getString(R.string.teammate_net_format_string_plus, AmountCurrencyUtil.getCurrencySign(mCurrency), Math.abs(net))));
            } else if (net < 0) {
                mNet.setText(Html.fromHtml(itemView.getContext().getString(R.string.teammate_net_format_string_minus, AmountCurrencyUtil.getCurrencySign(mCurrency), Math.abs(net))));
            } else {
                mNet.setText(itemView.getContext().getString(R.string.teammate_net_format_string_zero, AmountCurrencyUtil.getCurrencySign(mCurrency)));
            }

            mRisk.setText(itemView.getContext().getString(R.string.risk_format_string, item.getFloat(TeambrellaModel.ATTR_DATA_RISK)));
        }
    }


    class NewMemberViewHolder extends ATeammateViewHolder {
        NewMemberViewHolder(View itemView) {
            super(itemView);
        }
    }
}
