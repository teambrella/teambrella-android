package com.teambrella.android.ui.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.teambrella.android.data.base.IDataHost;
import com.teambrella.android.data.base.IDataPager;
import com.teambrella.android.data.base.TeambrellaDataFragment;
import com.teambrella.android.data.base.TeambrellaDataPagerFragment;

import io.reactivex.Notification;
import io.reactivex.Observable;

/**
 * A Data Host Activity
 */
public abstract class ADataHostActivity extends AppCompatActivity implements IDataHost {

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

    protected abstract String[] getDataTags();

    protected abstract String[] getPagerTags();

    protected abstract TeambrellaDataFragment getDataFragment(String tag);

    protected abstract TeambrellaDataPagerFragment getDataPagerFragment(String tag);

}
