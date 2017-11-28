package com.teambrella.android.ui.base;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.TeambrellaServerException;
import com.teambrella.android.data.base.IDataHost;
import com.teambrella.android.data.base.IDataPager;
import com.teambrella.android.data.base.TeambrellaDataFragment;
import com.teambrella.android.data.base.TeambrellaDataPagerFragment;
import com.teambrella.android.ui.TeambrellaUser;
import com.teambrella.android.ui.demo.NewDemoSessionActivity;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import io.reactivex.Notification;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

/**
 * A Data Host Activity
 */
public abstract class ADataHostActivity extends AppCompatActivity implements IDataHost {


    private List<Disposable> mNewSessionDisposables = new LinkedList<>();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        for (String tag : getDataTags()) {
            if (fragmentManager.findFragmentByTag(tag) == null) {
                transaction.add(getDataFragment(tag), tag);
            }
        }

        for (String tag : getPagerTags()) {
            if (fragmentManager.findFragmentByTag(tag) == null) {
                transaction.add(getDataPagerFragment(tag), tag);
            }
        }


        if (!transaction.isEmpty()) {
            transaction.commit();
        }
    }

    @Override
    public Observable<Notification<JsonObject>> getObservable(String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        TeambrellaDataFragment dataFragment = (TeambrellaDataFragment) fragmentManager.findFragmentByTag(tag);
        return dataFragment != null ? dataFragment.getObservable() : null;
    }

    @Override
    public void load(String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        TeambrellaDataFragment dataFragment = (TeambrellaDataFragment) fragmentManager.findFragmentByTag(tag);
        if (dataFragment != null) {
            dataFragment.load();
        }
    }

    @Override
    public IDataPager<JsonArray> getPager(String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        TeambrellaDataPagerFragment dataFragment = (TeambrellaDataPagerFragment) fragmentManager.findFragmentByTag(tag);
        if (dataFragment != null) {
            return dataFragment.getPager();
        }
        return null;
    }


    @Override
    protected void onStart() {
        super.onStart();
        TeambrellaUser user = TeambrellaUser.get(this);
        if (user.isDemoUser()) {
            String[] dataTags = getDataTags();
            if (dataTags != null && dataTags.length > 0) {
                for (String tag : dataTags) {
                    mNewSessionDisposables.add(getObservable(tag).subscribe(this::checkDemoAuthError));
                }
            }
            String[] pagerTags = getPagerTags();
            if (pagerTags != null && pagerTags.length > 0) {
                for (String tag : pagerTags) {
                    mNewSessionDisposables.add(getPager(tag).getObservable().subscribe(this::checkDemoAuthError));
                }
            }
        }
    }

    private void checkDemoAuthError(Notification<JsonObject> notification) {
        if (notification.isOnError()) {
            Throwable error = notification.getError();
            if (error instanceof TeambrellaServerException) {
                TeambrellaServerException serverException = (TeambrellaServerException) error;
                switch (serverException.getErrorCode()) {
                    case TeambrellaModel.VALUE_STATUS_RESULT_CODE_AUTH:
                        if (!isFinishing()) {
                            startActivity(new Intent(this, NewDemoSessionActivity.class));
                            finish();
                        }
                }
            }
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        Iterator<Disposable> iterator = mNewSessionDisposables.iterator();
        while (iterator.hasNext()) {
            Disposable disposable = iterator.next();
            if (disposable != null && !disposable.isDisposed()) {
                disposable.dispose();
                iterator.remove();
            }
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);
    }

    protected abstract String[] getDataTags();

    protected abstract String[] getPagerTags();

    protected abstract TeambrellaDataFragment getDataFragment(String tag);

    protected abstract TeambrellaDataPagerFragment getDataPagerFragment(String tag);

}
