package com.teambrella.android.data.teammates;

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

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Teammates Recycler Adapter
 */
public class TeammatesRecyclerAdapter extends RecyclerView.Adapter<TeammatesRecyclerAdapter.TeammatesViewHolder> {


    private final JsonArray mArray;

    /**
     * Constructor.
     *
     * @param array
     */
    public TeammatesRecyclerAdapter(JsonArray array) {
        mArray = array;
    }

    @Override
    public TeammatesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new TeammatesViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.teammate_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(TeammatesViewHolder holder, int position) {
        JsonObject item = mArray.get(position).getAsJsonObject();
        Picasso.with(holder.itemView.getContext()).load(TeambrellaServer.AUTHORITY + item.get(TeambrellaModel.ATTR_DATA_AVATAR).getAsString())
                .into(holder.mIcon);
        holder.mTitle.setText(item.get(TeambrellaModel.ATTR_DATA_NAME).getAsString());
    }



    @Override
    public int getItemCount() {
        return mArray.size();
    }

    static class TeammatesViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.icon)
        ImageView mIcon;
        @BindView(R.id.title)
        TextView mTitle;

        public TeammatesViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
