package com.teambrella.android.ui.team.claims;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.squareup.picasso.Picasso;
import com.teambrella.android.R;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.json.JsonWrapper;
import com.teambrella.android.data.base.IDataPager;
import com.teambrella.android.image.TeambrellaImageLoader;
import com.teambrella.android.ui.base.TeambrellaDataPagerAdapter;
import com.teambrella.android.ui.claim.ClaimActivity;
import com.teambrella.android.ui.claim.ReportClaimActivity;
import com.teambrella.android.util.AmountCurrencyUtil;

import jp.wasabeef.picasso.transformations.MaskTransformation;

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
    ClaimsAdapter(IDataPager<JsonArray> pager, int teamId, String currency, boolean submitClaim) {
        super(pager);
        mTeamId = teamId;
        mSubmitClaim = submitClaim;
        mCurrency = currency;
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
                        if (position == 0 || getItemViewType(position - 1) != VIEW_TYPE_VOTING) {
                            viewType = VIEW_TYPE_IN_PAYMENT_HEADER;
                        } else {
                            viewType = VIEW_TYPE_IN_PAYMENT_HEADER_TOP;
                        }
                        break;
                    case TeambrellaModel.ClaimsListItemType.ITEM_IN_PAYMENT:
                        viewType = VIEW_TYPE_IN_PAYMENT;
                        break;
                    case TeambrellaModel.ClaimsListItemType.ITEM_PROCESSED_HEADER:
                        if (position == 0 || getItemViewType(position - 1) != VIEW_TYPE_VOTING) {
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
                    viewHolder = new ClaimViewHolder(inflater.inflate(R.layout.list_item_claim_voting, parent, false));
                    break;
                case VIEW_TYPE_VOTED:
                    viewHolder = new ClaimViewHolder(inflater.inflate(R.layout.list_item_claim_voted, parent, false));
                    break;
                case VIEW_TYPE_IN_PAYMENT:
                case VIEW_TYPE_PROCESSED:
                    viewHolder = new ClaimViewHolder(inflater.inflate(R.layout.list_iten_claim_being_paid, parent, false));
                    break;

            }
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        if (mSubmitClaim && position == 0) {
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

    private class ClaimViewHolder extends RecyclerView.ViewHolder {
        ImageView mIcon;
        TextView mObject;
        TextView mClaimAmount;
        TextView mTeammateName;
        ImageView mTeammateIcon;
        TextView mVote;
        ImageView mProxyAvatar;
        TextView mProxyName;
        ProgressBar mPaymentProgress;
        View mViewToVote;

        ClaimViewHolder(View itemView) {
            super(itemView);
            mIcon = itemView.findViewById(R.id.icon);
            mObject = itemView.findViewById(R.id.object);
            mClaimAmount = itemView.findViewById(R.id.value);
            mTeammateName = itemView.findViewById(R.id.teammate);
            mTeammateIcon = itemView.findViewById(R.id.teammate_picture);
            mVote = itemView.findViewById(R.id.vote);
            mProxyAvatar = itemView.findViewById(R.id.proxy_picture);
            mProxyName = itemView.findViewById(R.id.proxy);
            mPaymentProgress = itemView.findViewById(R.id.payment_progress);
            mViewToVote = itemView.findViewById(R.id.view_to_vote);
        }


        void onBind(JsonWrapper item) {
            final Context context = itemView.getContext();
            final Uri objectPictureUri = TeambrellaImageLoader.getImageUri(item.getString(TeambrellaModel.ATTR_DATA_SMALL_PHOTO));
            final Uri teammatePictureUri = TeambrellaImageLoader.getImageUri(item.getString(TeambrellaModel.ATTR_DATA_AVATAR));
            final Uri proxyAvatarUri = TeambrellaImageLoader.getImageUri(item.getString(TeambrellaModel.ATTR_DATA_PROXY_AVATAR));
            Picasso picasso = TeambrellaImageLoader.getInstance(itemView.getContext()).getPicasso();

            if (mIcon != null) {
                picasso.load(objectPictureUri).
                        resizeDimen(R.dimen.image_size_40, R.dimen.image_size_40)
                        .centerCrop()
                        .transform(new MaskTransformation(itemView.getContext(), R.drawable.teammate_object_mask))
                        .into(mIcon);
            }

            if (teammatePictureUri != null) {
                picasso.load(teammatePictureUri).into(mTeammateIcon);
            }

            if (mProxyAvatar != null) {
                if (proxyAvatarUri != null) {
                    picasso.load(proxyAvatarUri).into(mProxyAvatar);
                } else {
                    mProxyAvatar.setImageBitmap(null);
                }
            }

            if (mProxyName != null) {
                String proxyName = item.getString(TeambrellaModel.ATTR_DATA_PROXY_NAME);
                if (proxyName != null) {
                    proxyName = proxyName.trim().split(" ")[0];
                }
                mProxyName.setText(proxyName);
            }

            mObject.setText(item.getString(TeambrellaModel.ATTR_DATA_MODEL));
            mTeammateName.setText(item.getString(TeambrellaModel.ATTR_DATA_NAME));
            mClaimAmount.setText(context.getString(R.string.amount_format_string, AmountCurrencyUtil.getCurrencySign(mCurrency), Math.round(item.getDouble(TeambrellaModel.ATTR_DATA_CLAIM_AMOUNT))));


            if (mVote != null) {
                mVote.setText(itemView.getContext().getString(R.string.claim_vote_format_string, Math.round(item.getFloat(TeambrellaModel.ATTR_DATA_MY_VOTE) * 100)));
            }

            if (mPaymentProgress != null) {
                float voting = item.getFloat(TeambrellaModel.ATTR_DATA_VOTING_RES_CRYPTO);
                float payment = item.getFloat(TeambrellaModel.ATTR_DATA_PAYMENT_RES_CRYPTO);
                mPaymentProgress.setProgress(Math.round((payment * 100) / voting));
            }


            if (mViewToVote != null) {
                mViewToVote.setOnClickListener(v -> context.startActivity(
                        ClaimActivity.getLaunchIntent(context, item.getInt(TeambrellaModel.ATTR_DATA_ID),
                                item.getString(TeambrellaModel.ATTR_DATA_MODEL), mTeamId)));
            } else {
                itemView.setOnClickListener(v -> context.startActivity(
                        ClaimActivity.getLaunchIntent(context, item.getInt(TeambrellaModel.ATTR_DATA_ID),
                                item.getString(TeambrellaModel.ATTR_DATA_MODEL), mTeamId)));
            }
        }
    }

    private class SubmitClaimViewHolder extends RecyclerView.ViewHolder {

        private ImageView mObjectIconView;
        private TextView mObjectNameView;
        private TextView mLocationView;
        private View mSubmitClaimView;


        SubmitClaimViewHolder(View itemView) {
            super(itemView);
            mObjectIconView = itemView.findViewById(R.id.object_icon);
            mObjectNameView = itemView.findViewById(R.id.title);
            mLocationView = itemView.findViewById(R.id.subtitle);
            mSubmitClaimView = itemView.findViewById(R.id.submit_claim);
        }

        public void onBind() {

            Context context = itemView.getContext();
            Picasso picasso = TeambrellaImageLoader.getInstance(context).getPicasso();
            mObjectNameView.setText(mObjectName);

            if (mObjectImageUri != null) {
                picasso.load(mObjectImageUri).resizeDimen(R.dimen.image_size_96, R.dimen.image_size_96)
                        .centerCrop().
                        transform(new MaskTransformation(context, R.drawable.teammate_object_mask)).
                        into(mObjectIconView);
            }

            mLocationView.setText(mLocation);

            mSubmitClaimView.setOnClickListener(v -> ReportClaimActivity.
                    start(context, mObjectImageUri, mObjectName, mTeamId, mCurrency));
        }
    }
}
