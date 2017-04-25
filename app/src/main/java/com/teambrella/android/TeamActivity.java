package com.teambrella.android;

import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;

import com.google.gson.JsonObject;
import com.teambrella.android.api.TeambrellaException;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.server.TeambrellaUris;
import com.teambrella.android.data.loaders.TeambrellaUriLoader;
import com.teambrella.android.data.teammates.TeammatesRecyclerAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TeamActivity extends AppCompatActivity {

    @BindView(R.id.list)
    RecyclerView mListView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team);
        ButterKnife.bind(this);
        mListView.setLayoutManager(new GridLayoutManager(this, 2));
        getLoaderManager().initLoader(0, null, new LoaderManager.LoaderCallbacks<Pair<JsonObject, TeambrellaException>>() {
            @Override
            public Loader<Pair<JsonObject, TeambrellaException>> onCreateLoader(int id, Bundle args) {
                return new TeambrellaUriLoader(TeamActivity.this, TeambrellaUris.getTeamUri(2006));
            }

            @Override
            public void onLoadFinished(Loader<Pair<JsonObject, TeambrellaException>> loader, Pair<JsonObject, TeambrellaException> data) {
                mListView.setAdapter(new TeammatesRecyclerAdapter(data.first.get(TeambrellaModel.ATTR_DATA)
                        .getAsJsonObject().get(TeambrellaModel.ATTR_DATA_TEAMMATES).getAsJsonArray()));
            }

            @Override
            public void onLoaderReset(Loader<Pair<JsonObject, TeambrellaException>> loader) {

            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}
