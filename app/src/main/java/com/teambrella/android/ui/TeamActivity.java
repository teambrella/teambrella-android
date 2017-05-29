package com.teambrella.android.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.teambrella.android.R;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.server.TeambrellaServer;
import com.teambrella.android.api.server.TeambrellaUris;
import com.teambrella.android.ui.team.TeammatesRecyclerAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class TeamActivity extends AppCompatActivity {

    @BindView(R.id.list)
    RecyclerView mListView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team);
        ButterKnife.bind(this);
        TeambrellaServer server = new TeambrellaServer(this, TeambrellaUser.get(this).getPrivateKey());
        server.requestObservable(TeambrellaUris.getTeamUri(2006, 0, 0), null)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onResult, this::onError, this::onComplete);

        mListView.setLayoutManager(new GridLayoutManager(this, 1));
    }


    private void onResult(JsonObject response) {
        mListView.setAdapter(new TeammatesRecyclerAdapter(response.get(TeambrellaModel.ATTR_DATA)
                .getAsJsonObject().get(TeambrellaModel.ATTR_DATA_TEAMMATES).getAsJsonArray()));
    }

    private void onError(Throwable e) {
        Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
    }

    private void onComplete() {

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}
