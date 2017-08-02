package com.teambrella.android.ui.photos;

import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.teambrella.android.R;
import com.teambrella.android.image.TeambrellaImageLoader;

import java.io.File;
import java.util.ArrayList;

import jp.wasabeef.picasso.transformations.MaskTransformation;

/**
 * Photo Adapter
 */
public class PhotoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private ArrayList<String> mPhotos = new ArrayList<>();
    private Picasso mPicasso;
    private LayoutInflater mLayoutInflater;

    public PhotoAdapter(Context context) {
        mPicasso = TeambrellaImageLoader.getInstance(context).getPicasso();
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ImageViewHolder(mLayoutInflater.inflate(R.layout.list_item_image, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((ImageViewHolder) holder).onBind(mPhotos.get(position));
    }

    @Override
    public int getItemCount() {
        return mPhotos.size();
    }

    public void addPhoto(String path) {
        mPhotos.add(path);
        notifyItemInserted(mPhotos.size() - 1);
    }

    private void removePhoto(int position) {
        mPhotos.remove(position);
        notifyItemRemoved(position);
    }


    private class ImageViewHolder extends RecyclerView.ViewHolder {

        private ImageView mIcon;
        private ImageView mClose;

        ImageViewHolder(View itemView) {
            super(itemView);
            mIcon = itemView.findViewById(R.id.image);
            mClose = itemView.findViewById(R.id.close);
        }

        void onBind(String path) {
            Context context = itemView.getContext();
            Resources resources = context.getResources();
            mPicasso.load(Uri.fromFile(new File(path)))
                    .resize(resources.getDimensionPixelSize(R.dimen.image_size_48), resources.getDimensionPixelSize(R.dimen.image_size_48))
                    .centerCrop()
                    .transform(new MaskTransformation(context, R.drawable.teammate_object_mask))
                    .into(mIcon);

            mClose.setOnClickListener(v -> removePhoto(getAdapterPosition()));
        }
    }
}
