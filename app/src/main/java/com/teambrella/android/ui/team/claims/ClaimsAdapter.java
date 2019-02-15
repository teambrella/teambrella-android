package com.teambrella.android.ui.team.claims;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.teambrella.android.R;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.json.JsonWrapper;
import com.teambrella.android.data.base.IDataPager;
import com.teambrella.android.image.glide.GlideApp;
import com.teambrella.android.ui.base.TeambrellaDataPagerAdapter;
import com.teambrella.android.ui.claim.ReportClaimActivity;

/**
 * Claims Adapter
 */
public class ClaimsAdapter extends TeambrellaDataPagerAdapter {


    static final int VIEW_TYPE_VOTING_HEADER = VIEW_TYPE_REGULAR + 1;
    static final int VIEW_TYPE_VOTING = VIEW_TYPE_REGULAR + 2;
    static final int VIEW_TYPE_VOTED_HEADER = VIEW_TYPE_REGULAR + 3;
    private static final int VIEW_TYPE_VOTED = VIEW_TYPE_REGULAR + 4;
    static final int VIEW_TYPE_IN_PAYMENT_HEADER = VIEW_TYPE_REGULAR + 5;
    private static final int VIEW_TYPE_IN_PAYMENT = VIEW_TYPE_REGULAR + 6;
    static final int VIEW_TYPE_PROCESSED_HEADER = VIEW_TYPE_REGULAR + 7;
    private static final int VIEW_TYPE_PROCESSED = VIEW_TYPE_REGULAR + 8;
    private static final int VIEW_TYPE_SUBMIT_CLAIM = VIEW_TYPE_REGULAR + 9;
    static final int VIEW_TYPE_VOTED_HEADER_TOP = VIEW_TYPE_REGULAR + 10;
    static final int VIEW_TYPE_IN_PAYMENT_HEADER_TOP = VIEW_TYPE_REGULAR + 11;
    static final int VIEW_TYPE_PROCESSED_HEADER_TOP = VIEW_TYPE_REGULAR + 12;


    private final int mTeamId;
    private final boolean mSubmitClaim;
    private final String mCurrency;
    private String mObjectImageUri;
    private String mObjectName;
    private String mLocation;


    /**
     * Constructor
     *
     * @param pager pager
     */
    ClaimsAdapter(IDataPager<JsonArray> pager, int teamId, String currency, boolean submitClaim, OnStartActivityListener listener) {
        super(pager, listener);
        mTeamId = teamId;
        mSubmitClaim = submitClaim;
        mCurrency = currency;
        setHasStableIds(true);
    }

    void setObjectDetails(String objectImageUri, String objectName, String location) {
        if (mSubmitClaim) {
            mObjectImageUri = objectImageUri;
            mObjectName = objectName;
            mLocation = location;
        }
        notifyItemChanged(0);
    }


