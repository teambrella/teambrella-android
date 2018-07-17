package com.teambrella.android.ui;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.teambrella.android.R;
import com.teambrella.android.api.model.json.JsonWrapper;
import com.teambrella.android.data.base.IDataPager;
import com.teambrella.android.ui.base.TeambrellaDaggerActivity;
import com.teambrella.android.ui.base.TeambrellaDataPagerAdapter;
import com.teambrella.android.ui.widget.AkkuratBoldTypefaceSpan;
import com.teambrella.android.util.log.Log;

import io.reactivex.Notification;
import io.reactivex.Observable;

/**
 * Cosigners
 */
public class CosignersActivity extends TeambrellaDaggerActivity {

    private static final String EXTRA_COSIGNERS = "cosigners";
    private static final String EXTRA_TEAM_ID = "team_id";

    public static void start(Context context, String cosigners, int teamId) {
        context.startActivity(new Intent(context, CosignersActivity.class)
                .putExtra(EXTRA_COSIGNERS, cosigners).putExtra(EXTRA_TEAM_ID, teamId));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cosigners);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_vector);
        }

        setTitle(R.string.cosigners);

        RecyclerView list = findViewById(R.id.list);
        list.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false) {
            @Override
            public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State
                    state) {
                try {
                    super.onLayoutChildren(recycler, state);
                } catch (Throwable e) {
                    Log.e("TEST", e.toString());
                }
            }
        });

        CosignersAdapter adapter = new CosignersAdapter(new CosignersDataPager(new Gson().fromJson(getIntent().getStringExtra(EXTRA_COSIGNERS), JsonArray.class))
                , getIntent().getIntExtra(EXTRA_TEAM_ID, 0));
        getComponent().inject(adapter);
        list.setAdapter(adapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this,
                LinearLayoutManager.VERTICAL) {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                int position = parent.getChildAdapterPosition(view);
                boolean drawDivider = true;
                switch (parent.getAdapter().getItemViewType(position)) {
                    case CosignersAdapter.VIEW_TYPE_BOTTOM:
                    case CosignersAdapter.VIEW_TYPE_ERROR:
                    case CosignersAdapter.VIEW_TYPE_LOADING:
                        drawDivider = false;
                }

                if (position + 1 < parent.getAdapter().getItemCount()) {
                    switch (parent.getAdapter().getItemViewType(position + 1)) {
                        case CosignersAdapter.VIEW_TYPE_BOTTOM:
                        case CosignersAdapter.VIEW_TYPE_ERROR:
                        case CosignersAdapter.VIEW_TYPE_LOADING:
                            drawDivider = false;
                    }
                }

                if (position != parent.getAdapter().getItemCount() - 1
                        && drawDivider) {
                    super.getItemOffsets(outRect, view, parent, state);
                } else {
                    outRect.set(0, 0, 0, 0);
                }
            }
        };
        dividerItemDecoration.setDrawable(getResources().getDrawable(R.drawable.divder));
        list.addItemDecoration(dividerItemDecoration);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setTitle(CharSequence title) {
        SpannableString s = new SpannableString(title);
        s.setSpan(new AkkuratBoldTypefaceSpan(this), 0, s.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        super.setTitle(s);
    }


    private static class CosignersAdapter extends TeambrellaDataPagerAdapter {

        private final int mTeamId;

        CosignersAdapter(IDataPager<JsonArray> pager, int teamId) {
            super(pager);
            mTeamId = teamId;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_REGULAR) {
                return new CosignerViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_cosigner, parent, false), mTeamId);
            }

            return super.onCreateViewHolder(parent, viewType);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            super.onBindViewHolder(holder, position);
            if (holder instanceof CosignerViewHolder) {
                ((CosignerViewHolder) holder).onBind(new JsonWrapper(mPager.getLoadedData().get(position).getAsJsonObject()));
            }
        }


        private class CosignerViewHolder extends AMemberViewHolder {
            CosignerViewHolder(View itemView, int teamId) {
                super(itemView, teamId);
            }

            @Override
            public void onBind(JsonWrapper item) {
                super.onBind(item);
            }
        }
    }


    private static class CosignersDataPager implements IDataPager<JsonArray> {


        private final JsonArray mData;

        CosignersDataPager(JsonArray data) {
            mData = data;
        }

        @Override
        public JsonArray getLoadedData() {
            return mData;
        }

        @NonNull
        @Override
        public LiveData<Notification<JsonObject>> getDataObservable() {
            return new MutableLiveData<>();
        }

        @Override
        public Observable<Integer> getItemChangeObservable() {
            return null;
        }

        @Override
        public void loadNext(boolean force) {

        }

        @Override
        public boolean getHasNext() {
            return false;
        }

        @Override
        public boolean getHasPrevious() {
            return false;
        }

        @Override
        public void loadPrevious(boolean force) {

        }

        @Override
        public boolean getHasNextError() {
            return false;
        }

        @Override
        public boolean isNextLoading() {
            return false;
        }

        @Override
        public boolean getHasPreviousError() {
            return false;
        }

        @Override
        public boolean isPreviousLoading() {
            return false;
        }

        @Override
        public void reload() {

        }

        @Override
        public void reload(@NonNull Uri uri) {

        }
    }
}
