package com.teambrella.android.ui.team;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.squareup.picasso.Picasso;
import com.teambrella.android.BuildConfig;
import com.teambrella.android.R;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.server.TeambrellaServer;
import com.teambrella.android.api.server.TeambrellaUris;
import com.teambrella.android.data.base.IDataPager;
import com.teambrella.android.ui.base.TeambrellaDataPagerAdapter;
import com.teambrella.android.ui.teammate.TeammateActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Teammates Recycler Adapter
 */
public class TeammatesRecyclerAdapter extends TeambrellaDataPagerAdapter {

    private static final int VIEW_TYPE_HEADER_TEAMMATES = VIEW_TYPE_REGULAR + 1;

    /**
     * Constructor.
     */
    TeammatesRecyclerAdapter(IDataPager<JsonArray> pager) {
        super(pager);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = super.onCreateViewHolder(parent, viewType);
        if (viewHolder == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            switch (viewType) {
                case VIEW_TYPE_REGULAR:
                    viewHolder = new TeammatesViewHolder(inflater.inflate(R.layout.list_item_teammate, parent, false));
                    break;
                case VIEW_TYPE_HEADER_TEAMMATES:
                    viewHolder = new RecyclerView.ViewHolder(inflater.inflate(R.layout.list_item_teammates_header, parent, false)) {
                    };
            }
        }
        return viewHolder;
    }

    @Override
    public int getItemViewType(int position) {

        if (position == 0) {
            return VIEW_TYPE_HEADER_TEAMMATES;
        }

        int size = mPager.getLoadedData().size() + 1;

        if (position == size) {
            if (mPager.hasNextError()) {
                return VIEW_TYPE_ERROR;
            } else if (mPager.hasNext() || mPager.isNextLoading()) {
                return VIEW_TYPE_LOADING;
            } else {
                throw new RuntimeException();
            }
        }

        return VIEW_TYPE_REGULAR;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        super.onBindViewHolder(holder, position);
        if (holder instanceof TeammatesViewHolder) {
            TeammatesViewHolder tholder = (TeammatesViewHolder) holder;
            final Context context = holder.itemView.getContext();
            final JsonObject item = mPager.getLoadedData().get(position - 1).getAsJsonObject();

            final String userPictureUri = TeambrellaServer.AUTHORITY + item.get(TeambrellaModel.ATTR_DATA_AVATAR).getAsString();
            Picasso.with(context).load(userPictureUri).into(tholder.mIcon);
            tholder.mTitle.setText(item.get(TeambrellaModel.ATTR_DATA_NAME).getAsString());
            tholder.mObject.setText(item.get(TeambrellaModel.ATTR_DATA_MODEL).getAsString());
            Long net = Math.round(item.get(TeambrellaModel.ATTR_DATA_TOTALLY_PAID).getAsDouble());
            if (net > 0) {
                tholder.mNet.setText(Html.fromHtml(context.getString(R.string.teammate_net_format_string_plus, Math.abs(net))));
            } else if (net < 0) {
                tholder.mNet.setText(Html.fromHtml(context.getString(R.string.teammate_net_format_string_minus, Math.abs(net))));
            } else {
                tholder.mNet.setText(context.getString(R.string.teammate_net_format_string_zero));
            }

            if (position == mPager.getLoadedData().size()) {
                tholder.mDivider.setVisibility(View.INVISIBLE);
            }

            tholder.mRisk.setText(context.getString(R.string.risk_format_string, item.get(TeambrellaModel.ATTR_DATA_RISK).getAsFloat()));

            holder.itemView.setOnClickListener(v -> context.startActivity(TeammateActivity.getIntent(context, TeambrellaUris.getTeammateUri(BuildConfig.TEAM_ID,
                    item.get(TeambrellaModel.ATTR_DATA_USER_ID).getAsString()), item.get(TeambrellaModel.ATTR_DATA_NAME).getAsString(), userPictureUri)));
        }
    }


    @Override
    protected boolean hasHeader() {
        return true;
    }

    static class TeammatesViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.icon)
        ImageView mIcon;
        @BindView(R.id.teammate)
        TextView mTitle;
        @BindView(R.id.object)
        TextView mObject;
        @BindView(R.id.net)
        TextView mNet;
        @BindView(R.id.divider)
        View mDivider;
        @BindView(R.id.indicator)
        TextView mRisk;

        TeammatesViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