    @Override
    public int getItemViewType(int position) {
        int viewType = super.getItemViewType(position);

        if (viewType == VIEW_TYPE_REGULAR) {
            if (mSubmitClaim && position == 0) {
                viewType = VIEW_TYPE_SUBMIT_CLAIM;
            } else {
                position = mSubmitClaim ? position - 1 : position;
                JsonObject item = mPager.getLoadedData().get(position).getAsJsonObject();
                switch (item.get(TeambrellaModel.ATTR_DATA_ITEM_TYPE).getAsString()) {
                    case TeambrellaModel.ClaimsListItemType.ITEM_VOTING_HEADER:
                        viewType = VIEW_TYPE_VOTING_HEADER;
                        break;
                    case TeambrellaModel.ClaimsListItemType.ITEM_VOTING:
                        viewType = VIEW_TYPE_VOTING;
                        break;
                    case TeambrellaModel.ClaimsListItemType.ITEM_VOTED_HEADER:
                        if (position == 0) {
                            viewType = VIEW_TYPE_VOTED_HEADER;
                        } else {
                            viewType = VIEW_TYPE_VOTED_HEADER_TOP;
                        }
                        break;
                    case TeambrellaModel.ClaimsListItemType.ITEM_VOTED:
                        viewType = VIEW_TYPE_VOTED;
                        break;
                    case TeambrellaModel.ClaimsListItemType.ITEM_IN_PAYMENT_HEADER:
                        if (position == 0 || getItemViewType(mSubmitClaim ? position : position - 1) != VIEW_TYPE_VOTING) {
                            viewType = VIEW_TYPE_IN_PAYMENT_HEADER;
                        } else {
                            viewType = VIEW_TYPE_IN_PAYMENT_HEADER_TOP;
                        }
                        break;
                    case TeambrellaModel.ClaimsListItemType.ITEM_IN_PAYMENT:
                        viewType = VIEW_TYPE_IN_PAYMENT;
                        break;
                    case TeambrellaModel.ClaimsListItemType.ITEM_PROCESSED_HEADER:
                        if (position == 0 || getItemViewType(mSubmitClaim ? position : position - 1) != VIEW_TYPE_VOTING) {
                            viewType = VIEW_TYPE_PROCESSED_HEADER;
                        } else {
                            viewType = VIEW_TYPE_PROCESSED_HEADER_TOP;
                        }
                        break;
                    case TeambrellaModel.ClaimsListItemType.ITEM_PROCESSED:
                        viewType = VIEW_TYPE_PROCESSED;
                        break;
                }
            }
        }
        return viewType;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = super.onCreateViewHolder(parent, viewType);
        if (viewHolder == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            switch (viewType) {
                case VIEW_TYPE_SUBMIT_CLAIM:
                    viewHolder = new SubmitClaimViewHolder(inflater.inflate(R.layout.list_item_submit_claim, parent, false));
                    break;
                case VIEW_TYPE_VOTING_HEADER:
                    viewHolder = new Header(parent, R.string.claim_header_voting, -1, R.drawable.list_item_header_background_top);
                    break;
                case VIEW_TYPE_VOTED_HEADER:
                case VIEW_TYPE_VOTED_HEADER_TOP:
                    viewHolder = new Header(parent, R.string.claim_header_voted, -1, viewType == VIEW_TYPE_VOTED_HEADER_TOP
                            ? R.drawable.list_item_header_background_top : R.drawable.list_item_header_background_middle);
                    break;
                case VIEW_TYPE_IN_PAYMENT_HEADER:
                case VIEW_TYPE_IN_PAYMENT_HEADER_TOP:
                    viewHolder = new Header(parent, R.string.claim_header_being_paid, -1, viewType == VIEW_TYPE_IN_PAYMENT_HEADER_TOP ?
                            R.drawable.list_item_header_background_top : R.drawable.list_item_header_background_middle);
                    break;
                case VIEW_TYPE_PROCESSED_HEADER:
                case VIEW_TYPE_PROCESSED_HEADER_TOP:
                    viewHolder = new Header(parent, R.string.claim_header_fully_paid, -1, viewType == VIEW_TYPE_PROCESSED_HEADER_TOP
                            ? R.drawable.list_item_header_background_top : R.drawable.list_item_header_background_middle);
                    break;
                case VIEW_TYPE_VOTING:
                    viewHolder = new ClaimViewHolder(inflater.inflate(R.layout.list_item_claim_voting, parent, false), mTeamId, mCurrency);
                    break;
                case VIEW_TYPE_VOTED:
                    viewHolder = new ClaimViewHolder(inflater.inflate(R.layout.list_item_claim_voted, parent, false), mTeamId, mCurrency);
                    break;
                case VIEW_TYPE_IN_PAYMENT:
                case VIEW_TYPE_PROCESSED:
                    viewHolder = new ClaimViewHolder(inflater.inflate(R.layout.list_item_claim_being_paid, parent, false), mTeamId, mCurrency);
                    break;

            }
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        if (holder instanceof SubmitClaimViewHolder) {
            ((SubmitClaimViewHolder) holder).onBind();
        } else if (holder instanceof ClaimViewHolder) {
            position -= getHeadersCount();
            ((ClaimViewHolder) holder).onBind(new JsonWrapper(mPager.getLoadedData().get(position).getAsJsonObject()));
        }
    }

    @Override
    protected int getHeadersCount() {
        return mSubmitClaim ? 1 : 0;
    }


    @Override
    protected RecyclerView.ViewHolder createEmptyViewHolder(ViewGroup parent) {
        return new NoClaimsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_no_claims, parent, false));
    }

    private class SubmitClaimViewHolder extends RecyclerView.ViewHolder {

        private ImageView mObjectIconView;
        private TextView mObjectNameView;
        private TextView mLocationView;
        private View mSubmitClaimView;
        private final RequestOptions imageOptions;

        SubmitClaimViewHolder(View itemView) {
            super(itemView);
            mObjectIconView = itemView.findViewById(R.id.object_icon);
            mObjectNameView = itemView.findViewById(R.id.title);
            mLocationView = itemView.findViewById(R.id.subtitle);
            mSubmitClaimView = itemView.findViewById(R.id.submit_claim);
            imageOptions = new RequestOptions().transforms(new CenterCrop()
                    , new RoundedCorners(itemView.getResources().getDimensionPixelOffset(R.dimen.rounded_corners_4dp)));
        }

        public void onBind() {

            Context context = itemView.getContext();
            mObjectNameView.setText(mObjectName);

            if (mObjectImageUri != null) {
                GlideApp.with(itemView.getContext()).load(getImageLoader().getImageUrl(mObjectImageUri))
                        .apply(imageOptions)
                        .into(mObjectIconView);
            }

            mLocationView.setText(mLocation);

            mSubmitClaimView.setOnClickListener(v -> startActivity(ReportClaimActivity.
                    getLaunchIntent(context, mObjectImageUri, mObjectName, mTeamId, mCurrency, mLocation)));
        }
    }

    private class NoClaimsViewHolder extends SubmitClaimViewHolder {
        private View submitPanelView;

        NoClaimsViewHolder(View itemView) {
            super(itemView);
            submitPanelView = itemView.findViewById(R.id.submit_claim_panel);
            ((TextView) itemView.findViewById(R.id.prompt)).setText(Html.fromHtml(itemView.getContext().getString(R.string.no_claims)));
        }

        @Override
        public void onBind() {
            super.onBind();
            if (submitPanelView != null) {
                submitPanelView.setVisibility(mSubmitClaim ? View.VISIBLE : View.GONE);
            }
        }
    }
}
