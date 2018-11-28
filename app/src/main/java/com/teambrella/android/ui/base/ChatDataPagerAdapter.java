package com.teambrella.android.ui.base;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.teambrella.android.R;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.json.JsonWrapper;
import com.teambrella.android.data.base.IDataPager;

import io.reactivex.Notification;

/**
 * Teambrella Data Pager Adapter
 */
public class ChatDataPagerAdapter extends ATeambrellaDataPagerAdapter {

  @SuppressWarnings("WeakerAccess")
  protected static final int VIEW_TYPE_LOADING = 1;
  @SuppressWarnings("WeakerAccess")
  protected static final int VIEW_TYPE_ERROR = 2;
  @SuppressWarnings("WeakerAccess")
  protected static final int VIEW_TYPE_REGULAR = 3;


  public ChatDataPagerAdapter(IDataPager<JsonArray> pager) {
    super(pager);
  }

  @Override
  protected void onPagerUpdated(Notification<JsonObject> notification) {
    if (notification.isOnNext()) {
      JsonWrapper metadata = new JsonWrapper(notification.getValue()).getObject(TeambrellaModel.ATTR_METADATA_);
      if (metadata != null) {
        if (metadata.getBoolean(TeambrellaModel.ATTR_METADATA_ITEM_DELETED, false)) {
          notifyDataSetChanged();
        }
        else if (metadata.getBoolean(TeambrellaModel.ATTR_METADATA_ITEMS_UPDATED, false)
                || metadata.getBoolean(TeambrellaModel.ATTR_METADATA_RELOAD, false)
                || metadata.getBoolean(TeambrellaModel.ATTR_METADATA_FORCE, false)
                && TeambrellaModel.ATTR_METADATA_PREVIOUS_DIRECTION.equals(metadata.getString(TeambrellaModel.ATTR_METADATA_DIRECTION))) {
          notifyDataSetChanged();
        } else {
          int dataSize = mPager.getLoadedData().size();
          int addedSize = metadata.getInt(TeambrellaModel.ATTR_METADATA_SIZE);
          int shift = hasHeader() ? 1 : 0;

          if (addedSize > 0) {
            switch (metadata.getString(TeambrellaModel.ATTR_METADATA_DIRECTION)) {
              case TeambrellaModel.ATTR_METADATA_NEXT_DIRECTION:
                notifyItemRangeInserted(dataSize - addedSize + shift, (mPager.getHasNext() ? 0 : -1) + addedSize);
                break;

              case TeambrellaModel.ATTR_METADATA_PREVIOUS_DIRECTION:
                notifyItemRangeInserted(0, addedSize - 1);
                // notifyItemChanged(addedSize); // see https://stackoverflow.com/questions/30220771/recyclerview-inconsistency-detected-invalid-item-position
                break;
            }
          }
          notifyDataSetChanged(); // see https://stackoverflow.com/questions/30220771/recyclerview-inconsistency-detected-invalid-item-position
        }
      }
    } else {
      notifyDataSetChanged();
    }
  }

  @Override
  public int getItemViewType(int position) {

    int size = mPager.getLoadedData().size();

    if (position == 0) {
      if (mPager.getHasPreviousError()) {
        return VIEW_TYPE_ERROR;
      } else if (mPager.getHasPrevious() || mPager.isPreviousLoading()) {
        return VIEW_TYPE_LOADING;
      }

    } else if ((position == size) && !mPager.getHasPrevious() && !mPager.isPreviousLoading() && !mPager.getHasPreviousError()
            || ((mPager.getHasPreviousError() || mPager.isPreviousLoading() || mPager.getHasPrevious()) && position == size + 1)) {
      if (mPager.getHasNextError()) {
        return VIEW_TYPE_ERROR;
      } else if (mPager.getHasNext() || mPager.isNextLoading()) {
        return VIEW_TYPE_LOADING;
      }
    }

    return VIEW_TYPE_REGULAR;
  }


  protected RecyclerView.ViewHolder createErrorViewHolder(ViewGroup parent) {
    Context context = parent.getContext();
    return new ErrorViewHolder(LayoutInflater.from(context).inflate(R.layout.list_item_reload, parent, false));
  }

  protected RecyclerView.ViewHolder createLoadingViewHolder(ViewGroup parent) {
    Context context = parent.getContext();
    return new LoadingViewHolder(LayoutInflater.from(context).inflate(R.layout.list_item_loading, parent, false));
  }


  public void exchangeItems(RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
    JsonArray data = mPager.getLoadedData();
    int srcPosition = viewHolder.getAdapterPosition();
    int dstPosition = target.getAdapterPosition();
    JsonElement srcElement = data.get(srcPosition);
    JsonElement dstElement = data.get(dstPosition);
    data.set(srcPosition, dstElement);
    data.set(dstPosition, srcElement);
    notifyItemMoved(srcPosition, dstPosition);
  }

  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    switch (viewType) {
      case VIEW_TYPE_LOADING:
        return createLoadingViewHolder(parent);
      case VIEW_TYPE_ERROR:
        return createErrorViewHolder(parent);
    }
    return null;
  }

  @Override
  public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

    if (mPager.getHasNext() && !mPager.isNextLoading() && !mPager.getHasNextError() && position > mPager.getLoadedData().size() - 10) {
      mPager.loadNext(false);
    }

    if (mPager.getHasPrevious() && !mPager.isNextLoading() && !mPager.getHasNextError() && position < 10) {
      mPager.loadPrevious(false);
    }


    if (holder instanceof ErrorViewHolder) {
      holder.itemView.setOnClickListener(v -> {
        mPager.loadNext(false);
        notifyDataSetChanged();
      });
    }
  }

  @Override
  public int getItemCount() {
    return mPager.getLoadedData().size() + (hasFooter() ? 1 : 0) + (hasHeader() ? 1 : 0);

  }

  protected boolean hasHeader() {
    return mPager.getHasPreviousError() || mPager.isPreviousLoading() || mPager.getHasPrevious();
  }

  protected boolean hasFooter() {
    return mPager.getHasNextError() || mPager.isNextLoading() || mPager.getHasNext();
  }

  private static class LoadingViewHolder extends RecyclerView.ViewHolder {
    LoadingViewHolder(View itemView) {
      super(itemView);
    }
  }

  private static class ErrorViewHolder extends RecyclerView.ViewHolder {
    ErrorViewHolder(View itemView) {
      super(itemView);
    }
  }
}
