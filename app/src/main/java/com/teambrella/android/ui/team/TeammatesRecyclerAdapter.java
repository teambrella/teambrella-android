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
            }
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        super.onBindViewHolder(holder, position);
        if (holder instanceof TeammatesViewHolder) {
            TeammatesViewHolder tholder = (TeammatesViewHolder) holder;
            final Context context = holder.itemView.getContext();
            final JsonObject item = mPager.getLoadedData().get(position).getAsJsonObject();

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
            holder.itemView.setOnClickListener(v -> context.startActivity(TeammateActivity.getIntent(context, TeambrellaUris.getTeammateUri(2,
                    item.get(TeambrellaModel.ATTR_DATA_USER_ID).getAsString()), item.get(TeambrellaModel.ATTR_DATA_NAME).getAsString(), userPictureUri)));
        }
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

        TeammatesViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
