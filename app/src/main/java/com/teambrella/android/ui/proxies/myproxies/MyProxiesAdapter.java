package com.teambrella.android.ui.proxies.myproxies;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.teambrella.android.R;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.json.JsonWrapper;
import com.teambrella.android.data.base.IDataPager;
import com.teambrella.android.image.TeambrellaImageLoader;
import com.teambrella.android.ui.base.TeambrellaDataPagerAdapter;

import io.reactivex.Observable;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;

/**
 * My Proxies Adapter
 */

public class MyProxiesAdapter extends TeambrellaDataPagerAdapter {


    MyProxiesAdapter(IDataPager<JsonArray> pager) {
        super(pager);
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder = super.onCreateViewHolder(parent, viewType);

        if (holder == null) {
            holder = new MyProxyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_my_proxy, parent, false));
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        if (holder instanceof MyProxyViewHolder) {
            ((MyProxyViewHolder) holder).onBind(new JsonWrapper(mPager.getLoadedData().get(position).getAsJsonObject()));
        }
    }

    private static final class MyProxyViewHolder extends RecyclerView.ViewHolder {

        private ImageView mIcon;
        private TextView mTitle;

        MyProxyViewHolder(View itemView) {
            super(itemView);
            mIcon = (ImageView) itemView.findViewById(R.id.icon);
            mTitle = (TextView) itemView.findViewById(R.id.title);
        }

        void onBind(JsonWrapper item) {
            Observable.fromArray(item).map(json -> TeambrellaImageLoader.getImageUri(json.getString(TeambrellaModel.ATTR_DATA_AVATAR)))
                    .map(uri -> TeambrellaImageLoader.getInstance(itemView.getContext()).getPicasso().load(uri))
                    .subscribe(requestCreator -> requestCreator.transform(new CropCircleTransformation()).into(mIcon), throwable -> {
                        // 8)
                    });

            mTitle.setText(item.getString(TeambrellaModel.ATTR_DATA_NAME));

        }


    }

}
