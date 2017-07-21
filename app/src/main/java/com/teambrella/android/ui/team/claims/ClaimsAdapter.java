package com.teambrella.android.ui.team.claims;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
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
import com.teambrella.android.image.TeambrellaImageLoader;
import com.teambrella.android.ui.base.TeambrellaDataPagerAdapter;
import com.teambrella.android.ui.claim.ClaimActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Claims Adapter
 */
public class ClaimsAdapter extends TeambrellaDataPagerAdapter {


    private final int mTeamId;


    /**
     * Constructor
     *
     * @param pager pager
     */
    public ClaimsAdapter(IDataPager<JsonArray> pager, int teamId) {
        super(pager);
        mTeamId = teamId;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = super.onCreateViewHolder(parent, viewType);
        if (viewHolder == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            switch (viewType) {
                case VIEW_TYPE_REGULAR:
                    viewHolder = new ClaimViewHolder(inflater.inflate(R.layout.list_item_claim, parent, false));
                    break;
            }
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        if (holder instanceof ClaimViewHolder) {
            ClaimViewHolder claimViewHolder = (ClaimViewHolder) holder;
            final Context context = holder.itemView.getContext();
            JsonObject item = mPager.getLoadedData().get(position).getAsJsonObject();
            final String objectPictureUri = TeambrellaServer.BASE_URL + item.get(TeambrellaModel.ATTR_DATA_SMALL_PHOTO).getAsString();
            final String teammatePictureUri = TeambrellaServer.BASE_URL + item.get(TeambrellaModel.ATTR_DATA_AVATAR).getAsString();
            Picasso picasso = TeambrellaImageLoader.getInstance(context).getPicasso();

            picasso.load(objectPictureUri).into(claimViewHolder.mIcon);
            picasso.load(teammatePictureUri).into(claimViewHolder.mTeammateIcon);
            claimViewHolder.mTitle.setText(item.get(TeambrellaModel.ATTR_DATA_MODEL).getAsString());
            claimViewHolder.mObject.setText(item.get(TeambrellaModel.ATTR_DATA_NAME).getAsString());
            claimViewHolder.mNet.setText("$" + Math.round(item.get(TeambrellaModel.ATTR_DATA_CLAIM_AMOUNT).getAsDouble()));
            claimViewHolder.itemView.setOnClickListener(v -> context.startActivity(
                    ClaimActivity.getLaunchIntent(context, TeambrellaUris.getClaimUri(item.get(TeambrellaModel.ATTR_DATA_ID).getAsInt()),
                            item.get(TeambrellaModel.ATTR_DATA_MODEL).getAsString(), mTeamId)));
        }
    }

    static class ClaimViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.icon)
        ImageView mIcon;
        @BindView(R.id.object)
        TextView mTitle;
        @BindView(R.id.teammate)
        TextView mObject;
        @BindView(R.id.value)
        TextView mNet;
        @BindView(R.id.teammate_picture)
        ImageView mTeammateIcon;

        public ClaimViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
