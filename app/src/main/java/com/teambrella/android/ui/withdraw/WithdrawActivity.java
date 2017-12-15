package com.teambrella.android.ui.withdraw;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import com.google.gson.JsonObject;
import com.teambrella.android.R;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.json.JsonWrapper;
import com.teambrella.android.api.server.TeambrellaUris;
import com.teambrella.android.data.base.TeambrellaDataFragment;
import com.teambrella.android.data.base.TeambrellaDataPagerFragment;
import com.teambrella.android.ui.base.ADataHostActivity;
import com.teambrella.android.ui.base.ADataPagerProgressFragment;
import com.teambrella.android.util.ConnectivityUtils;

import io.reactivex.Notification;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

/**
 * Withdraw Activity
 */
public class WithdrawActivity extends ADataHostActivity implements IWithdrawActivity {

    public static final String WITHDRAWALS_DATA_TAG = "withdrawals_data";
    public static final String WITHDRAWALS_UI_TAG = "withdrawals_ui";
    public static final String WITHDRAWALS_INFO_DIALOG_TAG = "info_dialog";

    private static final String EXTRA_TEAM_ID = "extra_team_id";

    private int mTeamId;
    private float mAvailableValue;
    private float mReservedValue;
    private Disposable mWithdrawalsDisposable;
    private Snackbar mSnackBar;

    public static void start(Context context, int teamId) {
        context.startActivity(new Intent(context, WithdrawActivity.class)
                .putExtra(EXTRA_TEAM_ID, teamId));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        final Intent intent = getIntent();
        mTeamId = intent != null ? intent.getIntExtra(EXTRA_TEAM_ID, -1) : -1;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one_fragment);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (fragmentManager.findFragmentByTag(WITHDRAWALS_UI_TAG) == null) {
            transaction.add(R.id.container
                    , ADataPagerProgressFragment.getInstance(WITHDRAWALS_DATA_TAG, WithdrawalsFragment.class)
                    , WITHDRAWALS_UI_TAG);
        }

        if (!transaction.isEmpty()) {
            transaction.commit();
        }

        setTitle(R.string.withdraw);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(VectorDrawableCompat.create(getResources(), R.drawable.ic_arrow_back, null));
        }
    }

    @Override
    protected String[] getDataTags() {
        return new String[]{};
    }

    @Override
    protected String[] getPagerTags() {
        return new String[]{WITHDRAWALS_DATA_TAG};
    }

    @Override
    protected TeambrellaDataFragment getDataFragment(String tag) {
        return null;
    }

    @Override
    protected TeambrellaDataPagerFragment getDataPagerFragment(String tag) {
        switch (tag) {
            case WITHDRAWALS_DATA_TAG:
                return TeambrellaDataPagerFragment.getInstance(TeambrellaUris.getWithdrawals(mTeamId),
                        TeambrellaModel.ATTR_DATA_TXS, WithdrawalsDataPagerFragment.class);
        }

        return null;
    }


    @Override
    protected void onStart() {
        super.onStart();
        mWithdrawalsDisposable = getPager(WITHDRAWALS_DATA_TAG).getObservable().subscribe(this::onDataUpdated);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mWithdrawalsDisposable != null && !mWithdrawalsDisposable.isDisposed()) {
            mWithdrawalsDisposable.dispose();
            mWithdrawalsDisposable = null;
        }
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

    public void showInfoDialog() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.findFragmentByTag(WITHDRAWALS_INFO_DIALOG_TAG) == null) {
            WithdrawInfoDialogFragment.getInstance(Math.round(mAvailableValue * 1000), Math.round(mReservedValue * 1000))
                    .show(fragmentManager, WITHDRAWALS_INFO_DIALOG_TAG);
        }
    }

    @Override
    public void showWithdrawInfo() {
        showInfoDialog();
    }

    @Override
    public void requestWithdraw(String address, float amount) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        TeambrellaDataPagerFragment dataFragment = (TeambrellaDataPagerFragment) fragmentManager.findFragmentByTag(WITHDRAWALS_DATA_TAG);
        if (dataFragment != null) {
            dataFragment.getPager().reload(TeambrellaUris.getNewWithdrawUri(mTeamId, amount, address));
        }

        ADataPagerProgressFragment fragment = (ADataPagerProgressFragment) fragmentManager.findFragmentByTag(WITHDRAWALS_UI_TAG);
        if (fragment != null) {
            fragment.setRefreshing(true);
        }

    }

    private void onDataUpdated(Notification<JsonObject> notification) {
        if (notification.isOnNext()) {
            Observable.just(notification.getValue())
                    .map(JsonWrapper::new)
                    .map(jsonWrapper -> jsonWrapper.getObject(TeambrellaModel.ATTR_DATA))
                    .doOnNext(jsonWrapper -> {
                        mReservedValue = jsonWrapper.getFloat(TeambrellaModel.ATTR_DATA_CRYPTO_RESERVED);
                        mAvailableValue = jsonWrapper.getFloat(TeambrellaModel.ATTR_DATA_CRYPTO_BALANCE) - mReservedValue;
                    }).blockingFirst();
        } else {
            showSnackBar(ConnectivityUtils.isNetworkAvailable(this)
                    ? R.string.something_went_wrong_error : R.string.no_internet_connection);
        }
    }

    private void showSnackBar(@StringRes int text) {
        if (mSnackBar == null) {
            mSnackBar = Snackbar.make(findViewById(R.id.container), text, Snackbar.LENGTH_LONG);

            mSnackBar.addCallback(new Snackbar.Callback() {
                @Override
                public void onShown(Snackbar sb) {
                    super.onShown(sb);
                }

                @Override
                public void onDismissed(Snackbar transientBottomBar, int event) {
                    super.onDismissed(transientBottomBar, event);
                    mSnackBar = null;
                }
            });
            mSnackBar.show();
        }
    }
}
