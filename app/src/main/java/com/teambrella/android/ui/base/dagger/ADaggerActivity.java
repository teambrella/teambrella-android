package com.teambrella.android.ui.base.dagger;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * Teambrella Activity
 */
public abstract class ADaggerActivity<T> extends AppCompatActivity implements IDaggerActivity<T> {

    private T mComponent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        mComponent = createComponent();
        super.onCreate(savedInstanceState);
    }

    @Override
    public T getComponent() {
        return mComponent;
    }

    protected abstract T createComponent();
}
