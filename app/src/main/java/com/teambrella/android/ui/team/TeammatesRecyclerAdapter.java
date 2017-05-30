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
import com.teambrella.android.ui.teammate.TeammateActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.Disposable;

/**
 * Teammates Recycler Adapter
 */
public class TeammatesRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_LOADING = 1;
    private static final int VIEW_TYPE_ERROR = 2;
    private static final int VIEW_TYPE_REGULAR = 3;
    private IDataPager<JsonArray> mPager;
    private Disposable mDisposal;

    /**
     * Constructor.
     */
    public TeammatesRecyclerAdapter(IDataPager<JsonArray> pager) {
        mPager = pager;
        mDisposal = mPager.getObservable().subscribe(d -> notifyDataSetChanged());
    }


    @Override
    public int getItemViewType(int position) {
        int size = mPager.getLoadedData().size();
        if (position == size) {
            if (mPager.hasError()) {
                return VIEW_TYPE_ERROR;
            } else if (mPager.hasNext() || mPager.isLoading()) {
                return VIEW_TYPE_LOADING;
            } else {
                throw new RuntimeException();
            }
        }

        return VIEW_TYPE_REGULAR;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case VIEW_TYPE_LOADING:
                return new LoadingViewHolder(inflater.inflate(R.layout.list_item_loading, parent, false));
            case VIEW_TYPE_ERROR:
                return new LoadingViewHolder(inflater.inflate(R.layout.list_item_reload, parent, false));
        }
        return new TeammatesViewHolder(inflater.inflate(R.layout.teammate_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {

        if (mPager.hasNext() && !mPager.isLoading() && position == mPager.getLoadedData().size()) {
            mPager.loadNext();
        }

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
            holder.itemView.setOnClickListener(v -> {
                context.startActivity(TeammateActivity.getIntent(context, TeambrellaUris.getTeammateUri(2,
                        item.get(TeambrellaModel.ATTR_DATA_USER_ID).getAsString()), item.get(TeambrellaModel.ATTR_DATA_NAME).getAsString(), userPictureUri));
            });
        }
    }

    @Override
    public int getItemCount() {
        return mPager.getLoadedData().size() + (mPager.hasError() || mPager.isLoading() || mPager.hasNext() ? 1 : 0);
    }

    /**
     * Destroy.
     */
    public void destroy() {
        if (mDisposal != null && !mDisposal.isDisposed()) {
            mDisposal.dispose();
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

    static class LoadingViewHolder extends RecyclerView.ViewHolder {
        public LoadingViewHolder(View itemView) {
            super(itemView);
        }
    }

    static class ErrorViewHolder extends RecyclerView.ViewHolder {
        public ErrorViewHolder(View itemView) {
            super(itemView);
        }
    }
}
