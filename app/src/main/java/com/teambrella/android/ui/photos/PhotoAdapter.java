package com.teambrella.android.ui.photos;

import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import com.teambrella.android.R;
import com.teambrella.android.ui.base.ATeambrellaAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import io.reactivex.Observable;
import jp.wasabeef.picasso.transformations.MaskTransformation;

/**
 * Photo Adapter
 */
public class PhotoAdapter extends ATeambrellaAdapter {


    private static class Photo {
        String filePath;
        String uri;
        boolean isError;

        Photo(String filePath) {
            this.filePath = filePath;
        }
    }


    private ArrayList<Photo> mPhotos = new ArrayList<>();
    private LayoutInflater mLayoutInflater;

    public PhotoAdapter(Context context) {
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
        mPhotos.add(new Photo(path));
        notifyItemInserted(mPhotos.size() - 1);
    }

    public void updatePhoto(String filePath, String uri) {
        Observable.range(0, mPhotos.size()).filter(index -> mPhotos.get(index).filePath.equals(filePath))
                .doOnNext(index -> mPhotos.get(index).uri = uri)
                .doOnNext(this::notifyItemChanged).blockingFirst(0);
    }


    public void exchangeItems(RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        Collections.swap(mPhotos, viewHolder.getAdapterPosition(), target.getAdapterPosition());
        notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
    }

    public void removePhoto(String filePath) {
        Observable.range(0, mPhotos.size()).filter(index -> mPhotos.get(index).filePath.equals(filePath))
                .doOnNext(index -> mPhotos.remove(index.intValue()))
                .doOnNext(this::notifyItemRemoved).blockingFirst(0);
    }

    private void removePhoto(int position) {
        mPhotos.remove(position);
        notifyItemRemoved(position);
    }


    public String getImages() {
        return Observable.fromIterable(mPhotos)
                .map(photo -> photo.uri)
                .reduce(new JsonArray(), (jsonElements, s) -> {
                    jsonElements.add(new JsonPrimitive(s));
                    return jsonElements;
                }).blockingGet().toString();
    }


    private class ImageViewHolder extends RecyclerView.ViewHolder {

        private ImageView mIcon;
        private ImageView mClose;
        private ProgressBar mProgressBar;

        ImageViewHolder(View itemView) {
            super(itemView);
            mIcon = itemView.findViewById(R.id.image);
            mClose = itemView.findViewById(R.id.close);
            mProgressBar = itemView.findViewById(R.id.upload_progress);
        }

        void onBind(Photo photo) {
            Context context = itemView.getContext();
            Resources resources = context.getResources();
            getPicasso().load(Uri.fromFile(new File(photo.filePath)))
                    .resize(resources.getDimensionPixelSize(R.dimen.image_size_48), resources.getDimensionPixelSize(R.dimen.image_size_48))
                    .centerCrop()
                    .transform(new MaskTransformation(context, R.drawable.teammate_object_mask))
                    .into(mIcon);


            mProgressBar.setVisibility(photo.uri != null ? View.GONE : View.VISIBLE);

            mClose.setOnClickListener(v -> removePhoto(getAdapterPosition()));
        }
    }
}
